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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.Traffic
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.IncidentReport
import com.example.data.model.IncidentType
import com.example.ui.theme.DarkCanvas
import com.example.ui.theme.DarkGlassBorder
import com.example.ui.theme.DarkGlassCard
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.RiskCritical
import com.example.ui.theme.RiskNormal
import com.example.ui.theme.RiskWatch

@Composable
fun CreateReportScreen(
    onBack: () -> Unit,
    onSubmitReport: (IncidentType, String, String, String, Double, Double, String?) -> IncidentReport
) {
    var selectedType by remember { mutableStateOf(IncidentType.ROCKFALL) }
    var locationName by remember { mutableStateOf("Barapani Slope, Shillong (NH-6)") }
    var description by remember {
        mutableStateOf("Noticed significant rock debris falling across northbound shoulder following intense hourly rainfall. Slope shows early fissure cracks.")
    }
    var title by remember { mutableStateOf("Fresh Rockfall Hazard near Barapani") }
    var hasPhotoAttached by remember { mutableStateOf(true) }
    var submittedReport by remember { mutableStateOf<IncidentReport?>(null) }

    val incidentTypes = listOf(
        Pair(IncidentType.GROUND_CRACKS, Icons.Default.Warning),
        Pair(IncidentType.ROCKFALL, Icons.Default.Landscape),
        Pair(IncidentType.ROAD_BLOCKAGE, Icons.Default.Traffic),
        Pair(IncidentType.HEAVY_RAINFALL, Icons.Default.Thunderstorm),
        Pair(IncidentType.SOIL_MOVEMENT, Icons.Default.Dangerous),
        Pair(IncidentType.OTHER, Icons.Default.Warning)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkCanvas)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(DarkGlassCard)
                        .border(1.dp, DarkGlassBorder, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Report Slope Hazard",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Broadcast ground observations to early warning registry",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.65f)
                    )
                }
            }

            // Form
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Incident Type Selector Grid
                Text(
                    text = "Select Incident Type",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(10.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TypeSelectorChip(
                            type = incidentTypes[0].first,
                            icon = incidentTypes[0].second,
                            isSelected = selectedType == incidentTypes[0].first,
                            modifier = Modifier.weight(1f),
                            onClick = { selectedType = incidentTypes[0].first }
                        )
                        TypeSelectorChip(
                            type = incidentTypes[1].first,
                            icon = incidentTypes[1].second,
                            isSelected = selectedType == incidentTypes[1].first,
                            modifier = Modifier.weight(1f),
                            onClick = { selectedType = incidentTypes[1].first }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TypeSelectorChip(
                            type = incidentTypes[2].first,
                            icon = incidentTypes[2].second,
                            isSelected = selectedType == incidentTypes[2].first,
                            modifier = Modifier.weight(1f),
                            onClick = { selectedType = incidentTypes[2].first }
                        )
                        TypeSelectorChip(
                            type = incidentTypes[3].first,
                            icon = incidentTypes[3].second,
                            isSelected = selectedType == incidentTypes[3].first,
                            modifier = Modifier.weight(1f),
                            onClick = { selectedType = incidentTypes[3].first }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TypeSelectorChip(
                            type = incidentTypes[4].first,
                            icon = incidentTypes[4].second,
                            isSelected = selectedType == incidentTypes[4].first,
                            modifier = Modifier.weight(1f),
                            onClick = { selectedType = incidentTypes[4].first }
                        )
                        TypeSelectorChip(
                            type = incidentTypes[5].first,
                            icon = incidentTypes[5].second,
                            isSelected = selectedType == incidentTypes[5].first,
                            modifier = Modifier.weight(1f),
                            onClick = { selectedType = incidentTypes[5].first }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Location Field
                Text(
                    text = "Incident Location",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = locationName,
                    onValueChange = { locationName = it },
                    leadingIcon = {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = EmeraldAccent)
                    },
                    trailingIcon = {
                        IconButton(onClick = { locationName = "GPS: 25.5788° N, 91.8933° E (Shillong)" }) {
                            Icon(Icons.Default.MyLocation, contentDescription = "Use GPS", tint = EmeraldAccent)
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldAccent,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        focusedContainerColor = DarkGlassCard,
                        unfocusedContainerColor = DarkGlassCard,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Title Field
                Text(
                    text = "Report Title",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldAccent,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        focusedContainerColor = DarkGlassCard,
                        unfocusedContainerColor = DarkGlassCard,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Description Field
                Text(
                    text = "Observations & Description",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    minLines = 3,
                    maxLines = 5,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EmeraldAccent,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        focusedContainerColor = DarkGlassCard,
                        unfocusedContainerColor = DarkGlassCard,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Photo Attachment Box
                Text(
                    text = "Photo Evidence",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(DarkGlassCard)
                        .border(1.dp, if (hasPhotoAttached) EmeraldAccent else DarkGlassBorder, RoundedCornerShape(14.dp))
                        .clickable { hasPhotoAttached = !hasPhotoAttached }
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (hasPhotoAttached) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = EmeraldAccent,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "slope_debris_inspection_01.jpg",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Attached (3.2 MB) • Tap to toggle",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.6f)
                                )
                            }
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = "Attach Photo",
                                tint = EmeraldAccent,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tap to attach camera photo or visual evidence",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(26.dp))

                // Submit Button
                Button(
                    onClick = {
                        val report = onSubmitReport(
                            selectedType,
                            title,
                            description,
                            locationName,
                            25.5788,
                            91.8933,
                            if (hasPhotoAttached) "slope_debris_inspection_01.jpg" else null
                        )
                        submittedReport = report
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text(
                        text = "Submit Local Incident Report",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF003822)
                    )
                }

                Spacer(modifier = Modifier.height(30.dp))
            }
        }

        // Submission Confirmation Dialog
        if (submittedReport != null) {
            val rep = submittedReport!!
            Dialog(onDismissRequest = { /* force action */ }) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0xFF0F1E17),
                    border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldAccent),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(EmeraldAccent.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = EmeraldAccent,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Report Submitted",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Registered as ${rep.id}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = EmeraldAccent
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(DarkSurfaceElevated)
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = "Status: ${rep.status.label}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = RiskWatch
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Your field report has been logged locally in the offline database and queued for verification by the Regional Geotechnical Response Unit.",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                submittedReport = null
                                onBack()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "View in My Reports",
                                color = Color(0xFF003822),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TypeSelectorChip(
    type: IncidentType,
    icon: ImageVector,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) EmeraldAccent.copy(alpha = 0.25f) else DarkGlassCard)
            .border(
                1.dp,
                if (isSelected) EmeraldAccent else DarkGlassBorder,
                RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) EmeraldAccent else Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = type.displayName,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) EmeraldAccent else Color.White
            )
        }
    }
}
