package com.mapbox.maps.renderer

import android.graphics.Bitmap
import android.graphics.Matrix
import android.opengl.GLES20
import androidx.annotation.*
import com.mapbox.common.Logger
import com.mapbox.maps.*
import com.mapbox.maps.MapView.OnSnapshotReady
import com.mapbox.maps.Size
import com.mapbox.maps.extension.observable.getRenderFrameFinishedEventData
import com.mapbox.maps.plugin.delegates.listeners.eventdata.RenderMode
import com.mapbox.maps.renderer.gl.PixelReader
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

internal abstract class MapboxRenderer : MapClient() {

  @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
  internal lateinit var renderThread: MapboxRenderThread

  @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
  internal var map: MapInterface? = null

  private var width: Int = 0
  private var height: Int = 0

  @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
  internal var pixelReader: PixelReader? = null

  internal var needDestroy = false

  // in order to prevent snapshots being black we capture first moment
  // when map is rendered fully
  @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
  internal var readyForSnapshot = AtomicBoolean(false)
  private val observer = object : Observer() {
    override fun notify(event: Event) {
      if (event.type == MapEvents.RENDER_FRAME_FINISHED) {
        val data = event.getRenderFrameFinishedEventData()
        if (data.renderMode == RenderMode.FULL) {
          readyForSnapshot.set(true)
        }
      }
    }
  }

  @UiThread
  fun onDestroy() {
    // we destroy and stop thread after surface or texture is destroyed
    needDestroy = true
    renderThread.fpsChangedListener = null
  }

  @AnyThread
  @Synchronized
  fun setMap(map: MapInterface) {
    this.map = map
  }

  @AnyThread
  override fun scheduleRepaint() {
    renderThread.requestRender()
  }

  @AnyThread
  override fun scheduleTask(task: Task) {
    renderThread.queueEvent {
      task.run()
    }
  }

  @WorkerThread
  fun onSurfaceCreated() {
    map?.createRenderer()
  }

  @WorkerThread
  fun onSurfaceChanged(width: Int, height: Int) {
    if (width != this.width || height != this.height) {
      this.width = width
      this.height = height
      GLES20.glViewport(0, 0, width, height)
      map?.size = Size(width.toFloat(), height.toFloat())
    }
  }

  @WorkerThread
  fun onSurfaceDestroyed() {
    map?.destroyRenderer()
    pixelReader?.release()
    pixelReader = null
  }

  @WorkerThread
  fun onDrawFrame() {
    map?.render()
  }

  @UiThread
  fun onStop() {
    renderThread.pause()
    map?.unsubscribe(observer)
    readyForSnapshot.set(false)
  }

  @UiThread
  fun onStart() {
    renderThread.resume()
    map?.subscribe(observer, listOf(MapEvents.RENDER_FRAME_FINISHED))
  }

  @AnyThread
  fun queueRenderEvent(runnable: Runnable) {
    renderThread.queueRenderEvent(runnable)
  }

  @AnyThread
  fun queueEvent(runnable: Runnable) {
    renderThread.queueEvent(runnable)
  }

  @AnyThread
  @Synchronized
  fun snapshot(): Bitmap? {
    if (!readyForSnapshot.get()) {
      Logger.e(TAG, "Could not take map snapshot because map is not ready yet.")
      return null
    }
    val lock = ReentrantLock()
    val waitCondition = lock.newCondition()
    lock.withLock {
      var snapshot: Bitmap? = null
      renderThread.queueRenderEvent {
        lock.withLock {
          snapshot = performSnapshot()
          waitCondition.signal()
        }
      }
      waitCondition.await(1, TimeUnit.SECONDS)
      return snapshot
    }
  }

  @AnyThread
  @Synchronized
  fun snapshot(listener: OnSnapshotReady) {
    if (!readyForSnapshot.get()) {
      Logger.e(TAG, "Could not take map snapshot because map is not ready yet.")
      listener.onSnapshotReady(null)
    }
    renderThread.queueRenderEvent {
      listener.onSnapshotReady(performSnapshot())
    }
  }

  @AnyThread
  fun setMaximumFps(fps: Int) {
    renderThread.setMaximumFps(fps)
  }

  @AnyThread
  @Synchronized
  fun setOnFpsChangedListener(listener: OnFpsChangedListener) {
    renderThread.fpsChangedListener = listener
  }

  @WorkerThread
  private fun performSnapshot(): Bitmap? {
    if (width == 0 && height == 0) {
      Logger.e(TAG, "Could not take map snapshot because map is not ready yet.")
      return null
    }

    if (pixelReader == null || pixelReader?.width != width || pixelReader?.height != height) {
      pixelReader?.release()
      pixelReader = PixelReader(width, height)
    }

    pixelReader?.let {
      val buffer = it.readPixels()
      buffer.rewind()
      val flipped = Bitmap.createBitmap(
        width,
        height,
        Bitmap.Config.ARGB_8888
      ).apply {
        copyPixelsFromBuffer(buffer)
      }
      val cx = width / 2.0f
      val cy = height / 2.0f
      try {
        return Bitmap.createBitmap(
          flipped,
          0, 0,
          width, height,
          Matrix().apply { postScale(1f, -1f, cx, cy) },
          true
        )
      } finally {
        flipped.recycle()
      }
    }
    return null
  }

  companion object {
    private const val TAG = "Mbgl-Renderer"
  }
}