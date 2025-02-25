// This file is generated.

package com.mapbox.maps.extension.style.sources.generated

import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import androidx.annotation.VisibleForTesting
import com.mapbox.bindgen.Value
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.GeoJson
import com.mapbox.geojson.Geometry
import com.mapbox.maps.StyleManager
import com.mapbox.maps.extension.style.expressions.generated.Expression
import com.mapbox.maps.extension.style.layers.properties.PropertyValue
import com.mapbox.maps.extension.style.sources.OnGeoJsonParsed
import com.mapbox.maps.extension.style.sources.Source
import com.mapbox.maps.extension.style.types.SourceDsl
import com.mapbox.maps.extension.style.utils.TypeUtils
import com.mapbox.maps.extension.style.utils.silentUnwrap
import com.mapbox.maps.extension.style.utils.toValue

/**
 * A GeoJSON data source.
 *
 * @see [The online documentation](https://docs.mapbox.com/mapbox-gl-js/style-spec/sources/#geojson)
 *
 */
class GeoJsonSource(builder: Builder) : Source(builder.sourceId) {
  private var geoJsonParsed = false
  private val onGeoJsonParsedListenerList = mutableListOf<OnGeoJsonParsed>()
  private val workerHandler by lazy {
    Handler(workerThread.looper)
  }
  private constructor(
    builder: Builder,
    rawGeoJson: GeoJson?,
    onGeoJsonParsed: OnGeoJsonParsed
  ) : this(builder) {
    rawGeoJson?.let {
      ignoreParsedGeoJsonRegistry[sourceId] = false
      onGeoJsonParsedListenerList.add(onGeoJsonParsed)
      workerHandler.post {
        val property = it.toPropertyValue()
        mainHandler.post {
          geoJsonParsed = true
          // we set parsed data when sync setter was not called during background work
          if (ignoreParsedGeoJsonRegistry[sourceId] == false) {
            setProperty(property, throwRuntimeException = false)
            onGeoJsonParsedListenerList.forEach {
              it.onGeoJsonParsed(this)
            }
          }
        }
      }
    } ?: run { geoJsonParsed = true }
  }

  /**
   * Add listener that gets invoked when feature, featureCollection or geometry data is parsed.
   *
   * @param listener Listener returning GeoJsonSource when data is parsed.
   */
  fun addOnGeoJsonParsedListener(listener: OnGeoJsonParsed) {
    onGeoJsonParsedListenerList.add(listener)
    if (geoJsonParsed) {
      listener.onGeoJsonParsed(this)
    }
  }

  /**
   * Remove listener that gets invoked when feature, featureCollection or geometry data is parsed.
   *
   * @param listener Listener to be removed.
   */
  fun removeOnGeoJsonParsedListener(listener: OnGeoJsonParsed) {
    onGeoJsonParsedListenerList.remove(listener)
  }

  init {
    sourceProperties.putAll(builder.properties)
    volatileSourceProperties.putAll(builder.volatileProperties)
  }

  /**
   * Get the type of the current source as a String.
   */
  override fun getType(): String {
    return "geojson"
  }

  /**
   * A URL to a GeoJSON file, or inline GeoJSON.
   *
   * If method is called while another asynchronous method is parsing data - asynchronous method will not
   * apply when data is parsed.
   */
  fun data(value: String) = apply {
    ignoreParsedGeoJsonRegistry[sourceId] = true
    workerHandler.removeCallbacksAndMessages(null)
    setProperty(PropertyValue("data", TypeUtils.wrapToValue(value)))
  }

  /**
   * A URL to a GeoJSON file, or inline GeoJSON.
   *
   * Note: Getter for plain Geojson string data is not supported due to performance consideration.
   */
  val data: String?
    /**
     * Get the Data property
     *
     * @return String
     */
    get() = getPropertyValue("data")

  /**
   * A URL to a GeoJSON file, or inline GeoJSON.
   */
  fun url(value: String) = apply {
    data(value)
  }

  /**
   * Maximum zoom level at which to create vector tiles (higher means greater detail at high zoom
   * levels).
   */
  val maxzoom: Long?
    /**
     * Get the Maxzoom property
     *
     * @return Long
     */
    get() = getPropertyValue("maxzoom")

  /**
   * Contains an attribution to be displayed when the map is shown to a user.
   */
  val attribution: String?
    /**
     * Get the Attribution property
     *
     * @return String
     */
    get() = getPropertyValue("attribution")

  /**
   * Size of the tile buffer on each side. A value of 0 produces no buffer. A
   * value of 512 produces a buffer as wide as the tile itself. Larger values produce fewer
   * rendering artifacts near tile edges and slower performance.
   */
  val buffer: Long?
    /**
     * Get the Buffer property
     *
     * @return Long
     */
    get() = getPropertyValue("buffer")

  /**
   * Douglas-Peucker simplification tolerance (higher means simpler geometries and faster performance).
   */
  val tolerance: Double?
    /**
     * Get the Tolerance property
     *
     * @return Double
     */
    get() = getPropertyValue("tolerance")

  /**
   * If the data is a collection of point features, setting this to true clusters the points
   * by radius into groups. Cluster groups become new `Point` features in the source with additional properties:
   * - `cluster` Is `true` if the point is a cluster
   * - `cluster_id` A unqiue id for the cluster to be used in conjunction with the
   * [cluster inspection methods](https://www.mapbox.com/mapbox-gl-js/api/#geojsonsource#getclusterexpansionzoom)
   * - `point_count` Number of original points grouped into this cluster
   * - `point_count_abbreviated` An abbreviated point count
   */
  val cluster: Boolean?
    /**
     * Get the Cluster property
     *
     * @return Boolean
     */
    get() = getPropertyValue("cluster")

  /**
   * Radius of each cluster if clustering is enabled. A value of 512 indicates a radius equal
   * to the width of a tile.
   */
  val clusterRadius: Long?
    /**
     * Get the ClusterRadius property
     *
     * @return Long
     */
    get() = getPropertyValue("clusterRadius")

  /**
   * Max zoom on which to cluster points if clustering is enabled. Defaults to one zoom less
   * than maxzoom (so that last zoom features are not clustered). Clusters are re-evaluated at integer zoom
   * levels so setting clusterMaxZoom to 14 means the clusters will be displayed until z15.
   */
  val clusterMaxZoom: Long?
    /**
     * Get the ClusterMaxZoom property
     *
     * @return Long
     */
    get() = getPropertyValue("clusterMaxZoom")

  /**
   * An object defining custom properties on the generated clusters if clustering is enabled, aggregating values from
   * clustered points. Has the form `{"property_name": [operator, map_expression]}`. `operator` is any expression function that accepts at
   * least 2 operands (e.g. `"+"` or `"max"`) — it accumulates the property value from clusters/points the
   * cluster contains; `map_expression` produces the value of a single point.
   *
   * Example: `{"sum": ["+", ["get", "scalerank"]]}`.
   *
   * For more advanced use cases, in place of `operator`, you can use a custom reduce expression
   * that references a special `["accumulated"]` value, e.g.:
   * `{"sum": [["+", ["accumulated"], ["get", "sum"]], ["get", "scalerank"]]}`
   */
  val clusterProperties: HashMap<String, Any>?
    /**
     * Get the ClusterProperties property
     *
     * @return HashMap<String, Any>
     */
    get() = getPropertyValue("clusterProperties")

  /**
   * Whether to calculate line distance metrics. This is required for line layers that specify `line-gradient` values.
   */
  val lineMetrics: Boolean?
    /**
     * Get the LineMetrics property
     *
     * @return Boolean
     */
    get() = getPropertyValue("lineMetrics")

  /**
   * Whether to generate ids for the geojson features. When enabled, the `feature.id` property will be auto
   * assigned based on its index in the `features` array, over-writing any previous values.
   */
  val generateId: Boolean?
    /**
     * Get the GenerateId property
     *
     * @return Boolean
     */
    get() = getPropertyValue("generateId")

  /**
   * When loading a map, if `PrefetchZoomDelta` is set to any number greater than 0, the map
   * will first request a tile for `zoom - delta` in a attempt to display a full
   * map at lower resolution as quick as possible. It will get clamped at the tile source
   * minimum zoom. The default `delta` is 4.
   */
  fun prefetchZoomDelta(value: Long = 4L) = apply {
    setVolatileProperty(PropertyValue("prefetch-zoom-delta", TypeUtils.wrapToValue(value)))
  }

  /**
   * When loading a map, if `PrefetchZoomDelta` is set to any number greater than 0, the map
   * will first request a tile for `zoom - delta` in a attempt to display a full
   * map at lower resolution as quick as possible. It will get clamped at the tile source
   * minimum zoom. The default `delta` is 4.
   */
  val prefetchZoomDelta: Long?
    /**
     * Get the PrefetchZoomDelta property
     *
     * @return Long
     */
    get() = getPropertyValue("prefetch-zoom-delta")

  /**
   * Add a Feature to the GeojsonSource.
   * If [onDataParsed] is provided and not null - data will be loaded in async mode.
   * Otherwise method will be synchronous.
   *
   * If synchronous method is called while another asynchronous method is parsing data -
   * asynchronous method will not apply when data is parsed.
   *
   * @param value the feature collection
   * @param onDataParsed optional callback notifying when data is parsed on a worker thread
   */
  fun feature(
    value: Feature,
    onDataParsed: ((GeoJsonSource) -> Unit)? = null
  ) = applyGeoJsonData(value, onDataParsed)

  /**
   * Add a FeatureCollection to the GeojsonSource.
   * If [onDataParsed] is provided and not null - data will be loaded in async mode.
   * Otherwise method will be synchronous.
   *
   * If synchronous method is called while another asynchronous method is parsing data -
   * asynchronous method will not apply when data is parsed.
   *
   * @param value the feature collection
   * @param onDataParsed optional callback notifying when data is parsed on a worker thread
   */
  fun featureCollection(
    value: FeatureCollection,
    onDataParsed: ((GeoJsonSource) -> Unit)? = null
  ) = applyGeoJsonData(value, onDataParsed)

  /**
   * Add a Geometry to the GeojsonSource.
   * If [onDataParsed] is provided and not null - data will be loaded in async mode.
   * Otherwise method will be synchronous.
   *
   * If synchronous method is called while another asynchronous method is parsing data -
   * asynchronous method will not apply when data is parsed.
   *
   * @param value the feature collection
   * @param onDataParsed optional callback notifying when data is parsed on a worker thread
   */
  fun geometry(
    value: Geometry,
    onDataParsed: ((GeoJsonSource) -> Unit)? = null
  ) = applyGeoJsonData(value, onDataParsed)

  private fun GeoJson.toPropertyValue() = PropertyValue(
    "data",
    when (this) {
      is Geometry -> toValue()
      is FeatureCollection -> toValue()
      is Feature -> toValue()
      else -> RuntimeException("GeoJson data must be Geometry, FeatureCollection or Feature!")
    }
  )

  private fun applyGeoJsonData(
    data: GeoJson,
    onDataParsed: ((GeoJsonSource) -> Unit)?
  ): GeoJsonSource = apply {
    onDataParsed?.let { listener ->
      ignoreParsedGeoJsonRegistry[sourceId] = false
      // remove any events from queue before posting this task
      workerHandler.removeCallbacksAndMessages(null)
      workerHandler.post {
        val property = data.toPropertyValue()
        mainHandler.post {
          // we set parsed data when sync setter was not called during background work
          if (ignoreParsedGeoJsonRegistry[sourceId] == false) {
            setProperty(property, throwRuntimeException = false)
            listener.invoke(this)
          }
        }
      }
    } ?: run {
      // if any task is running - set flag to skip it when it is finished
      ignoreParsedGeoJsonRegistry[sourceId] = true
      // remove any events from queue - they should not overwrite data set synchronously
      workerHandler.removeCallbacksAndMessages(null)
      setProperty(data.toPropertyValue())
    }
  }

  /**
   * Builder for GeoJsonSource.
   *
   * @param sourceId the ID of the source
   * @param onGeoJsonParsed callback invoked when data is parsed
   */
  @SourceDsl
  class Builder(
    val sourceId: String,
    private val onGeoJsonParsed: OnGeoJsonParsed
  ) {

    private var rawGeoJson: GeoJson? = null
    internal val properties = HashMap<String, PropertyValue<*>>()
    // Properties that only settable after the source is added to the style.
    internal val volatileProperties = HashMap<String, PropertyValue<*>>()

    /**
     * A URL to a GeoJSON file, or inline GeoJSON.
     */
    fun data(value: String) = apply {
      rawGeoJson = null
      val propertyValue = PropertyValue("data", TypeUtils.wrapToValue(value))
      properties[propertyValue.propertyName] = propertyValue
    }

    /**
     * A URL to a GeoJSON file, or inline GeoJSON.
     */
    fun url(value: String) = apply {
      data(value)
    }

    /**
     * Maximum zoom level at which to create vector tiles (higher means greater detail at high zoom
     * levels).
     */
    fun maxzoom(value: Long = 18L) = apply {
      val propertyValue = PropertyValue("maxzoom", TypeUtils.wrapToValue(value))
      properties[propertyValue.propertyName] = propertyValue
    }

    /**
     * Contains an attribution to be displayed when the map is shown to a user.
     */
    fun attribution(value: String) = apply {
      val propertyValue = PropertyValue("attribution", TypeUtils.wrapToValue(value))
      properties[propertyValue.propertyName] = propertyValue
    }

    /**
     * Size of the tile buffer on each side. A value of 0 produces no buffer. A
     * value of 512 produces a buffer as wide as the tile itself. Larger values produce fewer
     * rendering artifacts near tile edges and slower performance.
     */
    fun buffer(value: Long = 128L) = apply {
      val propertyValue = PropertyValue("buffer", TypeUtils.wrapToValue(value))
      properties[propertyValue.propertyName] = propertyValue
    }

    /**
     * Douglas-Peucker simplification tolerance (higher means simpler geometries and faster performance).
     */
    fun tolerance(value: Double = 0.375) = apply {
      val propertyValue = PropertyValue("tolerance", TypeUtils.wrapToValue(value))
      properties[propertyValue.propertyName] = propertyValue
    }

    /**
     * If the data is a collection of point features, setting this to true clusters the points
     * by radius into groups. Cluster groups become new `Point` features in the source with additional properties:
     * - `cluster` Is `true` if the point is a cluster
     * - `cluster_id` A unqiue id for the cluster to be used in conjunction with the
     * [cluster inspection methods](https://www.mapbox.com/mapbox-gl-js/api/#geojsonsource#getclusterexpansionzoom)
     * - `point_count` Number of original points grouped into this cluster
     * - `point_count_abbreviated` An abbreviated point count
     */
    fun cluster(value: Boolean = false) = apply {
      val propertyValue = PropertyValue("cluster", TypeUtils.wrapToValue(value))
      properties[propertyValue.propertyName] = propertyValue
    }

    /**
     * Radius of each cluster if clustering is enabled. A value of 512 indicates a radius equal
     * to the width of a tile.
     */
    fun clusterRadius(value: Long = 50L) = apply {
      val propertyValue = PropertyValue("clusterRadius", TypeUtils.wrapToValue(value))
      properties[propertyValue.propertyName] = propertyValue
    }

    /**
     * Max zoom on which to cluster points if clustering is enabled. Defaults to one zoom less
     * than maxzoom (so that last zoom features are not clustered). Clusters are re-evaluated at integer zoom
     * levels so setting clusterMaxZoom to 14 means the clusters will be displayed until z15.
     */
    fun clusterMaxZoom(value: Long) = apply {
      val propertyValue = PropertyValue("clusterMaxZoom", TypeUtils.wrapToValue(value))
      properties[propertyValue.propertyName] = propertyValue
    }

    /**
     * An object defining custom properties on the generated clusters if clustering is enabled, aggregating values from
     * clustered points. Has the form `{"property_name": [operator, map_expression]}`. `operator` is any expression function that accepts at
     * least 2 operands (e.g. `"+"` or `"max"`) — it accumulates the property value from clusters/points the
     * cluster contains; `map_expression` produces the value of a single point.
     *
     * Example: `{"sum": ["+", ["get", "scalerank"]]}`.
     *
     * For more advanced use cases, in place of `operator`, you can use a custom reduce expression
     * that references a special `["accumulated"]` value, e.g.:
     * `{"sum": [["+", ["accumulated"], ["get", "sum"]], ["get", "scalerank"]]}`
     */
    fun clusterProperties(value: HashMap<String, Any>) = apply {
      val propertyValue = PropertyValue("clusterProperties", TypeUtils.wrapToValue(value))
      properties[propertyValue.propertyName] = propertyValue
    }

    /**
     * An object defining custom properties on the generated clusters if clustering is enabled, aggregating values from
     * clustered points. Has the form `{"property_name": [operator, map_expression]}`. `operator` is any expression function that accepts at
     * least 2 operands (e.g. `"+"` or `"max"`) — it accumulates the property value from clusters/points the
     * cluster contains; `map_expression` produces the value of a single point.
     *
     * Example: `{"sum": ["+", ["get", "scalerank"]]}`.
     *
     * For more advanced use cases, in place of `operator`, you can use a custom reduce expression
     * that references a special `["accumulated"]` value, e.g.:
     * `{"sum": [["+", ["accumulated"], ["get", "sum"]], ["get", "scalerank"]]}`
     *
     * @param propertyName name of the property
     * @param operatorExpr operatorExpr is any expression function that accepts at least 2 operands (e.g. "+" or
     * "max").
     * It accumulates the property value from clusters/points the cluster contains. It can either be a literal
     * with single operator, or be a valid expression
     * @param mapExpr map expression produces the value of a single point, it shall be a valid
     * expression
     *
     */
    fun clusterProperty(propertyName: String, operatorExpr: Expression, mapExpr: Expression) =
      apply {
        @Suppress("UNCHECKED_CAST")
        val options: HashMap<String, Value> =
          properties["clusterProperties"]?.value?.contents as? HashMap<String, Value>
            ?: HashMap()
        options[propertyName] = Value(listOf(operatorExpr, mapExpr))
        val propertyValue = PropertyValue("clusterProperties", TypeUtils.wrapToValue(options))
        properties[propertyValue.propertyName] = propertyValue
      }

    /**
     * An object defining custom properties on the generated clusters if clustering is enabled, aggregating values from
     * clustered points. Has the form `{"property_name": [operator, map_expression]}`. `operator` is any expression function that accepts at
     * least 2 operands (e.g. `"+"` or `"max"`) — it accumulates the property value from clusters/points the
     * cluster contains; `map_expression` produces the value of a single point.
     *
     * Example: `{"sum": ["+", ["get", "scalerank"]]}`.
     *
     * For more advanced use cases, in place of `operator`, you can use a custom reduce expression
     * that references a special `["accumulated"]` value, e.g.:
     * `{"sum": [["+", ["accumulated"], ["get", "sum"]], ["get", "scalerank"]]}`
     *
     * @param propertyName name of the property
     * @param mapExpr map expression produces the value of a single point, it shall be a valid
     * expression
     *
     */
    fun clusterProperty(propertyName: String, mapExpr: Expression) =
      apply {
        @Suppress("UNCHECKED_CAST")
        val options: HashMap<String, Value> =
          properties["clusterProperties"]?.value?.contents as? HashMap<String, Value>
            ?: HashMap()
        options[propertyName] = mapExpr
        val propertyValue = PropertyValue("clusterProperties", TypeUtils.wrapToValue(options))
        properties[propertyValue.propertyName] = propertyValue
      }

    /**
     * Whether to calculate line distance metrics. This is required for line layers that specify `line-gradient` values.
     */
    fun lineMetrics(value: Boolean = false) = apply {
      val propertyValue = PropertyValue("lineMetrics", TypeUtils.wrapToValue(value))
      properties[propertyValue.propertyName] = propertyValue
    }

    /**
     * Whether to generate ids for the geojson features. When enabled, the `feature.id` property will be auto
     * assigned based on its index in the `features` array, over-writing any previous values.
     */
    fun generateId(value: Boolean = false) = apply {
      val propertyValue = PropertyValue("generateId", TypeUtils.wrapToValue(value))
      properties[propertyValue.propertyName] = propertyValue
    }

    /**
     * When loading a map, if `PrefetchZoomDelta` is set to any number greater than 0, the map
     * will first request a tile for `zoom - delta` in a attempt to display a full
     * map at lower resolution as quick as possible. It will get clamped at the tile source
     * minimum zoom. The default `delta` is 4.
     */
    fun prefetchZoomDelta(value: Long = 4L) = apply {
      val propertyValue = PropertyValue("prefetch-zoom-delta", TypeUtils.wrapToValue(value))
      volatileProperties[propertyValue.propertyName] = propertyValue
    }

    /**
     * Add a Feature to the GeojsonSource.
     *
     * @param value the feature
     */
    fun feature(value: Feature) = apply {
      rawGeoJson = value
      val propertyValue = PropertyValue("data", "null")
      properties[propertyValue.propertyName] = propertyValue
    }

    /**
     * Add a FeatureCollection to the GeojsonSource.
     *
     * @param value the feature collection
     */
    fun featureCollection(value: FeatureCollection) = apply {
      rawGeoJson = value
      val propertyValue = PropertyValue("data", "null")
      properties[propertyValue.propertyName] = propertyValue
    }

    /**
     * Add a Geometry to the GeojsonSource.
     *
     * @param value the geometry
     */
    fun geometry(value: Geometry) = apply {
      rawGeoJson = value
      val propertyValue = PropertyValue("data", "null")
      properties[propertyValue.propertyName] = propertyValue
    }

    /**
     * Build the GeoJsonSource.
     *
     * @return the GeoJsonSource
     */
    fun build() = GeoJsonSource(this, rawGeoJson, onGeoJsonParsed)
  }

  /**
   * Static variables and methods.
   */
  companion object {
    /** A worker thread to parse large geojson data. */
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    var workerThread = HandlerThread("STYLE_WORKER").apply {
      priority = Thread.MAX_PRIORITY
      start()
    }

    private val mainHandler = Handler(Looper.getMainLooper())
    /** Registry to control if we need to ignore parsed result for given sourceId. */
    private val ignoreParsedGeoJsonRegistry = hashMapOf<String, Boolean>()

    /**
     * Maximum zoom level at which to create vector tiles (higher means greater detail at high zoom
     * levels).
     */
    val defaultMaxzoom: Long?
      /**
       * Get the Maxzoom property
       *
       * @return Long
       */
      get() = StyleManager.getStyleSourcePropertyDefaultValue("geojson", "maxzoom").silentUnwrap()

    /**
     * Size of the tile buffer on each side. A value of 0 produces no buffer. A
     * value of 512 produces a buffer as wide as the tile itself. Larger values produce fewer
     * rendering artifacts near tile edges and slower performance.
     */
    val defaultBuffer: Long?
      /**
       * Get the Buffer property
       *
       * @return Long
       */
      get() = StyleManager.getStyleSourcePropertyDefaultValue("geojson", "buffer").silentUnwrap()

    /**
     * Douglas-Peucker simplification tolerance (higher means simpler geometries and faster performance).
     */
    val defaultTolerance: Double?
      /**
       * Get the Tolerance property
       *
       * @return Double
       */
      get() = StyleManager.getStyleSourcePropertyDefaultValue("geojson", "tolerance").silentUnwrap()

    /**
     * If the data is a collection of point features, setting this to true clusters the points
     * by radius into groups. Cluster groups become new `Point` features in the source with additional properties:
     * - `cluster` Is `true` if the point is a cluster
     * - `cluster_id` A unqiue id for the cluster to be used in conjunction with the
     * [cluster inspection methods](https://www.mapbox.com/mapbox-gl-js/api/#geojsonsource#getclusterexpansionzoom)
     * - `point_count` Number of original points grouped into this cluster
     * - `point_count_abbreviated` An abbreviated point count
     */
    val defaultCluster: Boolean?
      /**
       * Get the Cluster property
       *
       * @return Boolean
       */
      get() = StyleManager.getStyleSourcePropertyDefaultValue("geojson", "cluster").silentUnwrap()

    /**
     * Radius of each cluster if clustering is enabled. A value of 512 indicates a radius equal
     * to the width of a tile.
     */
    val defaultClusterRadius: Long?
      /**
       * Get the ClusterRadius property
       *
       * @return Long
       */
      get() = StyleManager.getStyleSourcePropertyDefaultValue("geojson", "clusterRadius").silentUnwrap()

    /**
     * Max zoom on which to cluster points if clustering is enabled. Defaults to one zoom less
     * than maxzoom (so that last zoom features are not clustered). Clusters are re-evaluated at integer zoom
     * levels so setting clusterMaxZoom to 14 means the clusters will be displayed until z15.
     */
    val defaultClusterMaxZoom: Long?
      /**
       * Get the ClusterMaxZoom property
       *
       * @return Long
       */
      get() = StyleManager.getStyleSourcePropertyDefaultValue("geojson", "clusterMaxZoom").silentUnwrap()

    /**
     * Whether to calculate line distance metrics. This is required for line layers that specify `line-gradient` values.
     */
    val defaultLineMetrics: Boolean?
      /**
       * Get the LineMetrics property
       *
       * @return Boolean
       */
      get() = StyleManager.getStyleSourcePropertyDefaultValue("geojson", "lineMetrics").silentUnwrap()

    /**
     * Whether to generate ids for the geojson features. When enabled, the `feature.id` property will be auto
     * assigned based on its index in the `features` array, over-writing any previous values.
     */
    val defaultGenerateId: Boolean?
      /**
       * Get the GenerateId property
       *
       * @return Boolean
       */
      get() = StyleManager.getStyleSourcePropertyDefaultValue("geojson", "generateId").silentUnwrap()

    /**
     * When loading a map, if `PrefetchZoomDelta` is set to any number greater than 0, the map
     * will first request a tile for `zoom - delta` in a attempt to display a full
     * map at lower resolution as quick as possible. It will get clamped at the tile source
     * minimum zoom. The default `delta` is 4.
     */
    val defaultPrefetchZoomDelta: Long?
      /**
       * Get the PrefetchZoomDelta property
       *
       * @return Long
       */
      get() = StyleManager.getStyleSourcePropertyDefaultValue("geojson", "prefetch-zoom-delta").silentUnwrap()
  }
}

/**
 * DSL function for [GeoJsonSource] performing parsing using background thread.
 * Immediately returns [GeoJsonSource] with no data set and starts preparing actual data
 * using a worker thread.
 *
 * If using runtime styling:
 *
 * loadStyle(style(Style.DARK) {
 *   +geoJsonSource(id) {
 *    featureCollection(collection)
 *   }
 *   ...
 * }
 *
 * compositing style will be performed correctly under the hood and
 * [Style.OnStyleLoaded] will be emitted in correct moment of time when all sources are parsed.
 *
 * If creating geojson sources for already loaded Style please consider using overloaded
 * geoJsonSource(String, GeoJsonSource.Builder.() -> Unit, onGeoJsonParsed: (GeoJsonSource) -> Unit) function
 * and use fully prepared [GeoJsonSource] in onGeoJsonParsed callback.
 */
fun geoJsonSource(
  id: String,
  block: GeoJsonSource.Builder.() -> Unit
): GeoJsonSource = GeoJsonSource.Builder(id) {}.apply(block).build()

/**
 * DSL function for [GeoJsonSource] performing parsing using a worker thread.
 * Immediately returns [GeoJsonSource] with no data set,
 * fully parsed [GeoJsonSource] is returned in [onGeoJsonParsed] callback.
 *
 * Using this method means that it is user's responsibility to proceed with adding this source,
 * layers or other style objects in [onGeoJsonParsed] callback.
 */
fun geoJsonSource(
  id: String,
  config: GeoJsonSource.Builder.() -> Unit,
  onGeoJsonParsed: OnGeoJsonParsed
): GeoJsonSource = GeoJsonSource.Builder(id, onGeoJsonParsed).apply(config).build()

// End of generated file.