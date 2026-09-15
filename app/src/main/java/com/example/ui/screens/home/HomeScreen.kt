package com.example.ui.screens.home

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.SensorNode
import com.example.data.model.ZoneRiskData
import com.example.ui.components.AlertBanner
import com.example.ui.components.AppStrings
import com.example.ui.components.BottomMetricChip
import com.example.ui.components.MountainLogo
import com.example.ui.components.MountainSlopeTelemetryView
import com.example.ui.components.NodeDetailDialog
import com.example.ui.components.RiskHUDCard
import com.example.ui.theme.DarkGlassBorder
import com.example.ui.theme.DarkGlassCard
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.ForestGreenDark
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.RiskCritical
import com.example.ui.theme.RiskWatch

@Composable
fun HomeScreen(
    zone: ZoneRiskData,
    allZones: List<ZoneRiskData>,
    selectedNode: SensorNode?,
    isRefreshing: Boolean,
    isOffline: Boolean,
    currentLanguage: String = "en",
    onRefresh: () -> Unit,
    onSelectZone: (String) -> Unit,
    onSelectNode: (SensorNode?) -> Unit,
    onNavigateToZoneDetails: (String) -> Unit,
    onNavigateToAlerts: () -> Unit,
    onNavigateToMap: () -> Unit
) {
    val strings = AppStrings.get(currentLanguage)
    var showZoneDropdown by remember { mutableStateOf(false) }
    var rotationAngle by remember { mutableFloatStateOf(0f) }

    val animatedRotation by animateFloatAsState(
        targetValue = if (isRefreshing) rotationAngle + 360f else rotationAngle,
        animationSpec = tween(durationMillis = 800, easing = LinearEasing),
        label = "refreshAnim"
    )

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        val isCompactHeight = maxHeight < 700.dp
        val isNarrowWidth = maxWidth < 380.dp

        // Fullscreen Mountain Slope Background
        Image(
            painter = painterResource(id = R.drawable.bg_mountain_slope),
            contentDescription = "Himalayan mountain slope terrain with active telemetry",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Subtle gradient vignette overlay to ensure text contrast
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xCC09140F),
                            Color(0x33000000),
                            Color(0x66000000),
                            Color(0xE608120D)
                        )
                    )
                )
        )

        // Interactive Telemetry Nodes & Telemetry Network Lines
        MountainSlopeTelemetryView(
            nodes = zone.nodes,
            selectedNodeId = selectedNode?.id,
            modifier = Modifier.fillMaxSize(),
            onNodeClick = { node ->
                onSelectNode(node)
            }
        )

        // UI Controls and Overlays
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .widthIn(max = 560.dp)
                .align(Alignment.Center)
                .padding(horizontal = if (isNarrowWidth) 12.dp else 16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top App Bar & Branding Section
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Utilities Row (Offline pill, Zone selector, Refresh)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Offline Indicator Pill
                    if (isOffline) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xD9B45309))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CloudOff,
                                    contentDescription = "Offline Mode",
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "OFFLINE",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    } else {
                        // Live Telemetry status
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(DarkGlassCard)
                                .border(1.dp, DarkGlassBorder, RoundedCornerShape(20.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(EmeraldAccent)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "LIVE TELEMETRY",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp,
                                    color = EmeraldAccent
                                )
                            }
                        }
                    }

                    // Zone Dropdown Selector & Refresh
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(DarkGlassCard)
                                    .border(1.dp, DarkGlassBorder, RoundedCornerShape(14.dp))
                                    .clickable { showZoneDropdown = true }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = "Select Zone",
                                        tint = EmeraldAccent,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = zone.name.substringBefore(" ("),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = showZoneDropdown,
                                onDismissRequest = { showZoneDropdown = false }
                            ) {
                                allZones.forEach { z ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .clip(CircleShape)
                                                        .background(z.riskLevel.color)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(z.name)
                                            }
                                        },
                                        onClick = {
                                            onSelectZone(z.id)
                                            showZoneDropdown = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Refresh button
                        IconButton(
                            onClick = {
                                rotationAngle += 360f
                                onRefresh()
                            },
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(DarkGlassCard)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh Telemetry",
                                tint = Color.White,
                                modifier = Modifier
                                    .size(18.dp)
                                    .rotate(animatedRotation)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(if (isCompactHeight) 4.dp else 12.dp))

                // Brand Mountain Logo
                MountainLogo(
                    size = if (isCompactHeight) 40.dp else 54.dp,
                    mountainColor = ForestGreenPrimary,
                    snowColor = Color.White
                )

                Spacer(modifier = Modifier.height(if (isCompactHeight) 2.dp else 4.dp))

                // LANDSLIDE Header
                Text(
                    text = strings.titleLandslide,
                    fontSize = if (isCompactHeight) 22.sp else 28.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    color = Color.White
                )

                Text(
                    text = strings.subtitleEarlyWarning,
                    fontSize = if (isCompactHeight) 11.sp else 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.5.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = strings.tagline,
                    fontSize = if (isCompactHeight) 11.sp else 13.sp,
                    fontWeight = FontWeight.Normal,
                    letterSpacing = 1.sp,
                    color = Color.White.copy(alpha = 0.75f)
                )
            }

            // Top-Left Floating HUD Card (matches Figma placement)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                contentAlignment = Alignment.TopStart
            ) {
                val normalCount = zone.nodes.count { it.status == com.example.data.model.RiskLevel.NORMAL }
                val watchCount = zone.nodes.count { it.status == com.example.data.model.RiskLevel.WATCH }
                val criticalCount = zone.nodes.count { it.status == com.example.data.model.RiskLevel.CRITICAL }

                RiskHUDCard(
                    riskLevel = zone.riskLevel,
                    riskScore = zone.riskScore,
                    normalCount = normalCount,
                    watchCount = watchCount,
                    criticalCount = criticalCount,
                    offlineCount = 0,
                    onClick = { onNavigateToZoneDetails(zone.id) }
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Lower Section: Prominent Alert Banner + Quick Metric Chips
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                // Landslide Detected Red Banner (Clickable to Alerts)
                if (zone.riskLevel == com.example.data.model.RiskLevel.CRITICAL) {
                    AlertBanner(
                        title = strings.landslideDetected,
                        timestamp = zone.lastUpdated,
                        onClick = onNavigateToAlerts
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Two Bottom Metric Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Left: Rainfall Chip
                    BottomMetricChip(
                        title = strings.heavyRainfall,
                        subtitle = "${zone.rainfallLastHourMm.toInt()} mm (last 1h)",
                        icon = Icons.Default.Thunderstorm,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToZoneDetails(zone.id) }
                    )

                    // Right: Region / Location Chip
                    BottomMetricChip(
                        title = strings.northEastRegion,
                        subtitle = "${zone.district}, ${zone.state.substringBefore(",")}",
                        icon = Icons.Default.LocationOn,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToMap
                    )
                }
            }
        }

        // Node Inspector Dialog when node is clicked
        if (selectedNode != null) {
            NodeDetailDialog(
                node = selectedNode,
                onDismiss = { onSelectNode(null) },
                onViewZoneDetails = {
                    onSelectNode(null)
                    onNavigateToZoneDetails(zone.id)
                }
            )
        }
    }
}
