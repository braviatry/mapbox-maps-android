package com.mapbox.maps.testapp.examples

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.mapbox.common.Logger
import com.mapbox.common.OfflineSwitch
import com.mapbox.geojson.Point
import com.mapbox.maps.*
import com.mapbox.maps.testapp.databinding.ActivityLegacyOfflineBinding

/**
 * Example app that downloads an offline region and when succeeded
 * shows a button to load a map at the offline region definition.
 */
class LegacyOfflineActivity : AppCompatActivity() {

  private lateinit var offlineManager: OfflineRegionManager
  private lateinit var offlineRegion: OfflineRegion
  private var mapView: MapView? = null
  private lateinit var binding: ActivityLegacyOfflineBinding

  private val regionObserver: OfflineRegionObserver = object : OfflineRegionObserver() {
    override fun mapboxTileCountLimitExceeded(limit: Long) {
      Logger.e(TAG, "Mapbox tile count max (= $limit) has exceeded!")
    }

    override fun statusChanged(status: OfflineRegionStatus) {
      Logger.d(
        TAG,
        "${status.completedResourceCount}/${status.requiredResourceCount} resources; ${status.completedResourceSize} bytes downloaded."
      )
      if (status.downloadState == OfflineRegionDownloadState.INACTIVE) {
        downloadComplete()
        return
      }
    }

    override fun responseError(error: ResponseError) {
      Logger.e(TAG, "onError: ${error.reason}, ${error.message}")
      offlineRegion.setOfflineRegionDownloadState(OfflineRegionDownloadState.INACTIVE)
    }
  }

  private val callback: OfflineRegionCreateCallback = OfflineRegionCreateCallback { expected ->
    if (expected.isValue) {
      expected.value?.let {
        offlineRegion = it
        it.setOfflineRegionObserver(regionObserver)
        it.setOfflineRegionDownloadState(OfflineRegionDownloadState.ACTIVE)
      }
    } else {
      Logger.e(TAG, expected.error!!)
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityLegacyOfflineBinding.inflate(layoutInflater)
    setContentView(binding.root)
    offlineManager = OfflineRegionManager(MapInitOptions.getDefaultResourceOptions(this))
    offlineManager.createOfflineRegion(
      OfflineRegionGeometryDefinition.Builder()
        .geometry(point)
        .pixelRatio(2f)
        .minZoom(zoom - 2)
        .maxZoom(zoom + 2)
        .styleURL(styleUrl)
        .glyphsRasterizationMode(GlyphsRasterizationMode.NO_GLYPHS_RASTERIZED_LOCALLY)
        .build(),
      callback
    )
  }

  @SuppressLint("Lifecycle")
  private fun downloadComplete() {
    // Disable network stack, so that the map can only load from downloaded region.
    OfflineSwitch.getInstance().isMapboxStackConnected = false
    binding.downloadProgress.visibility = View.GONE
    binding.showMapButton.visibility = View.VISIBLE
    binding.showMapButton.setOnClickListener {
      it.visibility = View.GONE
      // create mapView
      mapView = MapView(this@LegacyOfflineActivity).also { mapview ->
        val mapboxMap = mapview.getMapboxMap()
        mapboxMap.setCamera(CameraOptions.Builder().zoom(zoom).center(point).build())
        mapboxMap.loadStyleUri(styleUrl)
      }
      setContentView(mapView)
      mapView?.onStart()
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    offlineRegion.invalidate { }
    OfflineSwitch.getInstance().isMapboxStackConnected = true
  }

  companion object {
    private const val TAG = "Offline"
    private const val zoom = 16.0
    private val point: Point = Point.fromLngLat(57.818901, 20.071357)
    private const val styleUrl = Style.SATELLITE
  }
}