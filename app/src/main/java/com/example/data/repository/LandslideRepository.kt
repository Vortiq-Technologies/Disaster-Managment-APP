package com.example.data.repository

import com.example.data.model.AlertItem
import com.example.data.model.AlertSeverity
import com.example.data.model.IncidentReport
import com.example.data.model.IncidentType
import com.example.data.model.ReportStatus
import com.example.data.model.RiskLevel
import com.example.data.model.SensorNode
import com.example.data.model.ShapFactor
import com.example.data.model.SyncState
import com.example.data.model.UserProfile
import com.example.data.model.ZoneRiskData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class LandslideRepository {

    private val _zones = MutableStateFlow<List<ZoneRiskData>>(createInitialZones())
    val zones: StateFlow<List<ZoneRiskData>> = _zones.asStateFlow()

    private val _selectedZoneId = MutableStateFlow<String>("zone_shillong")
    val selectedZoneId: StateFlow<String> = _selectedZoneId.asStateFlow()

    private val _alerts = MutableStateFlow<List<AlertItem>>(createInitialAlerts())
    val alerts: StateFlow<List<AlertItem>> = _alerts.asStateFlow()

    private val _reports = MutableStateFlow<List<IncidentReport>>(createInitialReports())
    val reports: StateFlow<List<IncidentReport>> = _reports.asStateFlow()

    private val _isOfflineMode = MutableStateFlow<Boolean>(false)
    val isOfflineMode: StateFlow<Boolean> = _isOfflineMode.asStateFlow()

    private val _isSyncing = MutableStateFlow<Boolean>(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _syncProgress = MutableStateFlow<Float>(1.0f)
    val syncProgress: StateFlow<Float> = _syncProgress.asStateFlow()

    val userProfile = UserProfile()

    fun selectZone(zoneId: String) {
        _selectedZoneId.value = zoneId
    }

    fun getSelectedZone(): ZoneRiskData {
        return _zones.value.find { it.id == _selectedZoneId.value } ?: _zones.value.first()
    }

    fun getZoneById(zoneId: String): ZoneRiskData? {
        return _zones.value.find { it.id == zoneId }
    }

    fun refreshTelemetry() {
        val currentTimeStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())
        _zones.update { currentList ->
            currentList.map { zone ->
                if (zone.id == _selectedZoneId.value) {
                    zone.copy(lastUpdated = currentTimeStr)
                } else zone
            }
        }
    }

    fun submitReport(
        type: IncidentType,
        title: String,
        description: String,
        locationName: String,
        latitude: Double = 25.5788,
        longitude: Double = 91.8933,
        photoUri: String? = null
    ): IncidentReport {
        val nextId = "#${1040 + _reports.value.size + 1}"
        val now = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())
        val newReport = IncidentReport(
            id = nextId,
            incidentType = type,
            title = title.ifBlank { "${type.displayName} in $locationName" },
            description = description,
            locationName = locationName,
            latitude = latitude,
            longitude = longitude,
            photoUri = photoUri,
            timestamp = now,
            status = ReportStatus.PENDING,
            syncState = if (_isOfflineMode.value) SyncState.PENDING else SyncState.SYNCED
        )
        _reports.update { listOf(newReport) + it }
        return newReport
    }

    fun triggerLocalSync(onProgress: (Float) -> Unit, onComplete: () -> Unit) {
        _isSyncing.value = true
        _syncProgress.value = 0.0f
        
        // Simulating progressive sync
        Thread {
            for (step in 1..10) {
                Thread.sleep(200)
                val progress = step / 10f
                _syncProgress.value = progress
                onProgress(progress)
            }
            _reports.update { list ->
                list.map { it.copy(syncState = SyncState.SYNCED) }
            }
            _isSyncing.value = false
            onComplete()
        }.start()
    }

    fun setOfflineMode(offline: Boolean) {
        _isOfflineMode.value = offline
    }

    fun markAlertAsRead(alertId: String) {
        _alerts.update { list ->
            list.map { if (it.id == alertId) it.copy(isRead = true) else it }
        }
    }

    companion object {
        private fun createInitialZones(): List<ZoneRiskData> {
            val shillongNodes = listOf(
                SensorNode(
                    id = "node_01",
                    name = "Node 01",
                    status = RiskLevel.NORMAL,
                    xPercent = 0.76f,
                    yPercent = 0.44f,
                    latitude = 25.5892,
                    longitude = 91.8970,
                    soilMoisture = 42,
                    slopeTiltDeg = 18.2f,
                    poreWaterPressureKpa = 12.4f,
                    displacementMmPerHour = 0.1f
                ),
                SensorNode(
                    id = "node_02",
                    name = "Node 02",
                    status = RiskLevel.NORMAL,
                    xPercent = 0.40f,
                    yPercent = 0.54f,
                    latitude = 25.5855,
                    longitude = 91.8910,
                    soilMoisture = 55,
                    slopeTiltDeg = 24.5f,
                    poreWaterPressureKpa = 18.1f,
                    displacementMmPerHour = 0.2f
                ),
                SensorNode(
                    id = "node_03",
                    name = "Node 03",
                    status = RiskLevel.WATCH,
                    xPercent = 0.70f,
                    yPercent = 0.58f,
                    latitude = 25.5820,
                    longitude = 91.8965,
                    soilMoisture = 78,
                    slopeTiltDeg = 36.8f,
                    poreWaterPressureKpa = 34.6f,
                    displacementMmPerHour = 1.4f
                ),
                SensorNode(
                    id = "node_04",
                    name = "Node 04",
                    status = RiskLevel.NORMAL,
                    xPercent = 0.14f,
                    yPercent = 0.70f,
                    latitude = 25.5780,
                    longitude = 91.8840,
                    soilMoisture = 48,
                    slopeTiltDeg = 15.1f,
                    poreWaterPressureKpa = 14.8f,
                    displacementMmPerHour = 0.0f
                ),
                SensorNode(
                    id = "node_05",
                    name = "Node 05",
                    status = RiskLevel.NORMAL,
                    xPercent = 0.16f,
                    yPercent = 0.87f,
                    latitude = 25.5720,
                    longitude = 91.8860,
                    soilMoisture = 51,
                    slopeTiltDeg = 19.3f,
                    poreWaterPressureKpa = 16.0f,
                    displacementMmPerHour = 0.1f
                ),
                SensorNode(
                    id = "node_06",
                    name = "Node 06",
                    status = RiskLevel.CRITICAL,
                    xPercent = 0.76f,
                    yPercent = 0.74f,
                    latitude = 25.5760,
                    longitude = 91.8990,
                    soilMoisture = 94,
                    slopeTiltDeg = 47.9f,
                    poreWaterPressureKpa = 62.4f,
                    displacementMmPerHour = 4.8f
                )
            )

            val shillongShap = listOf(
                ShapFactor(
                    featureName = "Extreme Hourly Rainfall",
                    contributionPercent = 38,
                    isPositiveRisk = true,
                    description = "Recent torrential rain exceeds infiltration capacity of upper soil strata.",
                    measuredValue = "32 mm/hr"
                ),
                ShapFactor(
                    featureName = "Pore Water Pressure Saturation",
                    contributionPercent = 26,
                    isPositiveRisk = true,
                    description = "Pore pressure in bedrock fissures reached critical instability threshold.",
                    measuredValue = "62.4 kPa"
                ),
                ShapFactor(
                    featureName = "High Slope Angle & Shear Stress",
                    contributionPercent = 18,
                    isPositiveRisk = true,
                    description = "Steep 48° declivity on vulnerable sedimentary shale formation.",
                    measuredValue = "47.9° tilt"
                ),
                ShapFactor(
                    featureName = "Active Creep Displacement",
                    contributionPercent = 12,
                    isPositiveRisk = true,
                    description = "Sub-surface extensometer telemetry registered rapid ground displacement.",
                    measuredValue = "4.8 mm/hr"
                ),
                ShapFactor(
                    featureName = "Dense Pine Canopy Interception",
                    contributionPercent = -10,
                    isPositiveRisk = false,
                    description = "Forest root network partially mitigates superficial shear plane collapse.",
                    measuredValue = "64% canopy"
                )
            )

            return listOf(
                ZoneRiskData(
                    id = "zone_shillong",
                    name = "Shillong Ridge (Barapani Slope)",
                    district = "East Khasi Hills",
                    state = "Meghalaya, India",
                    riskScore = 84,
                    riskLevel = RiskLevel.CRITICAL,
                    probabilityPercent = 88,
                    rainfallLastHourMm = 32.0f,
                    rainfall24hMm = 188.5f,
                    soilMoisturePercent = 92,
                    temperatureC = 19.4f,
                    humidityPercent = 96,
                    slopeMovementMm = 4.8f,
                    activeSensorsCount = 6,
                    totalSensorsCount = 6,
                    lastUpdated = "14 Aug 2026, 10:24 AM",
                    latitude = 25.5788,
                    longitude = 91.8933,
                    riskFactors = listOf(
                        "Extreme precipitation exceeding 30mm/hr",
                        "High pore water pressure on Node 06",
                        "Ground displacement of 4.8mm registered",
                        "Steep shale gradient with fracture lines"
                    ),
                    shapExplanation = shillongShap,
                    nodes = shillongNodes
                ),
                ZoneRiskData(
                    id = "zone_gangtok",
                    name = "Gangtok East Ridge (NH-10 Sector)",
                    district = "East Sikkim",
                    state = "Sikkim, India",
                    riskScore = 78,
                    riskLevel = RiskLevel.CRITICAL,
                    probabilityPercent = 81,
                    rainfallLastHourMm = 28.5f,
                    rainfall24hMm = 162.0f,
                    soilMoisturePercent = 88,
                    temperatureC = 16.8f,
                    humidityPercent = 94,
                    slopeMovementMm = 3.2f,
                    activeSensorsCount = 5,
                    totalSensorsCount = 6,
                    lastUpdated = "14 Aug 2026, 10:15 AM",
                    latitude = 27.3389,
                    longitude = 88.6065,
                    riskFactors = listOf(
                        "NH-10 toe-cutting erosion",
                        "Continuous monsoon seepage",
                        "Soil shear strength degradation"
                    ),
                    shapExplanation = listOf(
                        ShapFactor("Cumulative 24h Rainfall", 34, true, "Prolonged wetting front penetration", "162 mm"),
                        ShapFactor("Highway Toe Undercutting", 28, true, "Excavation weakened slope base", "High"),
                        ShapFactor("Soil Moisture Saturation", 20, true, "Near liquid limit state", "88%"),
                        ShapFactor("Terraced Drainage Ditches", -8, false, "Surface culverts diverting water", "Functional")
                    ),
                    nodes = shillongNodes
                ),
                ZoneRiskData(
                    id = "zone_cherrapunji",
                    name = "Cherrapunji (Sohra) Escarpment",
                    district = "East Khasi Hills",
                    state = "Meghalaya, India",
                    riskScore = 65,
                    riskLevel = RiskLevel.WATCH,
                    probabilityPercent = 64,
                    rainfallLastHourMm = 44.0f,
                    rainfall24hMm = 310.0f,
                    soilMoisturePercent = 82,
                    temperatureC = 18.2f,
                    humidityPercent = 98,
                    slopeMovementMm = 1.6f,
                    activeSensorsCount = 7,
                    totalSensorsCount = 8,
                    lastUpdated = "14 Aug 2026, 09:50 AM",
                    latitude = 25.2702,
                    longitude = 91.7323,
                    riskFactors = listOf(
                        "World-record torrential rainfall zone",
                        "Deep limestone canyon weathering",
                        "High runoff velocity"
                    ),
                    shapExplanation = listOf(
                        ShapFactor("Extreme Cumulative Rain", 42, true, "Heavy cloudburst runoff", "310 mm"),
                        ShapFactor("Limestone Karst Dissolution", 18, true, "Subsurface voids weakening foundation", "Moderate"),
                        ShapFactor("Bedrock Stability", -15, false, "Competent quartzitic bedrock", "Solid")
                    ),
                    nodes = shillongNodes
                ),
                ZoneRiskData(
                    id = "zone_aizawl",
                    name = "Aizawl Chite Valley Slope",
                    district = "Aizawl",
                    state = "Mizoram, India",
                    riskScore = 52,
                    riskLevel = RiskLevel.WATCH,
                    probabilityPercent = 50,
                    rainfallLastHourMm = 14.2f,
                    rainfall24hMm = 74.0f,
                    soilMoisturePercent = 71,
                    temperatureC = 22.1f,
                    humidityPercent = 86,
                    slopeMovementMm = 0.9f,
                    activeSensorsCount = 5,
                    totalSensorsCount = 5,
                    lastUpdated = "14 Aug 2026, 09:30 AM",
                    latitude = 23.7271,
                    longitude = 92.7176,
                    riskFactors = listOf(
                        "Urban hillside construction load",
                        "Moderate continuous drizzle",
                        "Soft siltstone bedding plane"
                    ),
                    shapExplanation = listOf(
                        ShapFactor("Slope Structural Surcharge", 28, true, "Building weight on vulnerable ridge", "Elevated"),
                        ShapFactor("Siltstone Weathering", 22, true, "Moisture softening rock layers", "Moderate"),
                        ShapFactor("Retaining Gabion Walls", -12, false, "Civil protection structures present", "Stable")
                    ),
                    nodes = shillongNodes
                ),
                ZoneRiskData(
                    id = "zone_kohima",
                    name = "Kohima Dzükou Foothill",
                    district = "Kohima",
                    state = "Nagaland, India",
                    riskScore = 44,
                    riskLevel = RiskLevel.WATCH,
                    probabilityPercent = 42,
                    rainfallLastHourMm = 11.0f,
                    rainfall24hMm = 58.0f,
                    soilMoisturePercent = 64,
                    temperatureC = 20.0f,
                    humidityPercent = 82,
                    slopeMovementMm = 0.4f,
                    activeSensorsCount = 6,
                    totalSensorsCount = 6,
                    lastUpdated = "14 Aug 2026, 09:10 AM",
                    latitude = 25.6701,
                    longitude = 94.1077,
                    riskFactors = listOf(
                        "Moderate rain on clayey loam soil",
                        "Stable vegetation buffer"
                    ),
                    shapExplanation = listOf(
                        ShapFactor("Clay Infiltration Swelling", 20, true, "Expansive clay absorbs moisture", "Moderate"),
                        ShapFactor("Forest Cover Anchor", -18, false, "Bamboo and pine root system", "Dense")
                    ),
                    nodes = shillongNodes
                ),
                ZoneRiskData(
                    id = "zone_itanagar",
                    name = "Itanagar Papum Pare Hillside",
                    district = "Papum Pare",
                    state = "Arunachal Pradesh, India",
                    riskScore = 26,
                    riskLevel = RiskLevel.NORMAL,
                    probabilityPercent = 22,
                    rainfallLastHourMm = 4.2f,
                    rainfall24hMm = 28.0f,
                    soilMoisturePercent = 48,
                    temperatureC = 24.5f,
                    humidityPercent = 75,
                    slopeMovementMm = 0.1f,
                    activeSensorsCount = 6,
                    totalSensorsCount = 6,
                    lastUpdated = "14 Aug 2026, 08:45 AM",
                    latitude = 27.0844,
                    longitude = 93.6053,
                    riskFactors = listOf("Stable geological baseline", "Normal rainfall levels"),
                    shapExplanation = listOf(
                        ShapFactor("Mild Weather", -25, false, "Low rainfall volume", "4.2 mm/hr"),
                        ShapFactor("Intact Virgin Flora", -20, false, "High soil cohesion", "Optimal")
                    ),
                    nodes = shillongNodes
                ),
                ZoneRiskData(
                    id = "zone_imphal",
                    name = "Imphal West Foothills",
                    district = "Imphal West",
                    state = "Manipur, India",
                    riskScore = 18,
                    riskLevel = RiskLevel.NORMAL,
                    probabilityPercent = 14,
                    rainfallLastHourMm = 1.0f,
                    rainfall24hMm = 12.0f,
                    soilMoisturePercent = 38,
                    temperatureC = 26.0f,
                    humidityPercent = 68,
                    slopeMovementMm = 0.0f,
                    activeSensorsCount = 4,
                    totalSensorsCount = 4,
                    lastUpdated = "14 Aug 2026, 08:30 AM",
                    latitude = 24.8170,
                    longitude = 93.9368,
                    riskFactors = listOf("Low risk profile", "Gentle topography"),
                    shapExplanation = listOf(
                        ShapFactor("Dry Season Profile", -30, false, "Moisture well below critical limit", "38%"),
                        ShapFactor("Gentle Slope Angle", -25, false, "Gradient < 14 degrees", "Gentle")
                    ),
                    nodes = shillongNodes
                ),
                ZoneRiskData(
                    id = "zone_guwahati",
                    name = "Guwahati (Kamrup Hills)",
                    district = "Kamrup Metropolitan",
                    state = "Assam, India",
                    riskScore = 36,
                    riskLevel = RiskLevel.NORMAL,
                    probabilityPercent = 32,
                    rainfallLastHourMm = 8.4f,
                    rainfall24hMm = 46.0f,
                    soilMoisturePercent = 58,
                    temperatureC = 28.2f,
                    humidityPercent = 84,
                    slopeMovementMm = 0.2f,
                    activeSensorsCount = 6,
                    totalSensorsCount = 6,
                    lastUpdated = "14 Aug 2026, 09:05 AM",
                    latitude = 26.1445,
                    longitude = 91.7362,
                    riskFactors = listOf("Hill cutting in peri-urban slopes", "Drainage saturation"),
                    shapExplanation = listOf(
                        ShapFactor("Urban Hill Slopes", 18, true, "Unregulated cutting along hillside", "Moderate"),
                        ShapFactor("Drainage Infiltration", 12, true, "Stormwater saturation", "Standard")
                    ),
                    nodes = shillongNodes
                )
            )
        }

        private fun createInitialAlerts(): List<AlertItem> {
            return listOf(
                AlertItem(
                    id = "alt_1",
                    title = "LANDSLIDE DETECTED",
                    region = "Shillong Ridge (Node 06 Sector)",
                    severity = AlertSeverity.CRITICAL,
                    message = "Telemetry Node 06 detected rapid slope slip of 4.8mm/hr with 94% moisture saturation following 32mm rainfall.",
                    timestamp = "14 Aug 2026, 10:24 AM",
                    actionableAdvice = "Evacuate downhill residences along Barapani slope immediately. Avoid NH-6 bypass. Emergency NDRF units notified.",
                    zoneId = "zone_shillong"
                ),
                AlertItem(
                    id = "alt_2",
                    title = "Severe Flash Flood & Debris Flow Risk",
                    region = "Gangtok East Ridge (NH-10)",
                    severity = AlertSeverity.CRITICAL,
                    message = "Heavy catchment precipitation triggered slope toe-shear instability on the main valley artery.",
                    timestamp = "14 Aug 2026, 10:15 AM",
                    actionableAdvice = "Halt vehicular transit on NH-10. Seek designated high-ground shelter. Report rockfall sightings via incident tab.",
                    zoneId = "zone_gangtok"
                ),
                AlertItem(
                    id = "alt_3",
                    title = "Torrential Rainfall Threshold Breached",
                    region = "Cherrapunji (Sohra)",
                    severity = AlertSeverity.HIGH,
                    message = "Rainfall recorded 310mm in last 24h. Subsurface soil saturation high on canyon edges.",
                    timestamp = "14 Aug 2026, 09:50 AM",
                    actionableAdvice = "Stay away from vertical escarpment viewpoints and waterfall walking paths.",
                    zoneId = "zone_cherrapunji"
                ),
                AlertItem(
                    id = "alt_4",
                    title = "Slope Creep Advisory",
                    region = "Aizawl Chite Valley",
                    severity = AlertSeverity.MODERATE,
                    message = "Extensometers show progressive slow movement on soft shale bedrock.",
                    timestamp = "14 Aug 2026, 09:30 AM",
                    actionableAdvice = "Monitor retaining walls for fresh tension cracks. Heavy trucks diverted to outer ring road.",
                    zoneId = "zone_aizawl"
                ),
                AlertItem(
                    id = "alt_5",
                    title = "Early Warning System Sensors Online",
                    region = "North East Regional Network",
                    severity = AlertSeverity.INFORMATION,
                    message = "All 39 IoT pore-pressure, tilt, and rain-gauge sensors successfully transmitting telemetry packets.",
                    timestamp = "14 Aug 2026, 08:00 AM",
                    actionableAdvice = "System operating at full reliability across Meghalaya, Sikkim, Nagaland, and Mizoram.",
                    zoneId = null
                )
            )
        }

        private fun createInitialReports(): List<IncidentReport> {
            return listOf(
                IncidentReport(
                    id = "#1042",
                    incidentType = IncidentType.ROCKFALL,
                    title = "Rockfall on Shillong-Guwahati Highway",
                    description = "Fist-to-boulder sized stones dislodged near km-marker 42 after torrential rain. One lane partially blocked.",
                    locationName = "Umiam Lake Viewpoint, Shillong",
                    latitude = 25.6542,
                    longitude = 91.9054,
                    photoUri = null,
                    timestamp = "14 Aug 2026, 09:45 AM",
                    status = ReportStatus.VERIFIED,
                    syncState = SyncState.SYNCED,
                    reporterName = "Dr. Priya Sharma"
                ),
                IncidentReport(
                    id = "#1041",
                    incidentType = IncidentType.GROUND_CRACKS,
                    title = "Tension cracks across hillside road",
                    description = "Noticed 4-inch deep longitudinal fissures expanding parallel to the steep slope edge over the last 3 hours.",
                    locationName = "Upper Shillong Peak Road",
                    latitude = 25.5412,
                    longitude = 91.8561,
                    photoUri = null,
                    timestamp = "14 Aug 2026, 08:15 AM",
                    status = ReportStatus.PENDING,
                    syncState = SyncState.SYNCED,
                    reporterName = "Field Team Alpha"
                ),
                IncidentReport(
                    id = "#1040",
                    incidentType = IncidentType.ROAD_BLOCKAGE,
                    title = "Mudslide debris cleared",
                    description = "Small earth slump covered rural access culvert. Cleared by local road maintenance crew.",
                    locationName = "Mawlynnong Link Road",
                    latitude = 25.2014,
                    longitude = 91.9172,
                    photoUri = null,
                    timestamp = "13 Aug 2026, 05:20 PM",
                    status = ReportStatus.RESOLVED,
                    syncState = SyncState.SYNCED,
                    reporterName = "Roads Dept Shillong"
                )
            )
        }
    }
}
