package com.example.ui.screens.alerts

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.AlertItem
import com.example.data.model.AlertSeverity
import com.example.ui.theme.AppTheme
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.RiskCritical
import com.example.ui.theme.RiskNormal
import com.example.ui.theme.RiskWatch

@Composable
fun AlertsScreen(
    alerts: List<AlertItem>,
    onTriggerNotification: (Context) -> Unit,
    onNavigateToZone: (String) -> Unit
) {
    val context = LocalContext.current
    var selectedSeverityFilter by remember { mutableStateOf<AlertSeverity?>(null) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Active Alerts, 1 = History
    var selectedAlert by remember { mutableStateOf<AlertItem?>(null) }

    val filteredAlerts = alerts.filter { alert ->
        val matchesSeverity = selectedSeverityFilter == null || alert.severity == selectedSeverityFilter
        val matchesTab = if (selectedTab == 0) {
            alert.severity == AlertSeverity.CRITICAL || alert.severity == AlertSeverity.HIGH
        } else true
        matchesSeverity && matchesTab
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.canvas)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Early Warning Alerts",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.colors.textPrimary
                    )
                    Text(
                        text = "Real-time geotechnical hazard notifications",
                        fontSize = 12.sp,
                        color = AppTheme.colors.textSecondary
                    )
                }

                // Simulate Notification Button
                Button(
                    onClick = { onTriggerNotification(context) },
                    colors = ButtonDefaults.buttonColors(containerColor = RiskCritical.copy(alpha = 0.15f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, RiskCritical),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Campaign,
                        contentDescription = null,
                        tint = RiskCritical,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Test Alert",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = RiskCritical
                    )
                }
            }

            // Tabs: Active Alerts vs Alert History
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = AppTheme.colors.surfaceElevated,
                contentColor = AppTheme.colors.accent,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            text = "Active Alerts (${alerts.count { it.severity == AlertSeverity.CRITICAL || it.severity == AlertSeverity.HIGH }})",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 0) AppTheme.colors.accent else AppTheme.colors.textSecondary
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            text = "All History (${alerts.size})",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 1) AppTheme.colors.accent else AppTheme.colors.textSecondary
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Severity Filter Pills
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        label = "All Severities",
                        isSelected = selectedSeverityFilter == null,
                        onClick = { selectedSeverityFilter = null }
                    )
                }
                item {
                    FilterChip(
                        label = "Critical",
                        isSelected = selectedSeverityFilter == AlertSeverity.CRITICAL,
                        color = RiskCritical,
                        onClick = { selectedSeverityFilter = AlertSeverity.CRITICAL }
                    )
                }
                item {
                    FilterChip(
                        label = "High",
                        isSelected = selectedSeverityFilter == AlertSeverity.HIGH,
                        color = Color(0xFFF97316),
                        onClick = { selectedSeverityFilter = AlertSeverity.HIGH }
                    )
                }
                item {
                    FilterChip(
                        label = "Moderate",
                        isSelected = selectedSeverityFilter == AlertSeverity.MODERATE,
                        color = RiskWatch,
                        onClick = { selectedSeverityFilter = AlertSeverity.MODERATE }
                    )
                }
                item {
                    FilterChip(
                        label = "Info",
                        isSelected = selectedSeverityFilter == AlertSeverity.INFORMATION,
                        color = RiskNormal,
                        onClick = { selectedSeverityFilter = AlertSeverity.INFORMATION }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Alerts List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredAlerts) { alert ->
                    AlertCard(
                        alert = alert,
                        onClick = { selectedAlert = alert }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }

        // Alert Detail Dialog
        if (selectedAlert != null) {
            val alert = selectedAlert!!
            Dialog(onDismissRequest = { selectedAlert = null }) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = AppTheme.colors.card,
                    border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.cardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .padding(20.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(alert.severity.color.copy(alpha = 0.2f))
                                    .border(1.dp, alert.severity.color, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = alert.severity.label,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = alert.severity.color
                                )
                            }
                            Text(
                                text = alert.timestamp,
                                fontSize = 11.sp,
                                color = AppTheme.colors.textTertiary
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = alert.title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.colors.textPrimary
                        )

                        Text(
                            text = alert.region,
                            fontSize = 13.sp,
                            color = AppTheme.colors.accent,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = alert.message,
                            fontSize = 13.sp,
                            color = AppTheme.colors.textPrimary,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Actionable Advice Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(AppTheme.colors.surfaceElevated)
                                .border(1.dp, AppTheme.colors.cardBorder, RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = "SAFETY DIRECTIVE",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RiskWatch
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = alert.actionableAdvice,
                                    fontSize = 12.sp,
                                    color = AppTheme.colors.textPrimary,
                                    lineHeight = 16.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { selectedAlert = null },
                                colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.surfaceElevated),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Dismiss", color = AppTheme.colors.textPrimary)
                            }

                            if (alert.zoneId != null) {
                                Button(
                                    onClick = {
                                        val zid = alert.zoneId
                                        selectedAlert = null
                                        onNavigateToZone(zid)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.accent),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1.2f)
                                ) {
                                    Text("View Zone", color = AppTheme.colors.onAccent, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AlertCard(
    alert: AlertItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.card),
        border = androidx.compose.foundation.BorderStroke(1.dp, alert.severity.color.copy(alpha = 0.45f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(alert.severity.color.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = alert.severity.label,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = alert.severity.color
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = alert.region,
                        fontSize = 12.sp,
                        color = AppTheme.colors.textSecondary
                    )
                }

                Text(
                    text = alert.timestamp.substringAfter(", "),
                    fontSize = 11.sp,
                    color = AppTheme.colors.textTertiary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = alert.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.textPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = alert.message,
                fontSize = 12.sp,
                color = AppTheme.colors.textSecondary,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tap to view instructions",
                    fontSize = 11.sp,
                    color = AppTheme.colors.accent,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = null,
                    tint = AppTheme.colors.textTertiary,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

@Composable
private fun FilterChip(
    label: String,
    isSelected: Boolean,
    color: Color = EmeraldAccent,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) color.copy(alpha = 0.22f) else AppTheme.colors.card)
            .border(
                1.dp,
                if (isSelected) color else AppTheme.colors.cardBorder,
                RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) color else AppTheme.colors.textSecondary
        )
    }
}
