package com.example.ui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.data.model.AlertItem
import com.example.data.model.IncidentReport
import com.example.data.model.IncidentType
import com.example.data.model.SensorNode
import com.example.data.model.ZoneRiskData
import com.example.data.repository.LandslideRepository
import com.example.data.repository.UserPreferencesRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LandslideViewModel(
    private val repository: LandslideRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val zones: StateFlow<List<ZoneRiskData>> = repository.zones
    val allZones: StateFlow<List<ZoneRiskData>> = repository.zones
    val selectedZoneId: StateFlow<String> = repository.selectedZoneId
    val alerts: StateFlow<List<AlertItem>> = repository.alerts
    val reports: StateFlow<List<IncidentReport>> = repository.reports
    val isOfflineMode: StateFlow<Boolean> = repository.isOfflineMode
    val isSyncing: StateFlow<Boolean> = repository.isSyncing
    val syncProgress: StateFlow<Float> = repository.syncProgress

    val isOnboardingCompleted: StateFlow<Boolean> = preferencesRepository.isOnboardingCompleted
    val selectedLanguage: StateFlow<String> = preferencesRepository.selectedLanguage
    val currentLanguage: StateFlow<String> = preferencesRepository.selectedLanguage
    val notificationsEnabled: StateFlow<Boolean> = preferencesRepository.notificationsEnabled
    val isDarkTheme: StateFlow<Boolean> = preferencesRepository.isDarkTheme

    private val _userProfile = MutableStateFlow(com.example.data.model.UserProfile())
    val userProfile: StateFlow<com.example.data.model.UserProfile> = _userProfile.asStateFlow()

    private val _currentScenario = MutableStateFlow(com.example.data.model.DemoScenario.LANDSLIDE_CRITICAL)
    val currentScenario: StateFlow<com.example.data.model.DemoScenario> = _currentScenario.asStateFlow()

    private val _selectedNode = MutableStateFlow<SensorNode?>(null)
    val selectedNode: StateFlow<SensorNode?> = _selectedNode.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    val currentZone: StateFlow<ZoneRiskData> = combine(zones, selectedZoneId) { zonesList, zoneId ->
        zonesList.find { it.id == zoneId } ?: zonesList.first()
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        repository.getSelectedZone()
    )

    fun selectZone(zoneId: String) {
        repository.selectZone(zoneId)
        _selectedNode.value = null
    }

    fun selectNode(node: SensorNode?) {
        _selectedNode.value = node
    }

    fun refreshTelemetry() {
        viewModelScope.launch {
            _isRefreshing.value = true
            delay(800)
            repository.refreshTelemetry()
            _isRefreshing.value = false
        }
    }

    fun completeOnboarding() {
        preferencesRepository.setOnboardingCompleted(true)
    }

    fun resetOnboarding() {
        preferencesRepository.setOnboardingCompleted(false)
    }

    fun submitReport(
        type: IncidentType,
        title: String,
        description: String,
        locationName: String,
        latitude: Double,
        longitude: Double,
        photoUri: String? = null
    ): IncidentReport {
        return repository.submitReport(
            type = type,
            title = title,
            description = description,
            locationName = locationName,
            latitude = latitude,
            longitude = longitude,
            photoUri = photoUri
        )
    }

    fun toggleOfflineMode() {
        repository.setOfflineMode(!isOfflineMode.value)
    }

    fun triggerSync(onComplete: () -> Unit = {}) {
        repository.triggerLocalSync(
            onProgress = { /* updated via StateFlow */ },
            onComplete = onComplete
        )
    }

    fun setLanguage(langCode: String) {
        preferencesRepository.setLanguage(langCode)
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        preferencesRepository.setNotificationsEnabled(enabled)
    }

    fun setDarkTheme(isDark: Boolean) {
        preferencesRepository.setDarkTheme(isDark)
    }

    fun toggleNotifications(enabled: Boolean) {
        preferencesRepository.setNotificationsEnabled(enabled)
    }

    fun refreshData() {
        refreshTelemetry()
    }

    fun simulateSyncOfflineReports() {
        triggerSync()
    }

    fun setScenario(scenario: com.example.data.model.DemoScenario) {
        _currentScenario.value = scenario
        val targetZoneId = when (scenario) {
            com.example.data.model.DemoScenario.NORMAL_LOW_RISK -> "zone_itanagar"
            com.example.data.model.DemoScenario.HEAVY_RAIN_WATCH -> "zone_cherrapunji"
            com.example.data.model.DemoScenario.LANDSLIDE_CRITICAL -> "zone_shillong"
        }
        selectZone(targetZoneId)
    }

    fun triggerDemoNotification(context: Context) {
        val channelId = "landslide_alerts"
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Landslide Early Warnings",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Emergency landslide early warning telemetry alerts"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.stat_sys_warning)
            .setContentTitle("⚠️ CRITICAL LANDSLIDE ALERT")
            .setContentText("Shillong Ridge Node 06 is at CRITICAL risk (84/100). Movement: 4.8mm/hr.")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Shillong Ridge Node 06 is currently at CRITICAL risk (84/100). Extreme hourly rainfall of 32mm detected. Evacuate designated downslope perimeter immediately.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        try {
            notificationManager.notify(1001, notification)
        } catch (_: SecurityException) {
            // Handled gracefully if notification permission not granted
        }
    }
}
