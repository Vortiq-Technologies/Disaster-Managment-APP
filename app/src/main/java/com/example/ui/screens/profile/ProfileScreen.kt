package com.example.ui.screens.profile

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
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.example.data.model.UserProfile
import com.example.ui.components.AppStrings
import com.example.ui.components.MountainLogo
import com.example.ui.theme.AppTheme
import com.example.ui.theme.DarkCanvas
import com.example.ui.theme.DarkGlassBorder
import com.example.ui.theme.DarkGlassCard
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.ForestGreenDark
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.RiskCritical
import com.example.ui.theme.RiskWatch

@Composable
fun ProfileScreen(
    userProfile: UserProfile,
    isOffline: Boolean,
    notificationsEnabled: Boolean,
    isDarkTheme: Boolean = true,
    currentLanguage: String,
    onToggleOffline: () -> Unit,
    onToggleNotifications: (Boolean) -> Unit,
    onToggleTheme: (Boolean) -> Unit = {},
    onSelectLanguage: (String) -> Unit,
    onOpenSafetyGuidelines: () -> Unit,
    onResetOnboarding: () -> Unit
) {
    val strings = AppStrings.get(currentLanguage)
    var showLanguageMenu by remember { mutableStateOf(false) }

    val languageNames = mapOf(
        "en" to "English (Default)",
        "hi" to "हिंदी (Hindi)",
        "bn" to "বাংলা (Bengali)",
        "as" to "অসমীয়া (Assamese)"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.canvas)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Header
            Text(
                text = "Officer Profile & Settings",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.textPrimary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // User Profile Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = AppTheme.colors.card),
                border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.cardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Profile Avatar
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(AppTheme.colors.accentContainer)
                            .border(2.dp, AppTheme.colors.accent, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "PS",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.colors.accent
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = userProfile.name,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.colors.textPrimary
                        )
                        Text(
                            text = userProfile.title,
                            fontSize = 12.sp,
                            color = AppTheme.colors.accent,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = userProfile.department,
                            fontSize = 11.sp,
                            color = AppTheme.colors.textSecondary
                        )
                        Text(
                            text = "ID: ${userProfile.officerId}",
                            fontSize = 10.sp,
                            color = AppTheme.colors.textTertiary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section: System Configuration & Operations
            Text(
                text = "App Settings & System Controls",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.accent
            )

            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = AppTheme.colors.card),
                border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.cardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Light Theme Mode Toggle (Requested feature)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(AppTheme.colors.accentContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (!isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                    contentDescription = "Theme Mode",
                                    tint = AppTheme.colors.accent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Light Theme Mode",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AppTheme.colors.textPrimary
                                )
                                Text(
                                    text = if (!isDarkTheme) "Clean Light theme enabled across all screens" else "Tactical Dark theme enabled",
                                    fontSize = 11.sp,
                                    color = AppTheme.colors.textSecondary
                                )
                            }
                        }
                        Switch(
                            checked = !isDarkTheme,
                            onCheckedChange = { isLight -> onToggleTheme(!isLight) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = AppTheme.colors.accent,
                                uncheckedThumbColor = AppTheme.colors.textSecondary,
                                uncheckedTrackColor = AppTheme.colors.cardBorder
                            )
                        )
                    }

                    // Divider
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(AppTheme.colors.divider)
                    )

                    // Offline Mode Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(AppTheme.colors.accentContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.WifiOff,
                                    contentDescription = null,
                                    tint = AppTheme.colors.accent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Simulate Offline Mode",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AppTheme.colors.textPrimary
                                )
                                Text(
                                    text = if (isOffline) "Running offline (local database only)" else "Connected (simulated network relay)",
                                    fontSize = 11.sp,
                                    color = AppTheme.colors.textSecondary
                                )
                            }
                        }
                        Switch(
                            checked = isOffline,
                            onCheckedChange = { onToggleOffline() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = AppTheme.colors.accent,
                                uncheckedThumbColor = AppTheme.colors.textSecondary,
                                uncheckedTrackColor = AppTheme.colors.cardBorder
                            )
                        )
                    }

                    // Divider
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(AppTheme.colors.divider)
                    )

                    // Push Notifications Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(AppTheme.colors.accentContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Notifications,
                                    contentDescription = null,
                                    tint = AppTheme.colors.accent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Hazard Warning Alerts",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = AppTheme.colors.textPrimary
                                )
                                Text(
                                    text = "Send immediate Android push alerts on critical risk",
                                    fontSize = 11.sp,
                                    color = AppTheme.colors.textSecondary
                                )
                            }
                        }
                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = { onToggleNotifications(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = AppTheme.colors.accent,
                                uncheckedThumbColor = AppTheme.colors.textSecondary,
                                uncheckedTrackColor = AppTheme.colors.cardBorder
                            )
                        )
                    }

                    // Divider
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(AppTheme.colors.divider)
                    )

                    // Language Selector
                    Box {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showLanguageMenu = true }
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(AppTheme.colors.accentContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Language,
                                        contentDescription = null,
                                        tint = AppTheme.colors.accent,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Language / भाषा / ভাষা",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = AppTheme.colors.textPrimary
                                    )
                                    Text(
                                        text = languageNames[currentLanguage] ?: "English",
                                        fontSize = 11.sp,
                                        color = AppTheme.colors.accent
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                contentDescription = null,
                                tint = AppTheme.colors.textTertiary,
                                modifier = Modifier.size(14.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showLanguageMenu,
                            onDismissRequest = { showLanguageMenu = false }
                        ) {
                            languageNames.forEach { (code, name) ->
                                DropdownMenuItem(
                                    text = { Text(name) },
                                    onClick = {
                                        onSelectLanguage(code)
                                        showLanguageMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Section: Safety & Guidelines
            Text(
                text = "Emergency Directives & Knowledge",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.accent
            )

            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenSafetyGuidelines),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = AppTheme.colors.card),
                border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.cardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(RiskWatch.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Shield,
                                contentDescription = null,
                                tint = RiskWatch,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Landslide Safety Guidelines",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppTheme.colors.textPrimary
                            )
                            Text(
                                text = "NDMA evacuation protocols, warning signs & shelters",
                                fontSize = 11.sp,
                                color = AppTheme.colors.textSecondary
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = null,
                        tint = AppTheme.colors.textTertiary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Reset Onboarding Demo Flow Button
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onResetOnboarding),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = AppTheme.colors.card),
                border = androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.cardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(AppTheme.colors.cardBorder.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.RestartAlt,
                                contentDescription = null,
                                tint = AppTheme.colors.textSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Restart Demo Walkthrough",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AppTheme.colors.textPrimary
                            )
                            Text(
                                text = "Return to initial Welcome/Onboarding screen",
                                fontSize = 11.sp,
                                color = AppTheme.colors.textSecondary
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = null,
                        tint = AppTheme.colors.textTertiary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // App Version Footer
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MountainLogo(
                    size = 32.dp,
                    mountainColor = AppTheme.colors.accent,
                    snowColor = if (isDarkTheme) Color.White else AppTheme.colors.canvas
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Landslide Early Warning System",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.textPrimary
                )
                Text(
                    text = "Version 1.0.0-PROTOTYPE • Local Offline Engine",
                    fontSize = 10.sp,
                    color = AppTheme.colors.textTertiary
                )
                Text(
                    text = "Designed for North-Eastern Himalayan States (Meghalaya, Sikkim, Nagaland, Mizoram)",
                    fontSize = 10.sp,
                    color = AppTheme.colors.textTertiary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(90.dp))
        }
    }
}
