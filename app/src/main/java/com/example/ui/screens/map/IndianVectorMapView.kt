package com.example.ui.screens.map

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.ZoneRiskData
import com.example.ui.theme.DarkGlassBorder
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.RiskCritical
import com.example.ui.theme.RiskNormal
import com.example.ui.theme.RiskWatch
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * Geographic GPS coordinate data point
 */
data class GeoPoint(val lat: Double, val lon: Double)

/**
 * State boundary vector representation
 */
data class StateBoundary(
    val name: String,
    val shortCode: String,
    val centerLat: Double,
    val centerLon: Double,
    val polygon: List<GeoPoint>,
    val strokeColor: Color,
    val fillColor: Color
)

/**
 * Indian Vector Map Engine
 * Renders precise, guaranteed-visible outlines of India and the Northeastern States
 * (Sikkim, Assam, Meghalaya, Arunachal Pradesh, Nagaland, Manipur, Mizoram, Tripura).
 * Completely self-contained with zero external network or CDN dependencies.
 */
@Composable
fun IndianVectorMapView(
    zones: List<ZoneRiskData>,
    selectedZoneId: String,
    currentLayer: RealMapLayer,
    moistureOverlayEnabled: Boolean,
    onSelectZone: (String) -> Unit,
    modifier: Modifier = Modifier,
    mapController: RealMapController? = null
) {
    val coroutineScope = rememberCoroutineScope()
    val scaleAnim = remember { Animatable(1.15f) }
    val offsetXAnim = remember { Animatable(0f) }
    val offsetYAnim = remember { Animatable(0f) }

    // Geographic viewport bounds for Northeast India
    val minLat = 21.6
    val maxLat = 29.6
    val minLon = 87.6
    val maxLon = 97.4

    // Radar pulse animation for coordinate beacon pins
    val infiniteTransition = rememberInfiniteTransition(label = "vectorRadar")
    val pulseRingFraction by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseRingFraction"
    )
    val radarSweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radarSweepAngle"
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF06110B))
    ) {
        val width = constraints.maxWidth.toFloat()
        val height = constraints.maxHeight.toFloat()

        // Projection: Latitude / Longitude to local screen coordinates
        fun geoToLocal(lat: Double, lon: Double): Offset {
            val normX = ((lon - minLon) / (maxLon - minLon)).toFloat().coerceIn(0f, 1f)
            val normY = (1.0f - ((lat - minLat) / (maxLat - minLat))).toFloat().coerceIn(0f, 1f)
            val baseX = normX * width * 0.86f + width * 0.07f
            val baseY = normY * height * 0.74f + height * 0.13f
            return Offset(baseX, baseY)
        }

        // Camera flight helper
        fun flyToCoords(lat: Double, lon: Double, targetZoom: Float = 2.2f) {
            val targetBase = geoToLocal(lat, lon)
            val targetPanX = -(targetBase.x - width / 2f) * targetZoom
            val targetPanY = -(targetBase.y - height / 2f) * targetZoom
            val maxOffset = 850f * targetZoom

            coroutineScope.launch {
                scaleAnim.animateTo(targetZoom.coerceIn(0.9f, 4.2f), tween(420, easing = FastOutSlowInEasing))
            }
            coroutineScope.launch {
                offsetXAnim.animateTo(targetPanX.coerceIn(-maxOffset, maxOffset), tween(420, easing = FastOutSlowInEasing))
            }
            coroutineScope.launch {
                offsetYAnim.animateTo(targetPanY.coerceIn(-maxOffset, maxOffset), tween(420, easing = FastOutSlowInEasing))
            }
        }

        // Bind controller actions to animated camera states
        DisposableEffect(mapController, width, height) {
            if (mapController != null) {
                mapController.zoomIn = {
                    coroutineScope.launch {
                        scaleAnim.animateTo((scaleAnim.value * 1.35f).coerceAtMost(4.2f), tween(240))
                    }
                }
                mapController.zoomOut = {
                    coroutineScope.launch {
                        scaleAnim.animateTo((scaleAnim.value / 1.35f).coerceAtLeast(0.9f), tween(240))
                    }
                }
                mapController.resetOverview = {
                    coroutineScope.launch {
                        scaleAnim.animateTo(1.15f, tween(380))
                        offsetXAnim.animateTo(0f, tween(380))
                        offsetYAnim.animateTo(0f, tween(380))
                    }
                }
                mapController.flyTo = { _, lat, lon, zoom ->
                    flyToCoords(lat, lon, zoom)
                }
            }
            onDispose { }
        }

        // Fly camera when selected zone changes externally
        LaunchedEffect(selectedZoneId) {
            val zone = zones.find { it.id == selectedZoneId }
            if (zone != null) {
                flyToCoords(zone.latitude, zone.longitude, 2.3f)
            }
        }

        val currentScale = scaleAnim.value
        val currentOffsetX = offsetXAnim.value
        val currentOffsetY = offsetYAnim.value

        // Real Geographic Vector Boundaries for Northeast India
        val neStates = remember { getNorthEastStateBoundaries() }
        val indiaOutline = remember { getIndiaSubcontinentOutline() }
        val riverBrahmaputra = remember { getBrahmaputraRiverPath() }
        val nh10Highway = remember { getNH10Corridor() }
        val nh6Highway = remember { getNH6Corridor() }
        val nh29Highway = remember { getNH29Corridor() }

        // Interactive Box with gestures
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        coroutineScope.launch {
                            val newScale = (scaleAnim.value * zoom).coerceIn(0.9f, 4.2f)
                            scaleAnim.snapTo(newScale)
                            val maxOffset = 850f * newScale
                            offsetXAnim.snapTo((offsetXAnim.value + pan.x).coerceIn(-maxOffset, maxOffset))
                            offsetYAnim.snapTo((offsetYAnim.value + pan.y).coerceIn(-maxOffset, maxOffset))
                        }
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures { tapOffset ->
                        // Detect tap near any coordinate pin
                        var nearestZone: ZoneRiskData? = null
                        var minDistance = Float.MAX_VALUE
                        zones.forEach { zone ->
                            val base = geoToLocal(zone.latitude, zone.longitude)
                            val pinX = (base.x - width / 2f) * currentScale + width / 2f + currentOffsetX
                            val pinY = (base.y - height / 2f) * currentScale + height / 2f + currentOffsetY
                            val dx = tapOffset.x - pinX
                            val dy = tapOffset.y - pinY
                            val dist = sqrt(dx * dx + dy * dy)
                            if (dist < 64f && dist < minDistance) {
                                minDistance = dist
                                nearestZone = zone
                            }
                        }
                        if (nearestZone != null) {
                            onSelectZone(nearestZone!!.id)
                        }
                    }
                }
        ) {
            // 1. Satellite Base Layer (When in Satellite Mode)
            if (currentLayer == RealMapLayer.SATELLITE || currentLayer == RealMapLayer.TERRAIN_PHOTO) {
                Image(
                    painter = painterResource(id = R.drawable.img_ne_india_satellite_map),
                    contentDescription = "Northeast India Satellite Base",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = currentScale
                            scaleY = currentScale
                            translationX = currentOffsetX
                            translationY = currentOffsetY
                        }
                )
            }

            // 2. Vector Canvas Layer: Indian Outlines, State Borders, Rivers, and Highways
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = currentScale
                        scaleY = currentScale
                        translationX = currentOffsetX
                        translationY = currentOffsetY
                    }
            ) {
                // Background GIS Grid & Meridians
                drawGisGridLines(width, height, minLat, maxLat, minLon, maxLon)

                // Draw Surrounding India & International Outlines
                val subconPath = Path()
                indiaOutline.forEachIndexed { i, pt ->
                    val c = geoToLocal(pt.lat, pt.lon)
                    if (i == 0) subconPath.moveTo(c.x, c.y) else subconPath.lineTo(c.x, c.y)
                }
                drawPath(
                    subconPath,
                    color = Color(0x5510B981),
                    style = Stroke(width = 2.0f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f))
                )

                // Draw Topographic Contours (in Topo Mode)
                if (currentLayer == RealMapLayer.TOPOGRAPHIC) {
                    drawTopographicContours(width, height)
                }

                // Draw Each Northeast State Polygon (Accurate Outlines)
                neStates.forEach { state ->
                    val statePath = Path()
                    state.polygon.forEachIndexed { i, pt ->
                        val c = geoToLocal(pt.lat, pt.lon)
                        if (i == 0) statePath.moveTo(c.x, c.y) else statePath.lineTo(c.x, c.y)
                    }
                    statePath.close()

                    // Polygon tint
                    val isSatMode = currentLayer == RealMapLayer.SATELLITE || currentLayer == RealMapLayer.TERRAIN_PHOTO
                    val polyFillColor = if (isSatMode) state.fillColor.copy(alpha = 0.08f) else state.fillColor
                    drawPath(statePath, color = polyFillColor, style = Fill)

                    // Crisp Vector Boundary Outline
                    drawPath(
                        statePath,
                        color = state.strokeColor,
                        style = Stroke(width = if (isSatMode) 2.5f else 2.0f, cap = StrokeCap.Round)
                    )
                }

                // Draw River Brahmaputra (Cyan Lifeline)
                val riverPath = Path()
                riverBrahmaputra.forEachIndexed { i, pt ->
                    val c = geoToLocal(pt.lat, pt.lon)
                    if (i == 0) riverPath.moveTo(c.x, c.y) else riverPath.lineTo(c.x, c.y)
                }
                // River glowing aura and primary stream
                drawPath(riverPath, color = Color(0x4406B6D4), style = Stroke(width = 7.0f, cap = StrokeCap.Round))
                drawPath(riverPath, color = Color(0xEE38BDF8), style = Stroke(width = 3.0f, cap = StrokeCap.Round))

                // Draw Strategic Lifeline Highways (NH-10, NH-6, NH-29)
                drawHighway(nh10Highway, Color(0xFFF59E0B), "NH-10", ::geoToLocal)
                drawHighway(nh6Highway, Color(0xFFEF4444), "NH-6", ::geoToLocal)
                drawHighway(nh29Highway, Color(0xFF00E699), "NH-29", ::geoToLocal)
            }

            // 3. Dynamic Doppler Moisture Scan Overlay
            if (moistureOverlayEnabled) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Color(0x5506B6D4), Color(0x333B82F6), Color.Transparent),
                                radius = width * 0.75f
                            )
                        )
                )
            }

            // 4. State Territory Watermark Labels
            neStates.forEach { state ->
                val centerBase = geoToLocal(state.centerLat, state.centerLon)
                val labelX = (centerBase.x - width / 2f) * currentScale + width / 2f + currentOffsetX
                val labelY = (centerBase.y - height / 2f) * currentScale + height / 2f + currentOffsetY

                if (labelX in -100f..(width + 100f) && labelY in -40f..(height + 40f)) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xCC091811),
                        border = androidx.compose.foundation.BorderStroke(1.dp, state.strokeColor.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .offset { IntOffset((labelX - 35f).roundToInt(), (labelY - 12f).roundToInt()) }
                    ) {
                        Text(
                            text = state.name.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // 5. Real Location Pins & Danger Perimeters
            zones.forEach { zone ->
                val base = geoToLocal(zone.latitude, zone.longitude)
                val pinX = (base.x - width / 2f) * currentScale + width / 2f + currentOffsetX
                val pinY = (base.y - height / 2f) * currentScale + height / 2f + currentOffsetY

                val isSelected = zone.id == selectedZoneId
                val pinColor = when {
                    zone.riskScore >= 80 -> RiskCritical
                    zone.riskScore >= 60 -> RiskWatch
                    else -> RiskNormal
                }

                if (pinX in -120f..(width + 120f) && pinY in -60f..(height + 60f)) {
                    Box(
                        modifier = Modifier
                            .offset {
                                IntOffset(
                                    (pinX - 24f).roundToInt(),
                                    (pinY - 24f).roundToInt()
                                )
                            }
                    ) {
                        // Pulsing Beacon Waves
                        Canvas(modifier = Modifier.size(48.dp)) {
                            drawCircle(
                                color = pinColor.copy(alpha = 0.35f),
                                radius = 22f * pulseRingFraction,
                                style = Stroke(width = 2.5f)
                            )
                            if (isSelected || zone.riskScore >= 80) {
                                drawCircle(
                                    color = pinColor.copy(alpha = 0.2f),
                                    radius = 34f,
                                    style = Stroke(width = 1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f))
                                )
                            }
                        }

                        // Interactive High-Contrast Tag
                        Surface(
                            onClick = { onSelectZone(zone.id) },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) Color(0xF2003822) else Color(0xE6091811),
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) EmeraldAccent else pinColor.copy(alpha = 0.85f)
                            ),
                            modifier = Modifier
                                .offset(x = 12.dp, y = (-8).dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(9.dp)
                                        .clip(CircleShape)
                                        .background(pinColor)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = zone.name.substringBefore(" ("),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "${zone.state.substringBefore(",")} • ${zone.riskScore}%",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = pinColor
                                        )
                                        if (zone.rainfallLastHourMm > 15f) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "🌧️ ${zone.rainfallLastHourMm.toInt()}mm/h",
                                                fontSize = 9.sp,
                                                color = Color(0xFF38BDF8)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Helper to draw a highway with label
 */
private fun DrawScope.drawHighway(
    coords: List<GeoPoint>,
    color: Color,
    label: String,
    toLocal: (Double, Double) -> Offset
) {
    if (coords.isEmpty()) return
    val path = Path()
    coords.forEachIndexed { i, pt ->
        val c = toLocal(pt.lat, pt.lon)
        if (i == 0) path.moveTo(c.x, c.y) else path.lineTo(c.x, c.y)
    }
    drawPath(
        path,
        color = color,
        style = Stroke(width = 3.0f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f))
    )
}

/**
 * Draws background GIS grid lines
 */
private fun DrawScope.drawGisGridLines(
    width: Float,
    height: Float,
    minLat: Double,
    maxLat: Double,
    minLon: Double,
    maxLon: Double
) {
    val gridColor = Color(0x18FFFFFF)
    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 8f), 0f)

    // Latitudes (every 2 degrees)
    var lat = 22.0
    while (lat <= 29.0) {
        val ny = (1.0f - ((lat - minLat) / (maxLat - minLat))).toFloat()
        val y = ny * height * 0.74f + height * 0.13f
        drawLine(gridColor, Offset(0f, y), Offset(width, y), strokeWidth = 1f, pathEffect = dashEffect)
        lat += 2.0
    }

    // Longitudes (every 2 degrees)
    var lon = 88.0
    while (lon <= 96.0) {
        val nx = ((lon - minLon) / (maxLon - minLon)).toFloat()
        val x = nx * width * 0.86f + width * 0.07f
        drawLine(gridColor, Offset(x, 0f), Offset(x, height), strokeWidth = 1f, pathEffect = dashEffect)
        lon += 2.0
    }
}

/**
 * Draws topographic contour bands
 */
private fun DrawScope.drawTopographicContours(width: Float, height: Float) {
    val contourColor = Color(0x2210B981)
    val style = Stroke(width = 1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f))

    // Eastern Himalayas ridge line arc
    val himalayaArc = Path().apply {
        moveTo(width * 0.10f, height * 0.32f)
        quadraticTo(width * 0.40f, height * 0.20f, width * 0.85f, height * 0.25f)
    }
    drawPath(himalayaArc, color = Color(0x66FFFFFF), style = Stroke(width = 2f))

    // Meghalaya plateau ridge
    val shillongRidge = Path().apply {
        moveTo(width * 0.22f, height * 0.62f)
        lineTo(width * 0.52f, height * 0.60f)
    }
    drawPath(shillongRidge, color = contourColor, style = style)
}

/**
 * Accurate boundary coordinates for all 8 Northeastern States of India
 */
private fun getNorthEastStateBoundaries(): List<StateBoundary> {
    return listOf(
        // 1. SIKKIM
        StateBoundary(
            name = "Sikkim",
            shortCode = "SK",
            centerLat = 27.53,
            centerLon = 88.51,
            polygon = listOf(
                GeoPoint(27.08, 88.08),
                GeoPoint(27.35, 88.05), // Kanchenjunga border
                GeoPoint(27.85, 88.15),
                GeoPoint(28.08, 88.55), // Northern crest
                GeoPoint(27.95, 88.85),
                GeoPoint(27.42, 88.88), // Chumbi valley border
                GeoPoint(27.12, 88.65),
                GeoPoint(27.08, 88.08)
            ),
            strokeColor = Color(0xFF38BDF8),
            fillColor = Color(0x220284C7)
        ),

        // 2. ASSAM (The Brahmaputra Valley Corridor)
        StateBoundary(
            name = "Assam",
            shortCode = "AS",
            centerLat = 26.35,
            centerLon = 92.80,
            polygon = listOf(
                GeoPoint(26.00, 89.92), // Dhubri
                GeoPoint(26.45, 90.20),
                GeoPoint(26.80, 90.60), // Manas border
                GeoPoint(26.85, 91.80),
                GeoPoint(26.95, 92.70), // Tezpur north
                GeoPoint(27.25, 93.90),
                GeoPoint(27.50, 94.75), // North Lakhimpur
                GeoPoint(27.85, 95.65), // Sadiya
                GeoPoint(27.60, 95.95), // Tinsukia east
                GeoPoint(27.15, 95.40), // Dibrugarh south
                GeoPoint(26.85, 94.80), // Jorhat
                GeoPoint(26.30, 93.80), // Golaghat
                GeoPoint(25.80, 93.30), // Karbi Anglong
                GeoPoint(25.10, 93.10),
                GeoPoint(24.80, 92.80), // Silchar (Barak valley)
                GeoPoint(24.70, 92.35), // Karimganj
                GeoPoint(25.00, 92.40),
                GeoPoint(25.65, 92.60), // Border Meghalaya east
                GeoPoint(25.85, 91.90), // North of Shillong
                GeoPoint(25.95, 90.90), // North of Garo hills
                GeoPoint(26.00, 89.92)
            ),
            strokeColor = Color(0xFF00E699),
            fillColor = Color(0x1800E699)
        ),

        // 3. MEGHALAYA (The Shillong Plateau)
        StateBoundary(
            name = "Meghalaya",
            shortCode = "ML",
            centerLat = 25.50,
            centerLon = 91.35,
            polygon = listOf(
                GeoPoint(25.50, 89.95), // Garo hills west
                GeoPoint(25.85, 90.15),
                GeoPoint(25.95, 90.90),
                GeoPoint(25.85, 91.90), // North Khasi hills
                GeoPoint(25.65, 92.60), // Jaintia hills north
                GeoPoint(25.30, 92.75), // Jaintia east
                GeoPoint(25.15, 92.15), // Dawki border
                GeoPoint(25.18, 91.65), // Cherrapunji escarpment
                GeoPoint(25.20, 90.65), // Baghmara
                GeoPoint(25.50, 89.95)
            ),
            strokeColor = Color(0xFFEF4444),
            fillColor = Color(0x22EF4444)
        ),

        // 4. ARUNACHAL PRADESH (Eastern Himalayan Crest)
        StateBoundary(
            name = "Arunachal Pradesh",
            shortCode = "AR",
            centerLat = 28.15,
            centerLon = 94.60,
            polygon = listOf(
                GeoPoint(27.42, 88.88),
                GeoPoint(27.60, 91.90), // Tawang
                GeoPoint(27.85, 92.40),
                GeoPoint(28.25, 93.40), // Subansiri
                GeoPoint(28.85, 94.60), // Siang
                GeoPoint(29.25, 95.80), // Dibang valley north
                GeoPoint(28.50, 96.80), // Anjaw east
                GeoPoint(27.80, 97.20), // Easternmost border
                GeoPoint(27.30, 96.20), // Patkai border
                GeoPoint(27.60, 95.95),
                GeoPoint(27.85, 95.65),
                GeoPoint(27.50, 94.75),
                GeoPoint(27.25, 93.90),
                GeoPoint(26.95, 92.70),
                GeoPoint(26.85, 91.80),
                GeoPoint(27.60, 91.90)
            ),
            strokeColor = Color(0xFFA855F7),
            fillColor = Color(0x18A855F7)
        ),

        // 5. NAGALAND (The Naga Hills)
        StateBoundary(
            name = "Nagaland",
            shortCode = "NL",
            centerLat = 26.15,
            centerLon = 94.55,
            polygon = listOf(
                GeoPoint(26.85, 95.10), // Mon north
                GeoPoint(26.40, 95.20), // Myanmar border
                GeoPoint(25.80, 94.75),
                GeoPoint(25.55, 94.25), // Dzükou border
                GeoPoint(25.60, 93.85), // Dimapur south
                GeoPoint(26.10, 93.90),
                GeoPoint(26.55, 94.60),
                GeoPoint(26.85, 95.10)
            ),
            strokeColor = Color(0xFFF59E0B),
            fillColor = Color(0x20F59E0B)
        ),

        // 6. MANIPUR (The Imphal Basin)
        StateBoundary(
            name = "Manipur",
            shortCode = "MN",
            centerLat = 24.80,
            centerLon = 93.90,
            polygon = listOf(
                GeoPoint(25.55, 94.25), // North border
                GeoPoint(25.20, 94.65), // Myanmar border
                GeoPoint(24.40, 94.40),
                GeoPoint(23.85, 93.45), // South border
                GeoPoint(24.30, 93.10), // Mizoram border
                GeoPoint(24.95, 93.20),
                GeoPoint(25.40, 93.65),
                GeoPoint(25.55, 94.25)
            ),
            strokeColor = Color(0xFF10B981),
            fillColor = Color(0x2010B981)
        ),

        // 7. MIZORAM (The Lushai Ridges)
        StateBoundary(
            name = "Mizoram",
            shortCode = "MZ",
            centerLat = 23.40,
            centerLon = 92.80,
            polygon = listOf(
                GeoPoint(24.40, 92.75), // North border
                GeoPoint(24.30, 93.10),
                GeoPoint(23.85, 93.45),
                GeoPoint(22.95, 93.20), // East Myanmar border
                GeoPoint(22.15, 92.95), // South tip (Saiha)
                GeoPoint(22.45, 92.50), // Bangladesh border
                GeoPoint(23.40, 92.35),
                GeoPoint(24.15, 92.45),
                GeoPoint(24.40, 92.75)
            ),
            strokeColor = Color(0xFFEC4899),
            fillColor = Color(0x20EC4899)
        ),

        // 8. TRIPURA
        StateBoundary(
            name = "Tripura",
            shortCode = "TR",
            centerLat = 23.85,
            centerLon = 91.70,
            polygon = listOf(
                GeoPoint(24.50, 92.15), // North border
                GeoPoint(24.15, 92.45),
                GeoPoint(23.40, 92.35),
                GeoPoint(23.00, 91.75), // Sabroom south
                GeoPoint(23.35, 91.40), // Belonia west
                GeoPoint(23.85, 91.25), // Agartala
                GeoPoint(24.30, 91.80),
                GeoPoint(24.50, 92.15)
            ),
            strokeColor = Color(0xFF6366F1),
            fillColor = Color(0x206366F1)
        )
    )
}

/**
 * Surrounding Indian Subcontinent & West Bengal corridor outline
 */
private fun getIndiaSubcontinentOutline(): List<GeoPoint> {
    return listOf(
        GeoPoint(26.50, 88.10), // Siliguri / West Bengal corridor
        GeoPoint(26.10, 88.40),
        GeoPoint(25.30, 88.50),
        GeoPoint(24.50, 88.30),
        GeoPoint(22.50, 88.35), // Kolkata / Sundarbans
        GeoPoint(21.80, 87.80),
        GeoPoint(21.60, 89.00), // Bay of Bengal coastline
        GeoPoint(22.00, 91.50), // Chittagong coast
        GeoPoint(21.50, 92.20),
        GeoPoint(22.00, 93.20), // Myanmar border
        GeoPoint(24.00, 94.60),
        GeoPoint(26.00, 95.50),
        GeoPoint(27.50, 97.20),
        GeoPoint(28.50, 96.80),
        GeoPoint(29.25, 95.80),
        GeoPoint(28.00, 92.00),
        GeoPoint(27.10, 88.60),
        GeoPoint(26.50, 88.10)
    )
}

/**
 * Brahmaputra river pathway coordinates
 */
private fun getBrahmaputraRiverPath(): List<GeoPoint> {
    return listOf(
        GeoPoint(28.10, 95.40), // Pasighat (Siang becomes Brahmaputra)
        GeoPoint(27.85, 95.65), // Sadiya confluence
        GeoPoint(27.48, 94.90), // Dibrugarh
        GeoPoint(26.90, 94.20), // Majuli island
        GeoPoint(26.65, 92.80), // Tezpur
        GeoPoint(26.25, 92.00),
        GeoPoint(26.14, 91.74), // Guwahati
        GeoPoint(26.05, 90.62), // Goalpara
        GeoPoint(25.95, 89.98)  // Dhubri into Jamuna
    )
}

/**
 * NH-10 Siliguri to Gangtok Corridor
 */
private fun getNH10Corridor(): List<GeoPoint> {
    return listOf(
        GeoPoint(26.72, 88.40), // Siliguri
        GeoPoint(26.90, 88.45), // Sevoke coronation bridge
        GeoPoint(27.10, 88.48), // Teesta low dam
        GeoPoint(27.18, 88.52), // Rangpo border
        GeoPoint(27.24, 88.55), // Singtam
        GeoPoint(27.3389, 88.6065) // Gangtok
    )
}

/**
 * NH-6 Guwahati to Shillong to Cherrapunji Corridor
 */
private fun getNH6Corridor(): List<GeoPoint> {
    return listOf(
        GeoPoint(26.1445, 91.7362), // Guwahati
        GeoPoint(25.95, 91.80), // Jorabat / Nongpoh
        GeoPoint(25.75, 91.86), // Umsning / Umiam lake
        GeoPoint(25.5788, 91.8933), // Shillong Ridge
        GeoPoint(25.38, 91.80), // Sohra turnoff
        GeoPoint(25.2702, 91.7323)  // Cherrapunji
    )
}

/**
 * NH-29 Dimapur to Kohima to Imphal Corridor
 */
private fun getNH29Corridor(): List<GeoPoint> {
    return listOf(
        GeoPoint(25.92, 93.73), // Dimapur
        GeoPoint(25.80, 93.90), // Medziphema
        GeoPoint(25.6701, 94.1077), // Kohima
        GeoPoint(25.35, 94.12), // Maram
        GeoPoint(25.05, 94.00), // Senapati
        GeoPoint(24.8170, 93.9368)  // Imphal West
    )
}
