package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.ui.components.MountainLogo
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    composeTestRule.setContent {
      MyApplicationTheme {
        MountainLogo(mountainColor = ForestGreenPrimary)
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }

  @Test
  fun mapScreen_rendersWithoutCrash() {
    val repository = com.example.data.repository.LandslideRepository()
    val initialZones = repository.zones.value
    composeTestRule.setContent {
      MyApplicationTheme {
        com.example.ui.screens.map.MapScreen(
          zones = initialZones,
          selectedZoneId = "zone_shillong",
          onSelectZone = {},
          onNavigateToZoneDetails = {}
        )
      }
    }
  }
}
