package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CrisisAlert
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.model.DemoScenario
import com.example.data.model.RiskLevel
import com.example.ui.components.AppStrings
import com.example.ui.screens.alerts.AlertsScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.map.MapScreen
import com.example.ui.screens.onboarding.OnboardingScreen
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.screens.profile.SafetyGuidelinesScreen
import com.example.ui.screens.reports.CreateReportScreen
import com.example.ui.screens.reports.ReportsScreen
import com.example.ui.screens.zonedetails.ZoneDetailsScreen
import com.example.ui.theme.DarkGlassBorder
import com.example.ui.theme.DarkGlassCard
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.ForestGreenDark
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.RiskCritical
import com.example.ui.theme.RiskNormal
import com.example.ui.theme.RiskWatch

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Onboarding : Screen("onboarding", "Welcome", Icons.Default.Explore)
    data object Home : Screen("home", "Home", Icons.Default.Home)
    data object Map : Screen("map", "Map", Icons.Default.Map)
    data object Alerts : Screen("alerts", "Alerts", Icons.Default.Notifications)
    data object Reports : Screen("reports", "Reports", Icons.Default.Assignment)
    data object Profile : Screen("profile", "Profile", Icons.Default.Person)
    data object ZoneDetails : Screen("zone_details/{zoneId}", "Zone Details", Icons.Default.Explore) {
        fun createRoute(zoneId: String) = "zone_details/$zoneId"
    }
    data object CreateReport : Screen("create_report", "Create Report", Icons.Default.Assignment)
    data object SafetyGuidelines : Screen("safety_guidelines", "Safety Directives", Icons.Default.Warning)
}

@Composable
fun LandslideApp(
    viewModel: LandslideViewModel
) {
    val navController = rememberNavController()
    val context = LocalContext.current

    val isOnboardingCompleted by viewModel.isOnboardingCompleted.collectAsState()
    val isOffline by viewModel.isOfflineMode.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val currentLanguage by viewModel.currentLanguage.collectAsState()
    val currentScenario by viewModel.currentScenario.collectAsState()

    val currentZone by viewModel.currentZone.collectAsState()
    val allZones by viewModel.allZones.collectAsState()
    val selectedNode by viewModel.selectedNode.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    val alerts by viewModel.alerts.collectAsState()
    val reports by viewModel.reports.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

    val isSyncing by viewModel.isSyncing.collectAsState()
    val syncProgress by viewModel.syncProgress.collectAsState()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    var showScenarioDialog by remember { mutableStateOf(false) }

    val showBottomBar = currentRoute in listOf(
        Screen.Home.route,
        Screen.Map.route,
        Screen.Alerts.route,
        Screen.Reports.route,
        Screen.Profile.route
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                LandslideBottomNavigation(
                    currentRoute = currentRoute,
                    alertCount = alerts.count { it.severity == com.example.data.model.AlertSeverity.CRITICAL },
                    currentLanguage = currentLanguage,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            // Demo Scenario Switcher Floating Button (available across main tabs)
            if (showBottomBar) {
                FloatingActionButton(
                    onClick = { showScenarioDialog = true },
                    containerColor = Color(0xFF10261D),
                    contentColor = EmeraldAccent,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .size(46.dp)
                        .border(1.dp, EmeraldAccent.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.Science,
                        contentDescription = "Demo Scenarios",
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (isOnboardingCompleted) Screen.Home.route else Screen.Onboarding.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Onboarding / Sign In Screen
            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    currentLanguage = currentLanguage,
                    onProceedToApp = {
                        viewModel.completeOnboarding()
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }

            // Home Screen (Full bleed telemetry slope matching screenshot)
            composable(Screen.Home.route) {
                HomeScreen(
                    zone = currentZone,
                    allZones = allZones,
                    selectedNode = selectedNode,
                    isRefreshing = isRefreshing,
                    isOffline = isOffline,
                    currentLanguage = currentLanguage,
                    onRefresh = { viewModel.refreshData() },
                    onSelectZone = { viewModel.selectZone(it) },
                    onSelectNode = { viewModel.selectNode(it) },
                    onNavigateToZoneDetails = { zoneId ->
                        navController.navigate(Screen.ZoneDetails.createRoute(zoneId))
                    },
                    onNavigateToAlerts = {
                        navController.navigate(Screen.Alerts.route)
                    },
                    onNavigateToMap = {
                        navController.navigate(Screen.Map.route)
                    }
                )
            }

            // Map Screen
            composable(Screen.Map.route) {
                MapScreen(
                    zones = allZones,
                    selectedZoneId = currentZone.id,
                    onSelectZone = { viewModel.selectZone(it) },
                    onNavigateToZoneDetails = { zoneId ->
                        navController.navigate(Screen.ZoneDetails.createRoute(zoneId))
                    }
                )
            }

            // Alerts Screen
            composable(Screen.Alerts.route) {
                AlertsScreen(
                    alerts = alerts,
                    onTriggerNotification = { ctx ->
                        viewModel.triggerDemoNotification(ctx)
                    },
                    onNavigateToZone = { zoneId ->
                        viewModel.selectZone(zoneId)
                        navController.navigate(Screen.ZoneDetails.createRoute(zoneId))
                    }
                )
            }

            // Reports Screen
            composable(Screen.Reports.route) {
                ReportsScreen(
                    reports = reports,
                    isOffline = isOffline,
                    isSyncing = isSyncing,
                    syncProgress = syncProgress,
                    onTriggerSync = { viewModel.simulateSyncOfflineReports() },
                    onCreateReportClick = {
                        navController.navigate(Screen.CreateReport.route)
                    }
                )
            }

            // Create Report Screen
            composable(Screen.CreateReport.route) {
                CreateReportScreen(
                    onBack = { navController.popBackStack() },
                    onSubmitReport = { type, title, desc, loc, lat, lon, photo ->
                        viewModel.submitReport(type, title, desc, loc, lat, lon, photo)
                    }
                )
            }

            // Profile Screen
            composable(Screen.Profile.route) {
                ProfileScreen(
                    userProfile = userProfile,
                    isOffline = isOffline,
                    notificationsEnabled = notificationsEnabled,
                    currentLanguage = currentLanguage,
                    onToggleOffline = { viewModel.toggleOfflineMode() },
                    onToggleNotifications = { viewModel.toggleNotifications(it) },
                    onSelectLanguage = { viewModel.setLanguage(it) },
                    onOpenSafetyGuidelines = {
                        navController.navigate(Screen.SafetyGuidelines.route)
                    },
                    onResetOnboarding = {
                        viewModel.resetOnboarding()
                        navController.navigate(Screen.Onboarding.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            // Zone Details Screen
            composable(
                route = Screen.ZoneDetails.route,
                arguments = listOf(navArgument("zoneId") { type = NavType.StringType })
            ) { backStackEntry ->
                val zoneId = backStackEntry.arguments?.getString("zoneId") ?: currentZone.id
                val zone = allZones.find { it.id == zoneId } ?: currentZone
                ZoneDetailsScreen(
                    zone = zone,
                    onBack = { navController.popBackStack() }
                )
            }

            // Safety Directives Screen
            composable(Screen.SafetyGuidelines.route) {
                SafetyGuidelinesScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }

        // Demo Scenario Dialog
        if (showScenarioDialog) {
            Dialog(onDismissRequest = { showScenarioDialog = false }) {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = Color(0xFF10211A),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkGlassBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .padding(20.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Science,
                                contentDescription = null,
                                tint = EmeraldAccent,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Demo Simulation Lab",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Instantly switch environment telemetry scenarios",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.65f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Scenario A: Normal / Low Risk
                        ScenarioOptionCard(
                            title = "Scenario A: Normal Weather",
                            subtitle = "Low risk (18/100) • Light breeze • Stable slope shear",
                            level = RiskLevel.NORMAL,
                            isSelected = currentScenario == DemoScenario.NORMAL_LOW_RISK,
                            onClick = {
                                viewModel.setScenario(DemoScenario.NORMAL_LOW_RISK)
                                showScenarioDialog = false
                            }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Scenario B: Heavy Rain / Watch
                        ScenarioOptionCard(
                            title = "Scenario B: Heavy Rainfall",
                            subtitle = "Watch level (55/100) • 18mm/h rain • Rising pore pressure",
                            level = RiskLevel.WATCH,
                            isSelected = currentScenario == DemoScenario.HEAVY_RAIN_WATCH,
                            onClick = {
                                viewModel.setScenario(DemoScenario.HEAVY_RAIN_WATCH)
                                showScenarioDialog = false
                            }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Scenario C: Critical Landslide Detected
                        ScenarioOptionCard(
                            title = "Scenario C: Landslide Detected",
                            subtitle = "Critical emergency (84/100) • 32mm/h • Active rock creep",
                            level = RiskLevel.CRITICAL,
                            isSelected = currentScenario == DemoScenario.LANDSLIDE_CRITICAL,
                            onClick = {
                                viewModel.setScenario(DemoScenario.LANDSLIDE_CRITICAL)
                                showScenarioDialog = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ScenarioOptionCard(
    title: String,
    subtitle: String,
    level: RiskLevel,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = if (isSelected) Color(0xFF163226) else DarkGlassCard),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) EmeraldAccent else DarkGlassBorder
        )
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
                    .background(level.color)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun LandslideBottomNavigation(
    currentRoute: String?,
    alertCount: Int,
    currentLanguage: String,
    onNavigate: (String) -> Unit
) {
    val strings = AppStrings.get(currentLanguage)

    NavigationBar(
        containerColor = Color(0xF209140F),
        tonalElevation = 8.dp,
        modifier = Modifier.border(
            width = 1.dp,
            color = DarkGlassBorder,
            shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp)
        )
    ) {
        // Home
        NavigationBarItem(
            selected = currentRoute == Screen.Home.route,
            onClick = { onNavigate(Screen.Home.route) },
            icon = { Icon(Icons.Default.Home, contentDescription = strings.navHome) },
            label = { Text(strings.navHome, fontSize = 11.sp) },
            colors = navigationItemColors()
        )

        // Map
        NavigationBarItem(
            selected = currentRoute == Screen.Map.route,
            onClick = { onNavigate(Screen.Map.route) },
            icon = { Icon(Icons.Default.Map, contentDescription = strings.navMap) },
            label = { Text(strings.navMap, fontSize = 11.sp) },
            colors = navigationItemColors()
        )

        // Alerts (with badge)
        NavigationBarItem(
            selected = currentRoute == Screen.Alerts.route,
            onClick = { onNavigate(Screen.Alerts.route) },
            icon = {
                if (alertCount > 0) {
                    BadgedBox(badge = { Badge { Text("$alertCount") } }) {
                        Icon(Icons.Default.Notifications, contentDescription = strings.navAlerts)
                    }
                } else {
                    Icon(Icons.Default.Notifications, contentDescription = strings.navAlerts)
                }
            },
            label = { Text(strings.navAlerts, fontSize = 11.sp) },
            colors = navigationItemColors()
        )

        // Reports
        NavigationBarItem(
            selected = currentRoute == Screen.Reports.route,
            onClick = { onNavigate(Screen.Reports.route) },
            icon = { Icon(Icons.Default.Assignment, contentDescription = strings.navReports) },
            label = { Text(strings.navReports, fontSize = 11.sp) },
            colors = navigationItemColors()
        )

        // Profile
        NavigationBarItem(
            selected = currentRoute == Screen.Profile.route,
            onClick = { onNavigate(Screen.Profile.route) },
            icon = { Icon(Icons.Default.Person, contentDescription = strings.navProfile) },
            label = { Text(strings.navProfile, fontSize = 11.sp) },
            colors = navigationItemColors()
        )
    }
}

@Composable
private fun navigationItemColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = Color(0xFF003822),
    selectedTextColor = EmeraldAccent,
    indicatorColor = EmeraldAccent,
    unselectedIconColor = Color.White.copy(alpha = 0.6f),
    unselectedTextColor = Color.White.copy(alpha = 0.6f)
)
