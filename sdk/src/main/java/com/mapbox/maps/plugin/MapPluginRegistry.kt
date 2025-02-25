package com.mapbox.maps.plugin

import android.view.MotionEvent
import android.view.View
import com.mapbox.maps.*
import com.mapbox.maps.plugin.delegates.MapDelegateProvider
import com.mapbox.maps.plugin.gestures.GesturesPlugin
import com.mapbox.maps.plugin.lifecycle.MapboxLifecyclePlugin
import java.util.concurrent.CopyOnWriteArrayList

internal class MapPluginRegistry(
  private val mapDelegateProvider: MapDelegateProvider
) {

  private enum class State {
    STARTED,
    STOPPED,
  }

  private var mapState = State.STOPPED
    set(value) {
      if (value != field) {
        field = value
        when (field) {
          State.STARTED -> plugins.values.forEach {
            if (it is LifecyclePlugin) {
              it.onStart()
            }
          }
          State.STOPPED -> plugins.values.forEach {
            if (it is LifecyclePlugin) {
              it.onStop()
            }
          }
        }
      }
    }

  private val plugins = mutableMapOf<String, MapPlugin>()
  private val viewPlugins = mutableMapOf<ViewPlugin, View>()
  private val cameraPlugins = CopyOnWriteArrayList<MapCameraPlugin>()
  private val gesturePlugins = CopyOnWriteArrayList<GesturesPlugin>()
  private val styleObserverPlugins = CopyOnWriteArrayList<MapStyleObserverPlugin>()
  private val mapSizePlugins = CopyOnWriteArrayList<MapSizePlugin>()
  private var mapboxLifecyclePlugin: MapboxLifecyclePlugin? = null

  fun createPlugin(
    mapView: MapView?,
    mapInitOptions: MapInitOptions,
    plugin: Plugin
  ) {
    plugin.instance?.let { mapPlugin ->
      if (!plugins.containsKey(plugin.id)) {

        if (plugin.instance is ViewPlugin && mapView == null) {
          // throw if view plugin if host is not a MapView (eg. MapSurface)
          throw InvalidViewPluginHostException("Cause: ${mapPlugin.javaClass}")
        }

        plugins[plugin.id] = mapPlugin
        mapPlugin.onDelegateProvider(mapDelegateProvider)

        if (mapPlugin is ViewPlugin) {
          val pluginView = mapPlugin.bind(
            mapView!!,
            mapInitOptions.attrs,
            mapInitOptions.mapOptions.pixelRatio
          )
          mapView.addView(pluginView)
          mapPlugin.onPluginView(pluginView)
          viewPlugins[mapPlugin] = pluginView
        }

        if (mapPlugin is ContextBinder) {
          mapPlugin.bind(
            mapInitOptions.context,
            mapInitOptions.attrs,
            mapInitOptions.mapOptions.pixelRatio
          )
        }

        if (mapPlugin is MapSizePlugin) {
          mapSizePlugins.add(mapPlugin)
        }

        if (mapPlugin is MapCameraPlugin) {
          cameraPlugins.add(mapPlugin)
        }

        if (mapPlugin is GesturesPlugin) {
          gesturePlugins.add(mapPlugin)
        }

        if (mapPlugin is MapStyleObserverPlugin) {
          styleObserverPlugins.add(mapPlugin)
        }

        if (mapPlugin is MapboxLifecyclePlugin) {
          mapboxLifecyclePlugin = mapPlugin
        }

        mapPlugin.initialize()

        if (mapState == State.STARTED && mapPlugin is LifecyclePlugin) {
          mapPlugin.onStart()
        } else {
        }
      } else {
        plugins[plugin.id]?.initialize()
      }
    } ?: throw RuntimeException("MapPlugin instance is missing for ${plugin.id}!")
  }

  @Suppress("UNCHECKED_CAST")
  fun <T> getPlugin(id: String): T? = plugins[id] as T

  fun onStart() {
    mapState = State.STARTED
  }

  fun onStop() {
    mapState = State.STOPPED
  }

  fun onTouch(event: MotionEvent?): Boolean {
    var onTouchResult = false
    gesturePlugins.forEach {
      onTouchResult = it.onTouchEvent(event) || onTouchResult
    }
    return onTouchResult
  }

  fun onGenericMotionEvent(event: MotionEvent): Boolean {
    var onGenericMotionEventResult = false
    gesturePlugins.forEach {
      onGenericMotionEventResult = it.onGenericMotionEvent(event) || onGenericMotionEventResult
    }
    return onGenericMotionEventResult
  }

  fun onSizeChanged(width: Int, height: Int) {
    for (mapSizePlugin in mapSizePlugins) {
      mapSizePlugin.onSizeChanged(width, height)
    }
  }

  fun onCameraMove(cameraState: CameraState) {
    cameraPlugins.forEach {
      it.onCameraMove(
        cameraState.center.latitude(),
        cameraState.center.longitude(),
        cameraState.zoom,
        cameraState.pitch,
        cameraState.bearing,
        cameraState.padding.let { insets ->
          arrayOf(insets.left, insets.top, insets.right, insets.bottom)
        }
      )
    }
  }

  fun onStyleChanged(style: Style) {
    styleObserverPlugins.forEach {
      it.onStyleChanged(style)
    }
  }

  fun cleanup() {
    plugins.forEach {
      it.value.cleanup()
    }
  }

  fun onAttachedToWindow(mapView: MapView) {
    mapboxLifecyclePlugin?.registerLifecycleObserver(mapView, mapView)
  }
}