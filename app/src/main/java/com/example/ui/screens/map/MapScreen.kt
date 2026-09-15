package com.example.ui.screens.map

import android.graphics.Paint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import java.util.Locale
import kotlin.math.roundToInt
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CrisisAlert
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RiskLevel
import com.example.data.model.ZoneRiskData
import com.example.ui.theme.DarkGlassBorder
import com.example.ui.theme.DarkGlassCard
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.RiskCritical
import com.example.ui.theme.RiskNormal
import com.example.ui.theme.RiskWatch
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

enum class MapViewType {
    TACTICAL,
    SATELLITE
}

data class NorthEastRegionPreset(
    val id: String,
    val name: String,
    val stateName: String,
    val district: String,
    val latitude: Double,
    val longitude: Double,
    val defaultZoom: Float,
    val targetZoneId: String? = null,
    val elevationMeters: String = "1,500m",
    val description: String = "High geological susceptibility"
)

val NORTH_EAST_REGIONS = listOf(
    NorthEastRegionPreset(
        id = "all_ne",
        name = "All North East",
        stateName = "Overview Sector",
        district = "7 Sisters + Sikkim",
        latitude = 25.8,
        longitude = 92.4,
        defaultZoom = 1.05f,
        elevationMeters = "200m - 5,200m",
        description = "Entire Himalayan geological corridor overview"
    ),
    NorthEastRegionPreset(
        id = "sikkim_gangtok",
        name = "Gangtok & NH-10",
        stateName = "Sikkim",
        district = "East Sikkim",
        latitude = 27.3389,
        longitude = 88.6065,
        defaultZoom = 2.45f,
        targetZoneId = "zone_gangtok",
        elevationMeters = "1,650m",
        description = "Critical transit lifeline vulnerable to landslides"
    ),
    NorthEastRegionPreset(
        id = "meghalaya_shillong",
        name = "Shillong Ridge",
        stateName = "Meghalaya",
        district = "East Khasi Hills",
        latitude = 25.5788,
        longitude = 91.8933,
        defaultZoom = 2.45f,
        targetZoneId = "zone_shillong",
        elevationMeters = "1,525m",
        description = "Active slip detected along upper road corridor"
    ),
    NorthEastRegionPreset(
        id = "meghalaya_cherra",
        name = "Cherrapunji",
        stateName = "Meghalaya",
        district = "East Khasi Hills",
        latitude = 25.2702,
        longitude = 91.7323,
        defaultZoom = 2.5f,
        targetZoneId = "zone_cherrapunji",
        elevationMeters = "1,430m",
        description = "Extreme rainfall basin with deep pore water pressure"
    ),
    NorthEastRegionPreset(
        id = "mizoram_aizawl",
        name = "Aizawl Valley",
        stateName = "Mizoram",
        district = "Aizawl",
        latitude = 23.7271,
        longitude = 92.7176,
        defaultZoom = 2.4f,
        targetZoneId = "zone_aizawl",
        elevationMeters = "1,132m",
        description = "Steep urban ridges prone to slope displacement"
    ),
    NorthEastRegionPreset(
        id = "nagaland_kohima",
        name = "Kohima Dzükou",
        stateName = "Nagaland",
        district = "Kohima",
        latitude = 25.6701,
        longitude = 94.1077,
        defaultZoom = 2.4f,
        targetZoneId = "zone_kohima",
        elevationMeters = "1,444m",
        description = "Fault line corridor with continuous creep sensors"
    ),
    NorthEastRegionPreset(
        id = "arunachal_itanagar",
        name = "Itanagar Slopes",
        stateName = "Arunachal Pradesh",
        district = "Papum Pare",
        latitude = 27.0844,
        longitude = 93.6053,
        defaultZoom = 2.4f,
        targetZoneId = "zone_itanagar",
        elevationMeters = "320m",
        description = "Foothill sector with stable shale bedrock"
    ),
    NorthEastRegionPreset(
        id = "manipur_imphal",
        name = "Imphal Foothills",
        stateName = "Manipur",
        district = "Imphal West",
        latitude = 24.8170,
        longitude = 93.9368,
        defaultZoom = 2.4f,
        targetZoneId = "zone_imphal",
        elevationMeters = "786m",
        description = "Valley periphery monitoring with moderate saturation"
    ),
    NorthEastRegionPreset(
        id = "assam_guwahati",
        name = "Guwahati Hills",
        stateName = "Assam",
        district = "Kamrup Metropolitan",
        latitude = 26.1445,
        longitude = 91.7362,
        defaultZoom = 2.4f,
        targetZoneId = "zone_guwahati",
        elevationMeters = "55m - 280m",
        description = "Urban hill cutting and Brahmaputra river drainage"
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    zones: List<ZoneRiskData>,
    selectedZoneId: String,
    onSelectZone: (String) -> Unit,
    onNavigateToZoneDetails: (String) -> Unit
) {
    val dashEffect12 = remember { PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f) }
    val dashEffect10 = remember { PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f) }
    val dashEffect8 = remember { PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f) }
    val dashEffect15 = remember { PathEffect.dashPathEffect(floatArrayOf(15f, 10f), 0f) }

    var currentMapLayer by remember { mutableStateOf(RealMapLayer.SATELLITE) }
    var infraredMoistureOverlay by remember { mutableStateOf(false) }
    var showSectorSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    val defaultFallbackZone = remember {
        ZoneRiskData(
            id = "zone_shillong",
            name = "Shillong Ridge (NH-6)",
            district = "East Khasi Hills",
            state = "Meghalaya",
            riskScore = 84,
            riskLevel = com.example.data.model.RiskLevel.CRITICAL,
            probabilityPercent = 88,
            rainfallLastHourMm = 32.0f,
            rainfall24hMm = 145.0f,
            soilMoisturePercent = 88,
            temperatureC = 19.5f,
            humidityPercent = 94,
            slopeMovementMm = 4.8f,
            activeSensorsCount = 12,
            totalSensorsCount = 12,
            lastUpdated = "10 mins ago",
            latitude = 25.5788,
            longitude = 91.8933,
            riskFactors = listOf("Heavy 3-hour continuous precipitation", "Steep 48° shale gradient"),
            shapExplanation = emptyList(),
            nodes = emptyList()
        )
    }

    var selectedZone by remember(zones, selectedZoneId) {
        mutableStateOf(zones.find { it.id == selectedZoneId } ?: zones.firstOrNull() ?: defaultFallbackZone)
    }
    var activeRegionId by remember(selectedZoneId) {
        mutableStateOf(
            NORTH_EAST_REGIONS.find { it.targetZoneId == selectedZoneId }?.id ?: "sikkim_gangtok"
        )
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF07120C))
    ) {
        // 1-CLICK REGION SELECTION EXECUTION FUNCTION
        fun flyToRegion(preset: NorthEastRegionPreset) {
            activeRegionId = preset.id
            if (preset.targetZoneId != null) {
                val foundZone = zones.find { it.id == preset.targetZoneId }
                if (foundZone != null) {
                    selectedZone = foundZone
                    if (foundZone.id != selectedZoneId) {
                        onSelectZone(foundZone.id)
                    }
                }
            }
        }

        // Center initially on selected zone
        LaunchedEffect(selectedZoneId) {
            val matched = NORTH_EAST_REGIONS.find { it.targetZoneId == selectedZoneId }
            if (matched != null) {
                flyToRegion(matched)
            } else {
                val z = zones.find { it.id == selectedZoneId } ?: zones.firstOrNull() ?: defaultFallbackZone
                selectedZone = z
            }
        }

        // ==========================================
        // MAP CANVAS: TACTICAL & SATELLITE LAYERS
        // ==========================================
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        coroutineScope.launch {
                            val newZoom = (zoomAnim.value * zoom).coerceIn(0.8f, 4.2f)
                            zoomAnim.snapTo(newZoom)
                            val maxPan = 1200f * newZoom
                            panXAnim.snapTo((panXAnim.value + pan.x).coerceIn(-maxPan, maxPan))
                            panYAnim.snapTo((panYAnim.value + pan.y).coerceIn(-maxPan, maxPan))
                        }
                    }
                }
        ) {
            val currentZoom = zoomAnim.value
            val currentPanX = panXAnim.value
            val currentPanY = panYAnim.value

            fun projectToScreen(lat: Double, lon: Double): Offset {
                val normX = ((lon - minLon) / (maxLon - minLon)).toFloat().coerceIn(0f, 1f)
                val normY = (1.0f - ((lat - minLat) / (maxLat - minLat))).toFloat().coerceIn(0f, 1f)
                val baseX = normX * width * 0.82f + width * 0.09f
                val baseY = normY * height * 0.72f + height * 0.14f
                val sx = (baseX - width / 2f) * currentZoom + width / 2f + currentPanX
                val sy = (baseY - height / 2f) * currentZoom + height / 2f + currentPanY
                return Offset(sx, sy)
            }

            // 1. BACKGROUND RENDER (Tactical vs Satellite)
            if (mapViewType == MapViewType.TACTICAL) {
                drawRect(color = Color(0xFF0C1B14))

                // Tactical Coordinate Grid (Safe Bounded Loops)
                val gridStep = (48f * currentZoom).coerceIn(35f, 160f)
                val numLinesX = (size.width / gridStep).toInt().coerceIn(1, 35)
                val startX = (currentPanX % gridStep).let { if (it < 0) it + gridStep else it }
                for (i in 0..numLinesX) {
                    val lineX = startX + i * gridStep
                    if (lineX in 0f..size.width) {
                        drawLine(
                            color = Color(0x18FFFFFF),
                            start = Offset(lineX, 0f),
                            end = Offset(lineX, size.height),
                            strokeWidth = 1f
                        )
                    }
                }
                val numLinesY = (size.height / gridStep).toInt().coerceIn(1, 35)
                val startY = (currentPanY % gridStep).let { if (it < 0) it + gridStep else it }
                for (j in 0..numLinesY) {
                    val lineY = startY + j * gridStep
                    if (lineY in 0f..size.height) {
                        drawLine(
                            color = Color(0x18FFFFFF),
                            start = Offset(0f, lineY),
                            end = Offset(size.width, lineY),
                            strokeWidth = 1f
                        )
                    }
                }

                // Tactical Hazard Exclusion Zones
                val gangtokCenter = projectToScreen(27.3389, 88.6065)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(RiskCritical.copy(alpha = 0.35f), Color.Transparent),
                        center = gangtokCenter,
                        radius = 75f * currentZoom
                    ),
                    center = gangtokCenter,
                    radius = 75f * currentZoom
                )
                drawCircle(
                    color = RiskCritical.copy(alpha = 0.8f),
                    radius = 75f * currentZoom,
                    center = gangtokCenter,
                    style = Stroke(
                        width = 1.8f * currentZoom,
                        pathEffect = dashEffect12
                    )
                )

                val shillongCenter = projectToScreen(25.5788, 91.8933)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(RiskCritical.copy(alpha = 0.32f), Color.Transparent),
                        center = shillongCenter,
                        radius = 70f * currentZoom
                    ),
                    center = shillongCenter,
                    radius = 70f * currentZoom
                )

                // Doppler Radar Sweeping Beam
                val radarCenter = shillongCenter
                val radarRadius = 140f * currentZoom
                drawCircle(
                    color = Color(0x3310B981),
                    radius = radarRadius,
                    center = radarCenter,
                    style = Stroke(width = 1.2f)
                )
                drawCircle(
                    color = Color(0x1E10B981),
                    radius = radarRadius * 0.5f,
                    center = radarCenter,
                    style = Stroke(width = 1f)
                )
                val angleRad = Math.toRadians(radarAngle.toDouble())
                val beamEnd = Offset(
                    x = (radarCenter.x + radarRadius * cos(angleRad)).toFloat(),
                    y = (radarCenter.y + radarRadius * sin(angleRad)).toFloat()
                )
                drawLine(
                    color = Color(0xAA34D399),
                    start = radarCenter,
                    end = beamEnd,
                    strokeWidth = 2.4f
                )

                // Tactical Telemetry Link Vectors
                val pGuwahati = projectToScreen(26.1445, 91.7362)
                val pCherra = projectToScreen(25.2702, 91.7323)
                val pKohima = projectToScreen(25.6701, 94.1077)
                drawLine(
                    color = EmeraldAccent.copy(alpha = 0.4f),
                    start = pGuwahati,
                    end = shillongCenter,
                    strokeWidth = 1.5f,
                    pathEffect = dashEffect10
                )
                drawLine(
                    color = EmeraldAccent.copy(alpha = 0.35f),
                    start = shillongCenter,
                    end = pCherra,
                    strokeWidth = 1.5f,
                    pathEffect = dashEffect8
                )
                drawLine(
                    color = EmeraldAccent.copy(alpha = 0.35f),
                    start = pGuwahati,
                    end = pKohima,
                    strokeWidth = 1.5f,
                    pathEffect = dashEffect10
                )
            } else {
                // SATELLITE TOPOGRAPHIC VIEW
                drawRect(color = Color(0xFF07120A))

                // Snow Fields & Glaciers (Upper Himalayas)
                val snowArea = Path().apply {
                    val p0 = projectToScreen(28.5, 88.0)
                    moveTo(p0.x, p0.y)
                    val p1 = projectToScreen(28.8, 91.5)
                    val p2 = projectToScreen(28.9, 94.5)
                    val p3 = projectToScreen(28.7, 96.5)
                    val p4 = projectToScreen(27.8, 96.0)
                    val p5 = projectToScreen(27.9, 91.8)
                    val p6 = projectToScreen(27.6, 88.2)
                    cubicTo(p1.x, p1.y, p2.x, p2.y, p3.x, p3.y)
                    lineTo(p4.x, p4.y)
                    cubicTo(p5.x, p5.y, p6.x, p6.y, p0.x, p0.y)
                }
                drawPath(
                    path = snowArea,
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0x44E0F2FE), Color(0x18BAE6FD), Color.Transparent)
                    )
                )

                // Dense Rainforest Canopy
                val forestRange = Path().apply {
                    val f0 = projectToScreen(26.2, 90.5)
                    moveTo(f0.x, f0.y)
                    val f1 = projectToScreen(26.0, 93.5)
                    val f2 = projectToScreen(25.4, 94.6)
                    val f3 = projectToScreen(23.2, 93.4)
                    val f4 = projectToScreen(23.4, 92.2)
                    val f5 = projectToScreen(25.0, 91.2)
                    cubicTo(f1.x, f1.y, f2.x, f2.y, f3.x, f3.y)
                    cubicTo(f4.x, f4.y, f5.x, f5.y, f0.x, f0.y)
                }
                drawPath(
                    path = forestRange,
                    color = Color(0x3514532D)
                )

                // Topographic Elevation Contours
                for (i in 0..6) {
                    val latOffset = i * 0.7
                    val cPath = Path().apply {
                        val s = projectToScreen(28.2 - latOffset, 88.0)
                        moveTo(s.x, s.y)
                        val m1 = projectToScreen(27.7 - latOffset, 91.2)
                        val m2 = projectToScreen(27.3 - latOffset, 93.8)
                        val e = projectToScreen(27.9 - latOffset, 96.2)
                        cubicTo(m1.x, m1.y, m2.x, m2.y, e.x, e.y)
                    }
                    drawPath(
                        path = cPath,
                        color = Color(0xFF22543D).copy(alpha = 0.55f),
                        style = Stroke(width = 1.6f * currentZoom)
                    )
                }

                // False Color Infrared Moisture Scan
                if (infraredMoistureOverlay) {
                    val moistureCenter = projectToScreen(25.4, 91.8)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0x66A855F7),
                                Color(0x44EC4899),
                                Color(0x22F59E0B),
                                Color.Transparent
                            ),
                            center = moistureCenter,
                            radius = 160f * currentZoom
                        ),
                        center = moistureCenter,
                        radius = 160f * currentZoom
                    )
                }
            }

            // 2. COMMON GEOGRAPHICAL FEATURES (Borders & Corridors)
            val northernBorder = Path().apply {
                val p0 = projectToScreen(27.8, 88.0)
                moveTo(p0.x, p0.y)
                val p1 = projectToScreen(28.0, 88.8)
                val p2 = projectToScreen(28.2, 92.0)
                val p3 = projectToScreen(28.6, 94.2)
                val p4 = projectToScreen(28.4, 96.4)
                cubicTo(p1.x, p1.y, p2.x, p2.y, p3.x, p3.y)
                lineTo(p4.x, p4.y)
            }
            drawPath(
                path = northernBorder,
                color = if (mapViewType == MapViewType.TACTICAL) Color(0x664ADE80) else Color(0x44A7F3D0),
                style = Stroke(
                    width = 2.2f * currentZoom,
                    pathEffect = dashEffect15
                )
            )

            // Brahmaputra River Basin Corridor
            val riverPath = Path().apply {
                val r0 = projectToScreen(28.1, 95.6)
                moveTo(r0.x, r0.y)
                val r1 = projectToScreen(27.4, 94.8)
                val r2 = projectToScreen(26.6, 92.8)
                val r3 = projectToScreen(26.18, 91.75)
                val r4 = projectToScreen(26.0, 90.0)
                cubicTo(r1.x, r1.y, r2.x, r2.y, r3.x, r3.y)
                lineTo(r4.x, r4.y)
            }
            drawPath(
                path = riverPath,
                color = if (mapViewType == MapViewType.TACTICAL) Color(0x2238BDF8) else Color(0x330284C7),
                style = Stroke(width = 8f * currentZoom)
            )
            drawPath(
                path = riverPath,
                color = if (mapViewType == MapViewType.TACTICAL) Color(0x9938BDF8) else Color(0xCC38BDF8),
                style = Stroke(width = 3.2f * currentZoom)
            )

            // Strategic Lifeline Highways (NH-10 & NH-6)
            val nh10Path = Path().apply {
                val nh0 = projectToScreen(26.7, 88.4)
                moveTo(nh0.x, nh0.y)
                val nh1 = projectToScreen(27.3389, 88.6065)
                lineTo(nh1.x, nh1.y)
            }
            drawPath(
                path = nh10Path,
                color = Color(0x88FACC15),
                style = Stroke(
                    width = 2.4f * currentZoom,
                    pathEffect = dashEffect8
                )
            )

            val nh6Path = Path().apply {
                val g0 = projectToScreen(26.1445, 91.7362)
                moveTo(g0.x, g0.y)
                val g1 = projectToScreen(25.5788, 91.8933)
                lineTo(g1.x, g1.y)
            }
            drawPath(
                path = nh6Path,
                color = Color(0x99FACC15),
                style = Stroke(
                    width = 2.4f * currentZoom,
                    pathEffect = dashEffect8
                )
            )

            // 3. ZONE RISK MARKERS & ACTIVE BEACON
            zones.forEach { z ->
                val center = projectToScreen(z.latitude, z.longitude)
                val isSelected = z.id == selectedZone.id
                val isCritical = z.riskLevel == RiskLevel.CRITICAL

                val pinColor = when (z.riskLevel) {
                    RiskLevel.CRITICAL -> RiskCritical
                    RiskLevel.WATCH -> RiskWatch
                    RiskLevel.NORMAL -> RiskNormal
                    RiskLevel.OFFLINE -> Color(0xFF94A3B8)
                }

                // Pulsing outer ripple
                if (isCritical || isSelected) {
                    val rippleRadius = if (isSelected) 38f * currentZoom * pulseScale else 28f * currentZoom * pulseScale
                    drawCircle(
                        color = pinColor.copy(alpha = 0.28f),
                        radius = rippleRadius,
                        center = center
                    )
                }

                // Outer Halo
                drawCircle(
                    color = pinColor.copy(alpha = if (isSelected) 0.55f else 0.28f),
                    radius = if (isSelected) 24f * currentZoom else 15f * currentZoom,
                    center = center
                )

                // Solid Marker Circle
                drawCircle(
                    color = pinColor,
                    radius = if (isSelected) 14f * currentZoom else 9f * currentZoom,
                    center = center
                )

                // Inner White Bullseye
                drawCircle(
                    color = Color.White,
                    radius = if (isSelected) 5.5f * currentZoom else 3.5f * currentZoom,
                    center = center
                )

                // Selected Target Ring
                if (isSelected) {
                    drawCircle(
                        color = EmeraldAccent,
                        radius = 28f * currentZoom,
                        center = center,
                        style = Stroke(width = 2.5f)
                    )
                }
            }
        }



        // =========================================================
        // TOP CONTROLS: UNIFIED HEADER BAR WITH VIEW SLIDER & SECTORS
        // =========================================================
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            shape = RoundedCornerShape(18.dp),
            color = Color(0xF20A1911),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkGlassBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                // ROW 1: Title + Tactical / Satellite Slider Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(RiskCritical)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "North-East Sector Map",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Text(
                            text = if (mapViewType == MapViewType.TACTICAL)
                                "Tactical GIS Telemetry"
                            else
                                "Sentinel-2 Topography",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.65f)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // THE REQUESTED TACTICAL & SATELLITE VIEW SLIDER
                    TacticalSatelliteSlider(
                        mapViewType = mapViewType,
                        onTypeChanged = { mapViewType = it }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // ROW 2: Sectors Button + Horizontal Region Selector Carousel
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Quick Sectors Drawer Button
                    Surface(
                        onClick = { showSectorSheet = true },
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF143024),
                        border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldAccent.copy(alpha = 0.6f)),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.FilterList,
                                contentDescription = "Sectors",
                                tint = EmeraldAccent,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Sectors",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldAccent
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // 1-Click Region Pills Carousel
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        NORTH_EAST_REGIONS.forEach { preset ->
                            val isSelected = preset.id == activeRegionId
                            Surface(
                                onClick = { flyToRegion(preset) },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) EmeraldAccent else Color(0x9912281D),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) EmeraldAccent else DarkGlassBorder
                                ),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .size(5.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF003822))
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                    }
                                    Text(
                                        text = preset.name,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color(0xFF003822) else Color.White
                                    )
                                }
                            }
                        }
                    }
                }

                // Satellite Sub-Toolbar (Infrared Moisture Heatmap Toggle)
                if (mapViewType == MapViewType.SATELLITE) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🛰️ 10m Ground Resolution • Live",
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.65f)
                        )

                        Surface(
                            onClick = { infraredMoistureOverlay = !infraredMoistureOverlay },
                            shape = RoundedCornerShape(10.dp),
                            color = if (infraredMoistureOverlay) Color(0xFF6B21A8) else Color(0x66143024),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (infraredMoistureOverlay) Color(0xFFA855F7) else DarkGlassBorder
                            ),
                            modifier = Modifier.height(26.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (infraredMoistureOverlay) "⚡ Moisture Scan: ON" else "💧 Moisture Scan",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (infraredMoistureOverlay) Color.White else EmeraldAccent
                                )
                            }
                        }
                    }
                }
            }
        }

        // =========================================================
        // FLOATING ACTION CONTROLS (Grouped Compact Right-hand Cluster)
        // =========================================================
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Target Critical Hazard (1-Click Emergency Jump)
            IconButton(
                onClick = {
                    val criticalPreset = NORTH_EAST_REGIONS.find { it.id == "sikkim_gangtok" }
                        ?: NORTH_EAST_REGIONS.find { it.id == "meghalaya_shillong" }
                        ?: NORTH_EAST_REGIONS[1]
                    flyToRegion(criticalPreset)
                },
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(0xE62E1214))
                    .border(1.dp, RiskCritical, CircleShape)
            ) {
                Icon(
                    Icons.Default.CrisisAlert,
                    contentDescription = "Target Critical Hazard",
                    tint = RiskCritical,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Center Selected Zone
            IconButton(
                onClick = {
                    val currentPreset = NORTH_EAST_REGIONS.find { it.targetZoneId == selectedZone.id }
                        ?: NORTH_EAST_REGIONS[1]
                    flyToRegion(currentPreset)
                },
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(0xE60E2218))
                    .border(1.dp, DarkGlassBorder, CircleShape)
            ) {
                Icon(
                    Icons.Default.MyLocation,
                    contentDescription = "Center Selected Zone",
                    tint = EmeraldAccent,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Unified Zoom Capsule (+ and - in one sleek 38dp pill)
            Surface(
                shape = RoundedCornerShape(19.dp),
                color = Color(0xE60E2218),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkGlassBorder),
                modifier = Modifier.width(38.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                zoomAnim.animateTo((zoomAnim.value * 1.3f).coerceAtMost(4.2f), tween(200))
                            }
                        },
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Zoom In",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(22.dp)
                            .height(1.dp)
                            .background(DarkGlassBorder)
                    )

                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                zoomAnim.animateTo((zoomAnim.value / 1.3f).coerceAtLeast(0.8f), tween(200))
                            }
                        },
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            Icons.Default.Remove,
                            contentDescription = "Zoom Out",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // =========================================================
        // BOTTOM SELECTED ZONE TELEMETRY HUD CARD
        // =========================================================
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .widthIn(max = 560.dp)
                .padding(horizontal = 14.dp, vertical = 6.dp),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xF20B1912)),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkGlassBorder)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Header: Zone Name & Risk Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = selectedZone.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1
                        )
                        Text(
                            text = "${selectedZone.district}, ${selectedZone.state} • ${String.format(Locale.US, "%.4f°N, %.4f°E", selectedZone.latitude, selectedZone.longitude)}",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.65f),
                            maxLines = 1
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Risk Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(selectedZone.riskLevel.bgColor)
                            .border(1.dp, selectedZone.riskLevel.color, RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "${selectedZone.riskLevel.label} • ${selectedZone.riskScore}/100",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = selectedZone.riskLevel.color
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Telemetry Metrics Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TelemetryMiniItem(
                        title = "Rainfall (1h)",
                        value = "${selectedZone.rainfallLastHourMm} mm",
                        tint = Color(0xFF38BDF8)
                    )
                    TelemetryMiniItem(
                        title = "Soil Moisture",
                        value = "${selectedZone.soilMoisturePercent}%",
                        tint = EmeraldAccent
                    )
                    TelemetryMiniItem(
                        title = "Slope Creep",
                        value = "${selectedZone.slopeMovementMm} mm",
                        tint = if (selectedZone.slopeMovementMm > 2.0f) RiskCritical else RiskWatch
                    )
                    TelemetryMiniItem(
                        title = "Active Sensors",
                        value = "${selectedZone.activeSensorsCount}/${selectedZone.totalSensorsCount}",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Action: Deep Dive Analysis
                Button(
                    onClick = { onNavigateToZoneDetails(selectedZone.id) },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Open Zone Details & SHAP Analysis",
                            color = Color(0xFF003822),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color(0xFF003822),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // =========================================================
        // 1-CLICK SECTOR DRAWER / MODAL SHEET
        // =========================================================
        if (showSectorSheet) {
            ModalBottomSheet(
                onDismissRequest = { showSectorSheet = false },
                sheetState = sheetState,
                containerColor = Color(0xFF0F221A),
                scrimColor = Color.Black.copy(alpha = 0.65f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 32.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "North-East Geological Sectors",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Tap any sector to fly and inspect in 1 click",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                        IconButton(onClick = { showSectorSheet = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(NORTH_EAST_REGIONS) { regionPreset ->
                            val matchedZone = zones.find { it.id == regionPreset.targetZoneId }
                            val isCurrent = regionPreset.id == activeRegionId

                            Surface(
                                onClick = {
                                    showSectorSheet = false
                                    flyToRegion(regionPreset)
                                },
                                shape = RoundedCornerShape(14.dp),
                                color = if (isCurrent) Color(0xFF163728) else DarkGlassCard,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isCurrent) EmeraldAccent else DarkGlassBorder
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(
                                                matchedZone?.riskLevel?.color ?: EmeraldAccent
                                            )
                                    )

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = regionPreset.name,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "• ${regionPreset.stateName}",
                                                fontSize = 12.sp,
                                                color = Color.White.copy(alpha = 0.65f)
                                            )
                                        }
                                        Text(
                                            text = "${regionPreset.elevationMeters} • ${regionPreset.description}",
                                            fontSize = 11.sp,
                                            color = Color.White.copy(alpha = 0.6f)
                                        )
                                    }

                                    if (matchedZone != null) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(matchedZone.riskLevel.bgColor)
                                                .border(1.dp, matchedZone.riskLevel.color, RoundedCornerShape(8.dp))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = "${matchedZone.riskScore}/100",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = matchedZone.riskLevel.color
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

// =========================================================
// SLIDING TACTICAL / SATELLITE SWITCH COMPOSABLE
// =========================================================
@Composable
fun TacticalSatelliteSlider(
    mapViewType: MapViewType,
    onTypeChanged: (MapViewType) -> Unit,
    modifier: Modifier = Modifier
) {
    val isSatellite = mapViewType == MapViewType.SATELLITE
    val thumbOffsetFraction by animateFloatAsState(
        targetValue = if (isSatellite) 1f else 0f,
        animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
        label = "sliderThumbOffset"
    )

    BoxWithConstraints(
        modifier = modifier
            .width(152.dp)
            .height(34.dp)
            .clip(RoundedCornerShape(17.dp))
            .background(Color(0xFF0F261C))
            .border(1.dp, DarkGlassBorder, RoundedCornerShape(17.dp))
            .clickable {
                onTypeChanged(if (isSatellite) MapViewType.TACTICAL else MapViewType.SATELLITE)
            }
            .padding(2.dp)
    ) {
        val totalWidth = maxWidth
        val thumbWidth = totalWidth / 2

        // Sliding Highlight Indicator Thumb
        Box(
            modifier = Modifier
                .width(thumbWidth)
                .fillMaxHeight()
                .padding(start = (totalWidth - thumbWidth) * thumbOffsetFraction)
                .clip(RoundedCornerShape(15.dp))
                .background(EmeraldAccent)
        )

        // Text & Icon labels
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { onTypeChanged(MapViewType.TACTICAL) },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Sensors,
                        contentDescription = null,
                        tint = if (!isSatellite) Color(0xFF003822) else Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "Tactical",
                        fontSize = 11.sp,
                        fontWeight = if (!isSatellite) FontWeight.Bold else FontWeight.Medium,
                        color = if (!isSatellite) Color(0xFF003822) else Color.White.copy(alpha = 0.7f)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { onTypeChanged(MapViewType.SATELLITE) },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = null,
                        tint = if (isSatellite) Color(0xFF003822) else Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "Satellite",
                        fontSize = 11.sp,
                        fontWeight = if (isSatellite) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSatellite) Color(0xFF003822) else Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
private fun TelemetryMiniItem(
    title: String,
    value: String,
    tint: Color
) {
    Column {
        Text(
            text = title,
            fontSize = 10.sp,
            color = Color.White.copy(alpha = 0.6f)
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = tint
        )
    }
}
