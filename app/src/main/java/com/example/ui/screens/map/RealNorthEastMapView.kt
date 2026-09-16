package com.example.ui.screens.map

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Satellite
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.data.model.ZoneRiskData

/**
 * Controller to trigger camera movements, zoom, and layer switches
 */
class RealMapController {
    var zoomIn: () -> Unit = {}
    var zoomOut: () -> Unit = {}
    var resetOverview: () -> Unit = {}
    var flyTo: (zoneId: String, lat: Double, lon: Double, zoom: Float) -> Unit = { _, _, _, _ -> }
}

@Composable
fun rememberRealMapController(): RealMapController = remember { RealMapController() }

/**
 * Real Map layer options for Northeast India and Indian Map outlines
 */
enum class RealMapLayer(
    val id: String,
    val title: String,
    val shortLabel: String,
    val icon: ImageVector,
    val description: String
) {
    STREET_GIS(
        id = "outlines",
        title = "Indian Outlines",
        shortLabel = "Outlines",
        icon = Icons.Default.Map,
        description = "Accurate state vector outlines of India and the Northeast region"
    ),
    SATELLITE(
        id = "satellite",
        title = "Satellite Map",
        shortLabel = "Satellite",
        icon = Icons.Default.Satellite,
        description = "High-resolution Earth photography with Indian vector overlay"
    ),
    TOPOGRAPHIC(
        id = "topo",
        title = "Topographic",
        shortLabel = "Topo",
        icon = Icons.Default.Terrain,
        description = "Elevation contour lines, mountain ridges & hillshade"
    ),
    TERRAIN_PHOTO(
        id = "relief",
        title = "Relief Photo",
        shortLabel = "Relief",
        icon = Icons.Default.Layers,
        description = "Photorealistic Northeast satellite relief calibration"
    )
}

/**
 * Interactive Real Map of Northeast India & Subcontinent
 * Renders real vector outlines for India and all 8 Northeast states (Sikkim, Assam,
 * Meghalaya, Arunachal Pradesh, Nagaland, Manipur, Mizoram, Tripura) with exact GPS
 * coordinates, Brahmaputra river network, national highway lifelines, and live telemetry.
 */
@Composable
fun RealNorthEastMapView(
    zones: List<ZoneRiskData>,
    selectedZoneId: String,
    currentLayer: RealMapLayer,
    moistureOverlayEnabled: Boolean,
    onSelectZone: (String) -> Unit,
    modifier: Modifier = Modifier,
    mapController: RealMapController? = null
) {
    IndianVectorMapView(
        zones = zones,
        selectedZoneId = selectedZoneId,
        currentLayer = currentLayer,
        moistureOverlayEnabled = moistureOverlayEnabled,
        onSelectZone = onSelectZone,
        modifier = modifier,
        mapController = mapController
    )
}
