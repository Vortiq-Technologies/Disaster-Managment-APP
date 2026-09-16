package com.example.ui.screens.reports

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.IncidentReport
import com.example.data.model.ReportStatus
import com.example.data.model.SyncState
import com.example.ui.theme.AppTheme
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.RiskNormal
import com.example.ui.theme.RiskWatch

@Composable
fun ReportsScreen(
    reports: List<IncidentReport>,
    isOffline: Boolean,
    isSyncing: Boolean,
    syncProgress: Float,
    onTriggerSync: () -> Unit,
    onCreateReportClick: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = All, 1 = Under Review, 2 = Verified, 3 = Resolved
    var showSyncModal by remember { mutableStateOf(false) }

    val filteredReports = reports.filter { r ->
        when (selectedTab) {
            1 -> r.status == ReportStatus.PENDING
            2 -> r.status == ReportStatus.VERIFIED
            3 -> r.status == ReportStatus.RESOLVED
            else -> true
        }
    }

    val pendingSyncCount = reports.count { it.syncState == SyncState.PENDING }

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
            // Header with Sync Badge
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Field Incident Reports",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.colors.textPrimary
                    )
                    Text(
                        text = "Community & sensor hazard reports",
                        fontSize = 12.sp,
                        color = AppTheme.colors.textSecondary
                    )
                }

                // Sync Status Pill Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (pendingSyncCount > 0) Color(0x33F59E0B) else Color(0x3310B981))
                        .border(
                            1.dp,
                            if (pendingSyncCount > 0) RiskWatch else EmeraldAccent,
                            RoundedCornerShape(16.dp)
                        )
                        .clickable { showSyncModal = true }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (pendingSyncCount > 0) Icons.Default.CloudUpload else Icons.Default.CloudDone,
                            contentDescription = "Sync",
                            tint = if (pendingSyncCount > 0) RiskWatch else EmeraldAccent,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (pendingSyncCount > 0) "$pendingSyncCount Pending" else "All Synced",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (pendingSyncCount > 0) RiskWatch else EmeraldAccent
                        )
                    }
                }
            }

            // Tabs
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
                            text = "All (${reports.size})",
                            fontSize = 12.sp,
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
                            text = "Review",
                            fontSize = 12.sp,
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 1) AppTheme.colors.accent else AppTheme.colors.textSecondary
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = {
                        Text(
                            text = "Verified",
                            fontSize = 12.sp,
                            fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 2) AppTheme.colors.accent else AppTheme.colors.textSecondary
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    text = {
                        Text(
                            text = "Resolved",
                            fontSize = 12.sp,
                            fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 3) AppTheme.colors.accent else AppTheme.colors.textSecondary
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Reports List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredReports) { report ->
                    ReportCard(report = report)
                }

                item {
                    Spacer(modifier = Modifier.height(88.dp))
                }
            }
        }

        // Floating Action Button to Report Incident
        FloatingActionButton(
            onClick = onCreateReportClick,
            containerColor = AppTheme.colors.accent,
            contentColor = AppTheme.colors.onAccent,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 24.dp, end = 20.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Report")
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Report Incident",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }

        // Sync Modal Dialog
        if (showSyncModal) {
            Dialog(onDismissRequest = { showSyncModal = false }) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = AppTheme.colors.card,
                    border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.cardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = null,
                                tint = AppTheme.colors.accent,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Offline Data & Sync Queue",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppTheme.colors.textPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Reports submitted without internet connectivity are securely stored in the local device vault and queued for automatic relay to disaster headquarters.",
                            fontSize = 12.sp,
                            color = AppTheme.colors.textSecondary,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(AppTheme.colors.surfaceElevated)
                                .padding(12.dp)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Queue Status",
                                        fontSize = 12.sp,
                                        color = AppTheme.colors.textSecondary
                                    )
                                    Text(
                                        text = if (isSyncing) "Syncing..." else if (pendingSyncCount > 0) "Pending ($pendingSyncCount)" else "Fully Synced",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (pendingSyncCount > 0) RiskWatch else RiskNormal
                                    )
                                }

                                if (isSyncing) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    LinearProgressIndicator(
                                        progress = { syncProgress },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp)),
                                        color = AppTheme.colors.accent,
                                        trackColor = AppTheme.colors.cardBorder,
                                        strokeCap = StrokeCap.Round
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { showSyncModal = false },
                                colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.surfaceElevated),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Close", color = AppTheme.colors.textPrimary)
                            }

                            Button(
                                onClick = {
                                    onTriggerSync()
                                },
                                enabled = !isSyncing,
                                colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.accent),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1.3f)
                            ) {
                                Text(
                                    text = if (isSyncing) "Relaying..." else "Simulate Sync",
                                    color = AppTheme.colors.onAccent,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportCard(report: IncidentReport) {
    val statusColor = when (report.status) {
        ReportStatus.PENDING -> RiskWatch
        ReportStatus.VERIFIED -> EmeraldAccent
        ReportStatus.RESOLVED -> Color(0xFF38BDF8)
    }

    val syncIcon = when (report.syncState) {
        SyncState.PENDING -> Icons.Default.CloudUpload
        SyncState.SYNCING -> Icons.Default.Sync
        SyncState.SYNCED -> Icons.Default.CloudDone
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.card),
        border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.cardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = report.id,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = AppTheme.colors.accent
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(AppTheme.colors.surfaceElevated)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = report.incidentType.displayName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = AppTheme.colors.textSecondary
                        )
                    }
                }

                // Sync status indicator icon
                Icon(
                    imageVector = syncIcon,
                    contentDescription = report.syncState.name,
                    tint = if (report.syncState == SyncState.SYNCED) AppTheme.colors.accent else RiskWatch,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = report.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.textPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = report.description,
                fontSize = 12.sp,
                color = AppTheme.colors.textSecondary,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = AppTheme.colors.textTertiary,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = report.locationName,
                        fontSize = 11.sp,
                        color = AppTheme.colors.textSecondary
                    )
                }

                // Status pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(statusColor.copy(alpha = 0.18f))
                        .border(1.dp, statusColor, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = report.status.label,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Reporter: ${report.reporterName}",
                    fontSize = 10.sp,
                    color = AppTheme.colors.textTertiary
                )
                Text(
                    text = report.timestamp,
                    fontSize = 10.sp,
                    color = AppTheme.colors.textTertiary
                )
            }
        }
    }
}
