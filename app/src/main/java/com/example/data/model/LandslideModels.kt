package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.RiskCritical
import com.example.ui.theme.RiskCriticalBg
import com.example.ui.theme.RiskNormal
import com.example.ui.theme.RiskNormalBg
import com.example.ui.theme.RiskOffline
import com.example.ui.theme.RiskOfflineBg
import com.example.ui.theme.RiskWatch
import com.example.ui.theme.RiskWatchBg

enum class RiskLevel(
    val label: String,
    val color: Color,
    val bgColor: Color
) {
    NORMAL("Normal", RiskNormal, RiskNormalBg),
    WATCH("Watch", RiskWatch, RiskWatchBg),
    CRITICAL("Critical", RiskCritical, RiskCriticalBg),
    OFFLINE("Offline", RiskOffline, RiskOfflineBg);

    companion object {
        fun fromScore(score: Int): RiskLevel {
            return when {
                score >= 70 -> CRITICAL
                score >= 40 -> WATCH
                score >= 0 -> NORMAL
                else -> OFFLINE
            }
        }
    }
}

data class SensorNode(
    val id: String,
    val name: String,
    val status: RiskLevel,
    val xPercent: Float, // Relative position on the mountain slope map [0..1]
    val yPercent: Float,
    val latitude: Double,
    val longitude: Double,
    val soilMoisture: Int, // %
    val slopeTiltDeg: Float,
    val poreWaterPressureKpa: Float,
    val displacementMmPerHour: Float,
    val batteryPercent: Int = 94,
    val signalDbm: Int = -68,
    val lastUpdated: String = "Just now"
)

data class ShapFactor(
    val featureName: String,
    val contributionPercent: Int,
    val isPositiveRisk: Boolean, // increases risk if true
    val description: String,
    val measuredValue: String
)

data class ZoneRiskData(
    val id: String,
    val name: String,
    val district: String,
    val state: String,
    val riskScore: Int,
    val riskLevel: RiskLevel,
    val probabilityPercent: Int,
    val rainfallLastHourMm: Float,
    val rainfall24hMm: Float,
    val soilMoisturePercent: Int,
    val temperatureC: Float,
    val humidityPercent: Int,
    val slopeMovementMm: Float,
    val activeSensorsCount: Int,
    val totalSensorsCount: Int,
    val lastUpdated: String,
    val latitude: Double,
    val longitude: Double,
    val riskFactors: List<String>,
    val shapExplanation: List<ShapFactor>,
    val nodes: List<SensorNode>
)

enum class IncidentType(val displayName: String, val iconName: String) {
    GROUND_CRACKS("Ground Cracks", "cracks"),
    ROCKFALL("Rockfall", "rockfall"),
    ROAD_BLOCKAGE("Road Blockage", "blockage"),
    HEAVY_RAINFALL("Heavy Rainfall", "rainfall"),
    SOIL_MOVEMENT("Soil Movement", "soil"),
    OTHER("Other Hazard", "other")
}

enum class ReportStatus(val label: String) {
    PENDING("Under Review"),
    VERIFIED("Verified by NDRF"),
    RESOLVED("Resolved & Cleared")
}

enum class SyncState {
    PENDING,
    SYNCING,
    SYNCED
}

data class IncidentReport(
    val id: String,
    val incidentType: IncidentType,
    val title: String,
    val description: String,
    val locationName: String,
    val latitude: Double,
    val longitude: Double,
    val photoUri: String? = null,
    val timestamp: String,
    val status: ReportStatus = ReportStatus.PENDING,
    val syncState: SyncState = SyncState.PENDING,
    val reporterName: String = "Dr. Priya Sharma"
)

enum class AlertSeverity(val label: String, val color: Color) {
    CRITICAL("CRITICAL", RiskCritical),
    HIGH("HIGH", RiskCritical),
    MODERATE("MODERATE", RiskWatch),
    INFORMATION("INFO", RiskNormal)
}

enum class DemoScenario(val title: String, val description: String) {
    NORMAL_LOW_RISK("Normal Weather / Low Risk", "Score 18/100 • Stable terrain"),
    HEAVY_RAIN_WATCH("Heavy Rainfall / Watch", "Score 55/100 • Elevated moisture"),
    LANDSLIDE_CRITICAL("Landslide Detected / Critical", "Score 84/100 • Critical movement detected")
}

data class AlertItem(
    val id: String,
    val title: String,
    val region: String,
    val severity: AlertSeverity,
    val message: String,
    val timestamp: String,
    val actionableAdvice: String,
    val isRead: Boolean = false,
    val zoneId: String? = null
)

data class UserProfile(
    val name: String = "Dr. Priya Sharma",
    val title: String = "Lead Geologist & Early Warning Officer",
    val department: String = "State Disaster Management Authority (SDMA)",
    val region: String = "East Khasi Hills, Meghalaya",
    val email: String = "priya.sharma@sdma.gov.in",
    val phone: String = "+91 98765 43210",
    val officerId: String = "SDMA-IND-8842"
)
