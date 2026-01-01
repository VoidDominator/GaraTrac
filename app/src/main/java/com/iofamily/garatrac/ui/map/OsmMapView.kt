package com.iofamily.garatrac.ui.map

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import com.iofamily.garatrac.data.TrackPoint

@Composable
fun OsmMapView(
    modifier: Modifier = Modifier,
    trackPoints: List<TrackPoint> = emptyList(),
    mapType: String = "MAPNIK",
    onMapReady: (MapView) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val tileSource = remember(mapType) {
        when (mapType) {
            "USGS_TOPO" -> TileSourceFactory.USGS_TOPO
            "USGS_SAT" -> TileSourceFactory.USGS_SAT
            else -> TileSourceFactory.MAPNIK
        }
    }

    // Initialize OSMDroid configuration
    androidx.compose.runtime.LaunchedEffect(Unit) {
        val osmConfig = Configuration.getInstance()
        osmConfig.load(
            context,
            context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
        )
        osmConfig.userAgentValue = context.packageName
        osmConfig.osmdroidBasePath = java.io.File(context.cacheDir, "osmdroid")
        osmConfig.osmdroidTileCache = java.io.File(osmConfig.osmdroidBasePath, "tiles")
    }

    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(16.0)
            controller.setCenter(GeoPoint(43.09848812701165, -79.0712233140023)) // I wonder where this is
        }
    }

    // Handle Lifecycle
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onDetach()
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier,
        update = { view ->
            if (view.tileProvider.tileSource != tileSource) {
                view.setTileSource(tileSource)
            }

            view.overlays.clear()

            if (trackPoints.isNotEmpty()) {
                val line = Polyline()
                line.setPoints(trackPoints.map { GeoPoint(it.latitude, it.longitude) })
                view.overlays.add(line)

                val latest = trackPoints.last()
                val marker = Marker(view)
                marker.position = GeoPoint(latest.latitude, latest.longitude)
                marker.title = "Latest Location"
                marker.snippet = latest.timestamp
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                view.overlays.add(marker)

                view.controller.animateTo(GeoPoint(latest.latitude, latest.longitude))
            }

            view.invalidate()
            onMapReady(view)
        }
    )
}
