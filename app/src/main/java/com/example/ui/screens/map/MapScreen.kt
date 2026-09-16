package com.example.ui.screens.map

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CrisisAlert
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Remove
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ZoneRiskData
import com.example.ui.theme.DarkGlassBorder
import com.example.ui.theme.DarkGlassCard
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.RiskCritical
import com.example.ui.theme.RiskNormal
import com.example.ui.theme.RiskWatch
import java.util.Locale

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
        id = "ne_overview",
        name = "All North East",
        stateName = "Regional Corridor",
        district = "Eastern Himalayas",
        latitude = 25.85,
        longitude = 92.40,
        defaultZoom = 1.15f,
        targetZoneId = null,
        elevationMeters = "Regional Grid",
        description = "Regional surveillance of all 8 Northeastern states"
    ),
    NorthEastRegionPreset(
        id = "sikkim_gangtok",
        name = "Gangtok & NH-10",
        stateName = "Sikkim",
        district = "East Sikkim",
        latitude = 27.3389,
        longitude = 88.6065,
        defaultZoom = 2.4f,
        targetZoneId = "zone_gangtok",
        elevationMeters = "1,650m",
        description = "Teesta River gorge & vital highway lifeline"
    ),
    NorthEastRegionPreset(
        id = "meghalaya_shillong",
        name = "Shillong Ridge",
        stateName = "Meghalaya",
        district = "East Khasi Hills",
        latitude = 25.5788,
        longitude = 91.8933,
        defaultZoom = 2.3f,
        targetZoneId = "zone_shillong",
        elevationMeters = "1,525m",
        description = "Barapani Umiam basin slope and shale strata"
    ),
    NorthEastRegionPreset(
        id = "meghalaya_cherrapunji",
        name = "Cherrapunji (Sohra)",
        stateName = "Meghalaya",
        district = "East Khasi Hills",
        latitude = 25.2702,
        longitude = 91.7323,
        defaultZoom = 2.2f,
        targetZoneId = "zone_cherrapunji",
        elevationMeters = "1,484m",
        description = "World rainfall record escarpment and deep canyons"
    ),
    NorthEastRegionPreset(
        id = "mizoram_aizawl",
        name = "Aizawl Chite Valley",
        stateName = "Mizoram",
        district = "Aizawl",
        latitude = 23.7271,
        longitude = 92.7176,
        defaultZoom = 2.2f,
        targetZoneId = "zone_aizawl",
        elevationMeters = "1,132m",
        description = "Steep urbanized ridge with siltstone formations"
    ),
    NorthEastRegionPreset(
        id = "nagaland_kohima",
        name = "Kohima Dzükou",
        stateName = "Nagaland",
        district = "Kohima",
        latitude = 25.6701,
        longitude = 94.1077,
        defaultZoom = 2.2f,
        targetZoneId = "zone_kohima",
        elevationMeters = "1,444m",
        description = "Barail range foothills and monsoon runoff channels"
    ),
    NorthEastRegionPreset(
        id = "arunachal_itanagar",
        name = "Itanagar Papum Pare",
        stateName = "Arunachal Pradesh",
        district = "Papum Pare",
        latitude = 27.0844,
        longitude = 93.6053,
        defaultZoom = 2.2f,
        targetZoneId = "zone_itanagar",
        elevationMeters = "750m",
        description = "Himalayan foothills with dense virgin canopy"
    ),
    NorthEastRegionPreset(
        id = "manipur_imphal",
        name = "Imphal West Slopes",
        stateName = "Manipur",
        district = "Imphal West",
        latitude = 24.8170,
        longitude = 93.9368,
        defaultZoom = 2.1f,
        targetZoneId = "zone_imphal",
        elevationMeters = "786m",
        description = "Intermontane valley perimeter slopes"
    ),
    NorthEastRegionPreset(
        id = "assam_guwahati",
        name = "Guwahati Kamrup",
        stateName = "Assam",
        district = "Kamrup Metro",
        latitude = 26.1445,
        longitude = 91.7362,
        defaultZoom = 2.1f,
        targetZoneId = "zone_guwahati",
        elevationMeters = "55m",
        description = "Brahmaputra riverside hills and urban slopes"
    )
)

/**
 * Primary Map Screen - Renders Real Interactive Map of Northeast India
 * Users can view real satellite photography, topographic contours, OSM road lifelines,
 * or photorealistic relief assets pointing to real locations with live telemetry.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    zones: List<ZoneRiskData>,
    selectedZoneId: String,
    onSelectZone: (String) -> Unit,
    onNavigateToZoneDetails: (String) -> Unit
) {
    var currentMapLayer by remember { mutableStateOf(RealMapLayer.STREET_GIS) }
    var infraredMoistureOverlay by remember { mutableStateOf(false) }
    var showSectorSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    var showLayerSheet by remember { mutableStateOf(false) }
    val layerSheetState = rememberModalBottomSheetState()
    val mapController = rememberRealMapController()

    val defaultFallbackZone = remember {
        ZoneRiskData(
            id = "zone_shillong",
            name = "Shillong Ridge (Barapani Slope)",
            district = "East Khasi Hills",
            state = "Meghalaya, India",
            riskScore = 84,
            riskLevel = com.example.data.model.RiskLevel.CRITICAL,
            probabilityPercent = 88,
            rainfallLastHourMm = 32.0f,
            rainfall24hMm = 188.5f,
            soilMoisturePercent = 92,
            temperatureC = 19.4f,
            humidityPercent = 96,
            slopeMovementMm = 4.8f,
            activeSensorsCount = 6,
            totalSensorsCount = 6,
            lastUpdated = "10 mins ago",
            latitude = 25.5788,
            longitude = 91.8933,
            riskFactors = listOf("Extreme precipitation exceeding 30mm/hr", "Steep 48° shale gradient"),
            shapExplanation = emptyList(),
            nodes = emptyList()
        )
    }

    var selectedZone by remember(zones, selectedZoneId) {
        mutableStateOf(zones.find { it.id == selectedZoneId } ?: zones.firstOrNull() ?: defaultFallbackZone)
    }
    var activeRegionId by remember(selectedZoneId) {
        mutableStateOf(
            NORTH_EAST_REGIONS.find { it.targetZoneId == selectedZoneId }?.id ?: "meghalaya_shillong"
        )
    }

    // 1-Click region selection handler
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
        if (preset.id == "ne_overview") {
            mapController.resetOverview()
        } else {
            mapController.flyTo(
                preset.targetZoneId ?: "",
                preset.latitude,
                preset.longitude,
                preset.defaultZoom
            )
        }
    }

    // Keep active region in sync with incoming selected zone
    LaunchedEffect(selectedZoneId) {
        val matched = NORTH_EAST_REGIONS.find { it.targetZoneId == selectedZoneId }
        if (matched != null) {
            activeRegionId = matched.id
        }
        val z = zones.find { it.id == selectedZoneId }
        if (z != null) {
            selectedZone = z
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF07120C))
    ) {
        // =========================================================
        // REAL NORTHEAST INDIA MAP (SATELLITE / TOPO / OSM / RELIEF)
        // =========================================================
        RealNorthEastMapView(
            zones = zones,
            selectedZoneId = selectedZone.id,
            currentLayer = currentMapLayer,
            moistureOverlayEnabled = infraredMoistureOverlay,
            onSelectZone = { zoneId ->
                onSelectZone(zoneId)
                val z = zones.find { it.id == zoneId }
                if (z != null) {
                    selectedZone = z
                    val matchedPreset = NORTH_EAST_REGIONS.find { it.targetZoneId == z.id }
                    if (matchedPreset != null) {
                        activeRegionId = matchedPreset.id
                    }
                }
            },
            mapController = mapController,
            modifier = Modifier.fillMaxSize()
        )

        // =========================================================
        // TOP CONTROLS: UNIFIED CLEAN RESPONSIVE HEADER BAR
        // =========================================================
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xF2091811),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkGlassBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                // ROW 1: Header Title + Layer Selector Pill (Opens Layer Sheet)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f, fill = false),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(RiskCritical)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "Northeast GIS Map",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "8 States • Live Sensors",
                                fontSize = 10.sp,
                                color = Color.White.copy(alpha = 0.65f),
                                maxLines = 1
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Compact Layer Switcher Pill (Fits on every screen)
                    Surface(
                        onClick = { showLayerSheet = true },
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF143024),
                        border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldAccent.copy(alpha = 0.6f)),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = currentMapLayer.icon,
                                contentDescription = "Map Layer",
                                tint = EmeraldAccent,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = currentMapLayer.shortLabel,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = EmeraldAccent
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = EmeraldAccent,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // ROW 2: Single Fluid Horizontal Scroll Carousel
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Radar Moisture Scan Toggle Pill
                    Surface(
                        onClick = { infraredMoistureOverlay = !infraredMoistureOverlay },
                        shape = RoundedCornerShape(10.dp),
                        color = if (infraredMoistureOverlay) Color(0xFF0284C7) else Color(0x66143024),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (infraredMoistureOverlay) Color(0xFF38BDF8) else DarkGlassBorder
                        ),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (infraredMoistureOverlay) "🌧️ Radar: ON" else "💧 Radar Scan",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (infraredMoistureOverlay) Color.White else Color(0xFF7DD3FC)
                            )
                        }
                    }

                    // Sectors Drawer Button
                    Surface(
                        onClick = { showSectorSheet = true },
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF143024),
                        border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldAccent.copy(alpha = 0.5f)),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.FilterList,
                                contentDescription = "Sectors",
                                tint = EmeraldAccent,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "All Sectors",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldAccent
                            )
                        }
                    }

                    // 1-Click Region Pills Carousel
                    NORTH_EAST_REGIONS.forEach { preset ->
                        val isSelected = preset.id == activeRegionId
                        val matchedZone = zones.find { it.id == preset.targetZoneId }
                        val indicatorColor = matchedZone?.riskLevel?.color ?: EmeraldAccent

                        Surface(
                            onClick = { flyToRegion(preset) },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) EmeraldAccent else Color(0x9912281D),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) EmeraldAccent else DarkGlassBorder
                            ),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) Color(0xFF003822) else indicatorColor)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
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
        }

        // =========================================================
        // FLOATING ACTION CONTROLS (Right-hand Cluster)
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
                    val criticalPreset = NORTH_EAST_REGIONS.find { it.id == "meghalaya_shillong" }
                        ?: NORTH_EAST_REGIONS.find { it.id == "sikkim_gangtok" }
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
                        onClick = { mapController.zoomIn() },
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
                        onClick = { mapController.zoomOut() },
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
                .padding(horizontal = 12.dp, vertical = 6.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xF2091811)),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkGlassBorder)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Header: Real Zone Name & Risk Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = selectedZone.name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${selectedZone.district}, ${selectedZone.state}",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.65f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Risk Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(selectedZone.riskLevel.bgColor)
                            .border(1.dp, selectedZone.riskLevel.color, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${selectedZone.riskLevel.label} • ${selectedZone.riskScore}/100",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = selectedZone.riskLevel.color
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Real-Time Telemetry Metrics Grid (Scaled with equal weights)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TelemetryMiniItem(
                        title = "Rainfall (1h)",
                        value = "${selectedZone.rainfallLastHourMm} mm",
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.weight(1f)
                    )
                    TelemetryMiniItem(
                        title = "Soil Moisture",
                        value = "${selectedZone.soilMoisturePercent}%",
                        tint = EmeraldAccent,
                        modifier = Modifier.weight(1f)
                    )
                    TelemetryMiniItem(
                        title = "Slope Creep",
                        value = "${selectedZone.slopeMovementMm} mm",
                        tint = if (selectedZone.slopeMovementMm > 2.0f) RiskCritical else RiskWatch,
                        modifier = Modifier.weight(1f)
                    )
                    TelemetryMiniItem(
                        title = "Active Sensors",
                        value = "${selectedZone.activeSensorsCount}/${selectedZone.totalSensorsCount}",
                        tint = Color.White,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Action: Deep Dive Analysis Button
                Button(
                    onClick = { onNavigateToZoneDetails(selectedZone.id) },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(vertical = 0.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Open Zone Details & SHAP Analysis",
                            color = Color(0xFF003822),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color(0xFF003822),
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }

        // =========================================================
        // MAP LAYER PICKER MODAL SHEET
        // =========================================================
        if (showLayerSheet) {
            ModalBottomSheet(
                onDismissRequest = { showLayerSheet = false },
                sheetState = layerSheetState,
                containerColor = Color(0xFF0A1C13),
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
                                text = "Select Map Layer",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Choose GIS rendering style for Northeast India",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                        IconButton(onClick = { showLayerSheet = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        RealMapLayer.entries.forEach { layer ->
                            val isSelected = layer == currentMapLayer
                            Surface(
                                onClick = {
                                    currentMapLayer = layer
                                    showLayerSheet = false
                                },
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSelected) Color(0xFF143826) else Color(0x660E2218),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) EmeraldAccent else DarkGlassBorder
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isSelected) EmeraldAccent else Color(0xFF1A3326)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = layer.icon,
                                            contentDescription = layer.title,
                                            tint = if (isSelected) Color(0xFF003822) else EmeraldAccent,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = layer.title,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) EmeraldAccent else Color.White
                                        )
                                        Text(
                                            text = layer.description,
                                            fontSize = 11.sp,
                                            color = Color.White.copy(alpha = 0.7f)
                                        )
                                    }

                                    if (isSelected) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Selected",
                                            tint = EmeraldAccent,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
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

/**
 * Compact, stylish layer switcher capsule for the Real Map
 */
@Composable
fun RealMapLayerSwitcher(
    currentLayer: RealMapLayer,
    onLayerSelected: (RealMapLayer) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF0F261C),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkGlassBorder),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(2.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RealMapLayer.entries.forEach { layer ->
                val isSelected = layer == currentLayer
                val bgColor by animateColorAsState(
                    targetValue = if (isSelected) EmeraldAccent else Color.Transparent,
                    animationSpec = tween(durationMillis = 200),
                    label = "layerBg"
                )
                val textColor by animateColorAsState(
                    targetValue = if (isSelected) Color(0xFF003822) else Color.White.copy(alpha = 0.75f),
                    animationSpec = tween(durationMillis = 200),
                    label = "layerTextColor"
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(bgColor)
                        .clickable { onLayerSelected(layer) }
                        .padding(horizontal = 7.dp, vertical = 5.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = layer.icon,
                            contentDescription = layer.title,
                            tint = textColor,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = layer.shortLabel,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = textColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TelemetryMiniItem(
    title: String,
    value: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = title,
            fontSize = 10.sp,
            color = Color.White.copy(alpha = 0.6f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = tint,
            maxLines = 1
        )
    }
}
