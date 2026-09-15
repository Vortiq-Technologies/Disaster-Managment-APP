package com.example.ui.screens.map

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Satellite
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.R
import com.example.data.model.ZoneRiskData
import com.example.ui.theme.DarkGlassBorder
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.RiskCritical
import com.example.ui.theme.RiskNormal
import com.example.ui.theme.RiskWatch
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Real Map layer options for Northeast India
 */
enum class RealMapLayer(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val description: String
) {
    SATELLITE(
        id = "satellite",
        title = "Real Satellite",
        icon = Icons.Default.Satellite,
        description = "High-resolution real Earth imagery of Northeast India"
    ),
    TOPOGRAPHIC(
        id = "topo",
        title = "Topographic",
        icon = Icons.Default.Terrain,
        description = "Elevation contour lines, mountain ridges & hillshade"
    ),
    STREET_GIS(
        id = "street",
        title = "OSM Roads",
        icon = Icons.Default.Map,
        description = "OpenStreetMap road network, highways & rivers"
    ),
    TERRAIN_PHOTO(
        id = "photo",
        title = "Relief Photo",
        icon = Icons.Default.Layers,
        description = "Photorealistic Northeast satellite relief calibration"
    )
}

/**
 * Interactive Real Map of Northeast India
 * Supports real satellite tiles (ESRI World Imagery), Topographic contours (ESRI Topo),
 * OpenStreetMap road network, and photorealistic satellite relief asset with exact GPS coordinates.
 */
@Composable
fun RealNorthEastMapView(
    zones: List<ZoneRiskData>,
    selectedZoneId: String,
    currentLayer: RealMapLayer,
    moistureOverlayEnabled: Boolean,
    onSelectZone: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentOnSelectZone = rememberUpdatedState(onSelectZone)
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var isMapEngineLoaded by remember { mutableStateOf(false) }

    // When selected zone changes, fly real map camera to exact coordinates
    val targetZone = remember(zones, selectedZoneId) {
        zones.find { it.id == selectedZoneId }
    }

    LaunchedEffect(targetZone, isMapEngineLoaded, webViewInstance) {
        val webView = webViewInstance
        if (targetZone != null && isMapEngineLoaded && webView != null) {
            val js = String.format(
                Locale.US,
                "javascript:if(window.flyToZone){ window.flyToZone('%s', %.6f, %.6f, 13); }",
                targetZone.id,
                targetZone.latitude,
                targetZone.longitude
            )
            webView.evaluateJavascript(js, null)
        }
    }

    // When layer changes, inform map engine
    LaunchedEffect(currentLayer, isMapEngineLoaded, webViewInstance) {
        val webView = webViewInstance
        if (isMapEngineLoaded && webView != null) {
            webView.evaluateJavascript(
                "javascript:if(window.setMapLayer){ window.setMapLayer('${currentLayer.id}'); }",
                null
            )
        }
    }

    // When moisture overlay changes, toggle in map engine
    LaunchedEffect(moistureOverlayEnabled, isMapEngineLoaded, webViewInstance) {
        val webView = webViewInstance
        if (isMapEngineLoaded && webView != null) {
            webView.evaluateJavascript(
                "javascript:if(window.setMoistureOverlay){ window.setMoistureOverlay($moistureOverlayEnabled); }",
                null
            )
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (currentLayer == RealMapLayer.TERRAIN_PHOTO) {
            // Photorealistic satellite relief view using generated high-res GIS asset
            PhotorealisticTerrainView(
                zones = zones,
                selectedZoneId = selectedZoneId,
                moistureOverlayEnabled = moistureOverlayEnabled,
                onSelectZone = onSelectZone
            )
        } else {
            // Live Interactive GIS Map with Real Map Tiles
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    createRealMapView(
                        context = context,
                        zones = zones,
                        initialLayer = currentLayer.id,
                        initialMoisture = moistureOverlayEnabled,
                        initialZoneId = selectedZoneId,
                        onZoneClicked = { zoneId ->
                            currentOnSelectZone.value(zoneId)
                        },
                        onMapLoaded = {
                            isMapEngineLoaded = true
                        }
                    ).also { webViewInstance = it }
                },
                update = { webView ->
                    webViewInstance = webView
                }
            )

            // Loading indicator while initial satellite tiles stream in
            AnimatedVisibility(
                visible = !isMapEngineLoaded,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.Center)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xF208150F),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkGlassBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = EmeraldAccent,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Loading Real Satellite Imagery",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Northeast India GIS Grid (ESRI & OSM)",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }

        // Floating On-Screen Map Zoom & Reset Controls
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Zoom In (+)
            Surface(
                onClick = {
                    webViewInstance?.evaluateJavascript("javascript:if(window.mapZoomIn){ window.mapZoomIn(); }", null)
                },
                shape = CircleShape,
                color = Color(0xE60A1911),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkGlassBorder),
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Zoom In",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Zoom Out (-)
            Surface(
                onClick = {
                    webViewInstance?.evaluateJavascript("javascript:if(window.mapZoomOut){ window.mapZoomOut(); }", null)
                },
                shape = CircleShape,
                color = Color(0xE60A1911),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkGlassBorder),
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Zoom Out",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Reset Northeast Overview
            Surface(
                onClick = {
                    webViewInstance?.evaluateJavascript("javascript:if(window.resetMapToOverview){ window.resetMapToOverview(); }", null)
                },
                shape = CircleShape,
                color = Color(0xE60A1911),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkGlassBorder),
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "Northeast Overview",
                        tint = EmeraldAccent,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

/**
 * Photorealistic Northeast India satellite relief view using the generated GIS satellite asset
 */
@Composable
private fun PhotorealisticTerrainView(
    zones: List<ZoneRiskData>,
    selectedZoneId: String,
    moistureOverlayEnabled: Boolean,
    onSelectZone: (String) -> Unit
) {
    var scale by remember { mutableFloatStateOf(1.0f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    // Geographic bounding box for Northeast India
    val minLat = 22.0
    val maxLat = 29.2
    val minLon = 87.8
    val maxLon = 97.2

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF07120C))
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(0.9f, 3.8f)
                    val maxOffset = 600f * scale
                    offsetX = (offsetX + pan.x).coerceIn(-maxOffset, maxOffset)
                    offsetY = (offsetY + pan.y).coerceIn(-maxOffset, maxOffset)
                }
            }
    ) {
        val width = constraints.maxWidth.toFloat()
        val height = constraints.maxHeight.toFloat()

        // 1. High-Detail Satellite Photo Asset
        Image(
            painter = painterResource(id = R.drawable.img_ne_india_satellite_map),
            contentDescription = "Northeast India Satellite Topography",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
        )

        // 2. Moisture / Precipitation Radar Gradient Tint
        if (moistureOverlayEnabled) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x3306B6D4))
            )
        }

        // 3. Real Location Pins & Danger Perimeters
        zones.forEach { zone ->
            val normX = ((zone.longitude - minLon) / (maxLon - minLon)).toFloat().coerceIn(0f, 1f)
            val normY = (1.0f - ((zone.latitude - minLat) / (maxLat - minLat))).toFloat().coerceIn(0f, 1f)

            val baseX = normX * width * 0.84f + width * 0.08f
            val baseY = normY * height * 0.74f + height * 0.13f

            val pinX = (baseX - width / 2f) * scale + width / 2f + offsetX
            val pinY = (baseY - height / 2f) * scale + height / 2f + offsetY

            val isSelected = zone.id == selectedZoneId
            val pinColor = when {
                zone.riskScore >= 80 -> RiskCritical
                zone.riskScore >= 60 -> RiskWatch
                else -> RiskNormal
            }

            if (pinX in -80f..(width + 80f) && pinY in -40f..(height + 40f)) {
                // Interactive Marker Tag
                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                (pinX - 16f).roundToInt(),
                                (pinY - 16f).roundToInt()
                            )
                        }
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) Color(0xF2003822) else Color(0xD90A1911))
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) EmeraldAccent else pinColor.copy(alpha = 0.8f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { onSelectZone(zone.id) }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(pinColor)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "${zone.name.substringBefore(" (")} (${zone.riskScore}%)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else Color(0xFFE2E8F0)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Creates and configures the WebView running Leaflet with real map tiles
 */
@SuppressLint("SetJavaScriptEnabled")
private fun createRealMapView(
    context: Context,
    zones: List<ZoneRiskData>,
    initialLayer: String,
    initialMoisture: Boolean,
    initialZoneId: String,
    onZoneClicked: (String) -> Unit,
    onMapLoaded: () -> Unit
): WebView {
    val mainHandler = Handler(Looper.getMainLooper())

    return WebView(context).apply {
        settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            useWideViewPort = true
            loadWithOverviewMode = true
            setSupportZoom(true)
            builtInZoomControls = false
            displayZoomControls = false
            cacheMode = WebSettings.LOAD_DEFAULT
        }

        // Bridge to receive clicks from Javascript Leaflet markers
        addJavascriptInterface(object {
            @JavascriptInterface
            fun onZoneClickedFromJs(zoneId: String) {
                mainHandler.post {
                    onZoneClicked(zoneId)
                }
            }

            @JavascriptInterface
            fun notifyMapReady() {
                mainHandler.post {
                    onMapLoaded()
                }
            }
        }, "AndroidBridge")

        webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                onMapLoaded()
            }
        }

        val html = buildLeafletMapHtml(
            zones = zones,
            initialLayer = initialLayer,
            initialMoisture = initialMoisture,
            initialZoneId = initialZoneId
        )

        loadDataWithBaseURL("https://leafletjs.com", html, "text/html", "UTF-8", null)
    }
}

/**
 * Builds HTML and JavaScript for Leaflet map with real tile sources and real GPS coordinates
 */
private fun buildLeafletMapHtml(
    zones: List<ZoneRiskData>,
    initialLayer: String,
    initialMoisture: Boolean,
    initialZoneId: String
): String {
    val zonesJsonArray = JSONArray()
    zones.forEach { z ->
        val obj = JSONObject()
        obj.put("id", z.id)
        obj.put("name", z.name)
        obj.put("district", z.district)
        obj.put("state", z.state)
        obj.put("lat", z.latitude)
        obj.put("lon", z.longitude)
        obj.put("score", z.riskScore)
        obj.put("rainfall", z.rainfall24hMm.toDouble())
        obj.put("moisture", z.soilMoisturePercent)
        obj.put("slopeMovement", z.slopeMovementMm.toDouble())
        obj.put("sensors", z.activeSensorsCount)
        zonesJsonArray.put(obj)
    }

    return """
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
    <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
    <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        html, body, #map { width: 100%; height: 100%; background: #07120C; font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif; overflow: hidden; }
        
        /* Dark Glass Popup styling */
        .leaflet-popup-content-wrapper {
            background: rgba(10, 25, 17, 0.95) !important;
            border: 1px solid rgba(0, 230, 153, 0.4) !important;
            border-radius: 12px !important;
            color: #FFFFFF !important;
            box-shadow: 0 8px 32px rgba(0, 0, 0, 0.6) !important;
            padding: 2px !important;
        }
        .leaflet-popup-tip {
            background: rgba(10, 25, 17, 0.95) !important;
        }
        .leaflet-popup-content {
            margin: 10px 14px !important;
            line-height: 1.4 !important;
        }
        .popup-title {
            font-size: 14px;
            font-weight: 700;
            color: #FFFFFF;
            margin-bottom: 2px;
        }
        .popup-sub {
            font-size: 11px;
            color: rgba(255, 255, 255, 0.7);
            margin-bottom: 8px;
        }
        .popup-badge {
            display: inline-block;
            padding: 2px 8px;
            border-radius: 6px;
            font-size: 11px;
            font-weight: 700;
            margin-bottom: 8px;
        }
        .badge-critical { background: rgba(239, 68, 68, 0.2); color: #EF4444; border: 1px solid #EF4444; }
        .badge-watch { background: rgba(245, 158, 11, 0.2); color: #F59E0B; border: 1px solid #F59E0B; }
        .badge-normal { background: rgba(16, 185, 129, 0.2); color: #10B981; border: 1px solid #10B981; }
        .popup-grid {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 6px;
            font-size: 10px;
            color: rgba(255, 255, 255, 0.85);
            margin-bottom: 10px;
            background: rgba(0,0,0,0.25);
            padding: 6px;
            border-radius: 6px;
        }
        .popup-btn {
            display: block;
            width: 100%;
            background: #00E699;
            color: #003822;
            text-align: center;
            padding: 6px;
            font-size: 11px;
            font-weight: 700;
            border-radius: 8px;
            text-decoration: none;
            cursor: pointer;
            border: none;
        }

        /* Pulsing Radar Marker */
        .pulse-pin {
            position: relative;
            width: 28px;
            height: 28px;
        }
        .pulse-dot {
            position: absolute;
            top: 50%;
            left: 50%;
            transform: translate(-50%, -50%);
            width: 14px;
            height: 14px;
            border-radius: 50%;
            border: 2px solid #FFFFFF;
            box-shadow: 0 0 10px currentColor;
        }
        .pulse-ring {
            position: absolute;
            top: 0;
            left: 0;
            width: 28px;
            height: 28px;
            border-radius: 50%;
            animation: radarPulse 2s infinite ease-out;
            opacity: 0.8;
            border: 2px solid currentColor;
        }
        .pulse-label {
            position: absolute;
            left: 32px;
            top: 4px;
            white-space: nowrap;
            background: rgba(7, 18, 12, 0.9);
            color: #FFFFFF;
            border: 1px solid rgba(255, 255, 255, 0.3);
            border-radius: 6px;
            padding: 2px 6px;
            font-size: 10px;
            font-weight: 700;
            box-shadow: 0 2px 6px rgba(0,0,0,0.5);
            pointer-events: none;
        }
        @keyframes radarPulse {
            0% { transform: scale(0.6); opacity: 0.9; }
            100% { transform: scale(1.6); opacity: 0; }
        }

        /* Hide default Leaflet controls to keep a clean HUD */
        .leaflet-control-zoom { display: none !important; }
        .leaflet-control-attribution {
            background: rgba(7, 18, 12, 0.75) !important;
            color: rgba(255, 255, 255, 0.6) !important;
            font-size: 9px !important;
        }
        .leaflet-control-attribution a {
            color: #00E699 !important;
        }
    </style>
</head>
<body>
    <div id="map"></div>

    <script>
        var zones = $zonesJsonArray;
        var initialLayerName = '$initialLayer';
        var initialZoneId = '$initialZoneId';
        var markers = {};
        var circles = {};

        // Real Tile Providers for Northeast India
        // 1. ESRI World Imagery (Real High-Resolution Satellite)
        var satelliteLayer = L.tileLayer('https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}', {
            maxZoom: 18,
            attribution: 'Tiles &copy; Esri &mdash; Source: Esri, i-cubed, USDA, USGS, AEX, GeoEye, Getmapping, Aerogrid, IGN, IGP, UPR-EGP, and the GIS User Community'
        });

        // 2. ESRI World Topographic Map (Real Elevation Contours & Hillshading)
        var topoLayer = L.tileLayer('https://server.arcgisonline.com/ArcGIS/rest/services/World_Topo_Map/MapServer/tile/{z}/{y}/{x}', {
            maxZoom: 18,
            attribution: 'Tiles &copy; Esri, DeLorme, NAVTEQ, TomTom, Intermap, iPC, USGS, FAO, NPS, NRCAN, GeoBase'
        });

        // 3. OpenStreetMap Roads & Highways (Lifelines: NH-10, NH-6, NH-29)
        var streetLayer = L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
            maxZoom: 19,
            attribution: '&copy; OpenStreetMap contributors'
        });

        // Initialize map centered at Northeast India (between Sikkim, Assam & Meghalaya)
        var map = L.map('map', {
            center: [25.85, 92.40],
            zoom: 8,
            minZoom: 6,
            maxZoom: 17,
            zoomControl: false,
            layers: [initialLayerName === 'topo' ? topoLayer : (initialLayerName === 'street' ? streetLayer : satelliteLayer)]
        });

        var currentLayer = initialLayerName === 'topo' ? topoLayer : (initialLayerName === 'street' ? streetLayer : satelliteLayer);

        // Moisture Overlay Layer Group (simulating Doppler radar precipitation over wettest ridges)
        var moistureLayerGroup = L.layerGroup();
        // High precipitation heat zones (Cherrapunji, Mawsynram, Sikkim slope)
        var rainZones = [
            { lat: 25.2986, lon: 91.5822, radius: 24000, color: '#06B6D4' },
            { lat: 25.5788, lon: 91.8933, radius: 18000, color: '#0EA5E9' },
            { lat: 27.3389, lon: 88.6065, radius: 20000, color: '#3B82F6' },
            { lat: 23.7271, lon: 92.7176, radius: 16000, color: '#06B6D4' }
        ];
        rainZones.forEach(function(rz) {
            L.circle([rz.lat, rz.lon], {
                radius: rz.radius,
                color: rz.color,
                fillColor: rz.color,
                fillOpacity: 0.28,
                weight: 1.5,
                dashArray: '4, 8'
            }).addTo(moistureLayerGroup);
        });

        if ($initialMoisture) {
            moistureLayerGroup.addTo(map);
        }

        // Draw Lifeline Corridors (Real NH Corridors connecting the North-East)
        // NH-10 Siliguri to Gangtok
        var nh10Coords = [[26.7271, 88.3953], [26.8500, 88.4200], [27.0500, 88.4700], [27.1800, 88.5100], [27.3389, 88.6065]];
        L.polyline(nh10Coords, { color: '#F59E0B', weight: 2.5, opacity: 0.7, dashArray: '6, 6' }).addTo(map);

        // NH-6 Guwahati to Shillong to Cherrapunji
        var nh6Coords = [[26.1445, 91.7362], [25.9000, 91.8000], [25.7000, 91.8600], [25.5788, 91.8933], [25.2986, 91.5822]];
        L.polyline(nh6Coords, { color: '#EF4444', weight: 2.5, opacity: 0.7, dashArray: '6, 6' }).addTo(map);

        // NH-29 Guwahati to Kohima to Imphal
        var nh29Coords = [[26.1445, 91.7362], [26.1500, 92.8000], [25.9000, 93.7000], [25.6701, 94.1077], [24.8170, 93.9368]];
        L.polyline(nh29Coords, { color: '#00E699', weight: 2.5, opacity: 0.6, dashArray: '6, 6' }).addTo(map);

        // Add Real Pins and Hazard Radius for all zones
        zones.forEach(function(zone) {
            var color = '#10B981';
            var badgeClass = 'badge-normal';
            var badgeText = 'MODERATE ' + zone.score + '%';
            if (zone.score >= 80) {
                color = '#EF4444';
                badgeClass = 'badge-critical';
                badgeText = 'CRITICAL ' + zone.score + '%';
            } else if (zone.score >= 60) {
                color = '#F59E0B';
                badgeClass = 'badge-watch';
                badgeText = 'WATCH ' + zone.score + '%';
            }

            // Real Geodesic Hazard Radius (6,000 meters buffer)
            var dangerCircle = L.circle([zone.lat, zone.lon], {
                radius: zone.score >= 80 ? 7500 : 5000,
                color: color,
                fillColor: color,
                fillOpacity: 0.16,
                weight: 1.8,
                dashArray: '5, 5'
            }).addTo(map);
            circles[zone.id] = dangerCircle;

            // Custom Pulsing Beacon Icon
            var shortName = zone.name.split(' (')[0].substring(0, 14);
            var pinHtml = '<div class="pulse-pin" style="color: ' + color + ';">' +
                '<div class="pulse-ring"></div>' +
                '<div class="pulse-dot" style="background: ' + color + ';"></div>' +
                '<div class="pulse-label">' + shortName + '</div>' +
                '</div>';

            var customIcon = L.divIcon({
                className: '',
                html: pinHtml,
                iconSize: [28, 28],
                iconAnchor: [14, 14]
            });

            var marker = L.marker([zone.lat, zone.lon], { icon: customIcon }).addTo(map);

            var popupContent = '' +
                '<div class="popup-title">' + zone.name + '</div>' +
                '<div class="popup-sub">' + zone.district + ', ' + zone.state + ' • ' + zone.lat.toFixed(4) + '°N, ' + zone.lon.toFixed(4) + '°E</div>' +
                '<div class="popup-badge ' + badgeClass + '">' + badgeText + '</div>' +
                '<div class="popup-grid">' +
                    '<div>🌧️ 24h Rain: <b>' + zone.rainfall.toFixed(1) + ' mm</b></div>' +
                    '<div>💧 Soil Sat: <b>' + zone.moisture + '%</b></div>' +
                    '<div>⛰️ Slip Rate: <b>' + zone.slopeMovement.toFixed(1) + ' mm/d</b></div>' +
                    '<div>📡 Sensors: <b>' + zone.sensors + ' Active</b></div>' +
                '</div>' +
                '<button class="popup-btn" onclick="triggerZoneSelect(\'' + zone.id + '\')">Inspect Live Telemetry</button>';

            marker.bindPopup(popupContent);

            marker.on('click', function() {
                triggerZoneSelect(zone.id);
            });

            markers[zone.id] = marker;
        });

        function triggerZoneSelect(zoneId) {
            if (window.AndroidBridge && window.AndroidBridge.onZoneClickedFromJs) {
                window.AndroidBridge.onZoneClickedFromJs(zoneId);
            }
        }

        // Global functions called from Kotlin
        window.flyToZone = function(zoneId, lat, lon, zoom) {
            map.flyTo([lat, lon], zoom || 13, {
                animate: true,
                duration: 1.2
            });
            if (markers[zoneId]) {
                setTimeout(function() {
                    markers[zoneId].openPopup();
                }, 400);
            }
        };

        window.setMapLayer = function(layerName) {
            if (currentLayer) map.removeLayer(currentLayer);
            if (layerName === 'topo') {
                currentLayer = topoLayer;
            } else if (layerName === 'street') {
                currentLayer = streetLayer;
            } else {
                currentLayer = satelliteLayer;
            }
            currentLayer.addTo(map);
        };

        window.setMoistureOverlay = function(enabled) {
            if (enabled) {
                if (!map.hasLayer(moistureLayerGroup)) {
                    moistureLayerGroup.addTo(map);
                }
            } else {
                if (map.hasLayer(moistureLayerGroup)) {
                    map.removeLayer(moistureLayerGroup);
                }
            }
        };

        window.mapZoomIn = function() {
            map.zoomIn();
        };

        window.mapZoomOut = function() {
            map.zoomOut();
        };

        window.resetMapToOverview = function() {
            map.flyTo([25.85, 92.40], 8, { animate: true, duration: 1.0 });
        };

        // Notify Android that map is ready
        setTimeout(function() {
            if (window.AndroidBridge && window.AndroidBridge.notifyMapReady) {
                window.AndroidBridge.notifyMapReady();
            }
            if (initialZoneId && markers[initialZoneId]) {
                // Focus on initial zone
                var target = zones.find(function(z) { return z.id === initialZoneId; });
                if (target) {
                    window.flyToZone(initialZoneId, target.lat, target.lon, 12);
                }
            }
        }, 500);
    </script>
</body>
</html>
    """.trimIndent()
}
