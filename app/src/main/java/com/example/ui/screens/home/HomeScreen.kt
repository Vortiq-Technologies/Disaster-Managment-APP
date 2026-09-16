package com.example.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.example.R
import com.example.data.model.SensorNode
import com.example.data.model.ZoneRiskData
import com.example.ui.components.AlertBanner
import com.example.ui.components.AppStrings
import com.example.ui.components.BottomMetricChip
import com.example.ui.components.MountainLogo
import com.example.ui.components.MountainSlopeTelemetryView
import com.example.ui.components.NodeDetailDialog
import com.example.ui.theme.AppTheme
import com.example.ui.theme.EmeraldAccent

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

    val coroutineScope = rememberCoroutineScope()

    // Smooth Animatable zoom and pan state for double-tap and pinch-to-zoom gestures
    val scaleAnim = remember { Animatable(1f) }
    val offsetXAnim = remember { Animatable(0f) }
    val offsetYAnim = remember { Animatable(0f) }

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
        val containerWidth = constraints.maxWidth.toFloat()
        val containerHeight = constraints.maxHeight.toFloat()

        val transformableState = rememberTransformableState { zoomChange, panChange, _ ->
            val currentScale = scaleAnim.value
            val newScale = (currentScale * zoomChange).coerceIn(1f, 3.5f)
            val maxPanX = (containerWidth * (newScale - 1f)) / 2f
            val maxPanY = (containerHeight * (newScale - 1f)) / 2f
            coroutineScope.launch {
                scaleAnim.snapTo(newScale)
                val newX = if (newScale <= 1.01f) 0f else (offsetXAnim.value + panChange.x * newScale).coerceIn(-maxPanX, maxPanX)
                val newY = if (newScale <= 1.01f) 0f else (offsetYAnim.value + panChange.y * newScale).coerceIn(-maxPanY, maxPanY)
                offsetXAnim.snapTo(newX)
                offsetYAnim.snapTo(newY)
            }
        }

        // Zoomable & Pannable Layer with native mobile double-tap zoom & pinch gestures
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clipToBounds()
                .transformable(state = transformableState)
                .pointerInput(containerWidth, containerHeight) {
                    detectTapGestures(
                        onDoubleTap = { tapOffset ->
                            coroutineScope.launch {
                                if (scaleAnim.value > 1.2f) {
                                    // Smoothly reset back to 1.0x original view on double tap
                                    launch {
                                        scaleAnim.animateTo(
                                            targetValue = 1f,
                                            animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)
                                        )
                                    }
                                    launch {
                                        offsetXAnim.animateTo(
                                            targetValue = 0f,
                                            animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)
                                        )
                                    }
                                    launch {
                                        offsetYAnim.animateTo(
                                            targetValue = 0f,
                                            animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)
                                        )
                                    }
                                } else {
                                    // Double tap to zoom in smoothly centered on the tapped location
                                    val targetScale = 2.4f
                                    val maxPanX = (containerWidth * (targetScale - 1f)) / 2f
                                    val maxPanY = (containerHeight * (targetScale - 1f)) / 2f
                                    val targetOffsetX = ((containerWidth / 2f - tapOffset.x) * (targetScale - 1f))
                                        .coerceIn(-maxPanX, maxPanX)
                                    val targetOffsetY = ((containerHeight / 2f - tapOffset.y) * (targetScale - 1f))
                                        .coerceIn(-maxPanY, maxPanY)

                                    launch {
                                        scaleAnim.animateTo(
                                            targetValue = targetScale,
                                            animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)
                                        )
                                    }
                                    launch {
                                        offsetXAnim.animateTo(
                                            targetValue = targetOffsetX,
                                            animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)
                                        )
                                    }
                                    launch {
                                        offsetYAnim.animateTo(
                                            targetValue = targetOffsetY,
                                            animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)
                                        )
                                    }
                                }
                            }
                        }
                    )
                }
                .graphicsLayer {
                    scaleX = scaleAnim.value
                    scaleY = scaleAnim.value
                    translationX = offsetXAnim.value
                    translationY = offsetYAnim.value
                }
        ) {
            // Fullscreen Mountain Slope Background
            Image(
                painter = painterResource(id = R.drawable.bg_mountain_slope),
                contentDescription = "Himalayan mountain slope terrain with active telemetry",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Subtle gradient vignette overlay to ensure text and node contrast
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = if (AppTheme.colors.isDark) {
                                listOf(
                                    Color(0xCC09140F),
                                    Color(0x33000000),
                                    Color(0x66000000),
                                    Color(0xE608120D)
                                )
                            } else {
                                listOf(
                                    Color(0xAA081A12),
                                    Color(0x22000000),
                                    Color(0x44000000),
                                    Color(0xCC081A12)
                                )
                            }
                        )
                    )
            )

            // Interactive Telemetry Nodes & Telemetry Network Lines (zooms seamlessly with slope image)
            MountainSlopeTelemetryView(
                nodes = zone.nodes,
                selectedNodeId = selectedNode?.id,
                modifier = Modifier.fillMaxSize(),
                onNodeClick = { node ->
                    onSelectNode(node)
                }
            )
        }

        // Fixed UI Controls and Overlays
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
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
                                    .background(AppTheme.colors.card.copy(alpha = 0.85f))
                                    .border(1.dp, AppTheme.colors.cardBorder, RoundedCornerShape(20.dp))
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

                        // Subtle zoom indicator pill (appears only when zoomed in)
                        AnimatedVisibility(
                            visible = scaleAnim.value > 1.05f,
                            enter = fadeIn() + scaleIn(),
                            exit = fadeOut() + scaleOut()
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(AppTheme.colors.card.copy(alpha = 0.85f))
                                    .border(1.dp, AppTheme.colors.cardBorder, RoundedCornerShape(20.dp))
                                    .clickable {
                                        coroutineScope.launch {
                                            launch { scaleAnim.animateTo(1f, tween(300, easing = FastOutSlowInEasing)) }
                                            launch { offsetXAnim.animateTo(0f, tween(300, easing = FastOutSlowInEasing)) }
                                            launch { offsetYAnim.animateTo(0f, tween(300, easing = FastOutSlowInEasing)) }
                                        }
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = String.format(java.util.Locale.US, "%.1fx", scaleAnim.value),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AppTheme.colors.accent
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
                                    .background(AppTheme.colors.card.copy(alpha = 0.85f))
                                    .border(1.dp, AppTheme.colors.cardBorder, RoundedCornerShape(14.dp))
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
                                        color = AppTheme.colors.textPrimary
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
                                .background(AppTheme.colors.card.copy(alpha = 0.85f))
                                .border(1.dp, AppTheme.colors.cardBorder, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh Telemetry",
                                tint = AppTheme.colors.textPrimary,
                                modifier = Modifier
                                    .size(18.dp)
                                    .rotate(animatedRotation)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(if (isCompactHeight) 12.dp else 20.dp))

                // Brand Header Section
                MountainLogo(
                    size = if (isCompactHeight) 38.dp else 48.dp,
                    mountainColor = EmeraldAccent,
                    snowColor = Color.White
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = strings.titleLandslide,
                    fontSize = if (isCompactHeight) 24.sp else 30.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp,
                    color = Color.White
                )

                Text(
                    text = strings.subtitleEarlyWarning,
                    fontSize = if (isCompactHeight) 10.sp else 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 3.sp,
                    color = EmeraldAccent
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = strings.tagline,
                    fontSize = if (isCompactHeight) 11.sp else 13.sp,
                    fontWeight = FontWeight.Normal,
                    letterSpacing = 1.sp,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }

            // Spacious center area where the zoomable mountain slope and telemetry nodes shine
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
