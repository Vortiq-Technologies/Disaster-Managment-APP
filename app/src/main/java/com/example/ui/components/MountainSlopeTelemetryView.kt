package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RiskLevel
import com.example.data.model.SensorNode
import com.example.ui.theme.RiskCritical
import com.example.ui.theme.RiskNormal
import com.example.ui.theme.RiskWatch

@Composable
fun MountainSlopeTelemetryView(
    nodes: List<SensorNode>,
    selectedNodeId: String?,
    modifier: Modifier = Modifier,
    onNodeClick: (SensorNode) -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAnim by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()

        // Canvas for dashed telemetry connecting lines and danger zone boundary
        Canvas(modifier = Modifier.fillMaxSize()) {
            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)

            // Draw connection lines between nodes
            fun drawNodeLink(fromId: String, toId: String) {
                val from = nodes.find { it.id == fromId } ?: return
                val to = nodes.find { it.id == toId } ?: return
                drawLine(
                    color = Color.White.copy(alpha = 0.5f),
                    start = Offset(from.xPercent * widthPx, from.yPercent * heightPx),
                    end = Offset(to.xPercent * widthPx, to.yPercent * heightPx),
                    strokeWidth = 2.5f,
                    pathEffect = dashEffect
                )
            }

            drawNodeLink("node_01", "node_02")
            drawNodeLink("node_02", "node_03")
            drawNodeLink("node_02", "node_04")
            drawNodeLink("node_04", "node_05")
            drawNodeLink("node_03", "node_06")

            // Draw Hazard zone red perimeter around Node 06
            val node06 = nodes.find { it.id == "node_06" }
            if (node06 != null) {
                val cx = node06.xPercent * widthPx
                val cy = node06.yPercent * heightPx
                val hazardPath = Path().apply {
                    moveTo(cx - 90f, cy - 70f)
                    cubicTo(cx - 30f, cy - 90f, cx + 50f, cy - 70f, cx + 80f, cy - 30f)
                    cubicTo(cx + 90f, cy + 30f, cx + 60f, cy + 90f, cx - 20f, cy + 110f)
                    cubicTo(cx - 90f, cy + 100f, cx - 110f, cy + 40f, cx - 90f, cy - 70f)
                    close()
                }

                drawPath(
                    path = hazardPath,
                    color = RiskCritical.copy(alpha = 0.65f),
                    style = Stroke(
                        width = 3.5f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
                    )
                )

                // Fill with subtle transparent red tint
                drawPath(
                    path = hazardPath,
                    color = RiskCritical.copy(alpha = 0.12f)
                )
            }
        }

        // Draw Interactive Sensor Nodes
        nodes.forEach { node ->
            val isSelected = node.id == selectedNodeId
            val nodeColor = when (node.status) {
                RiskLevel.CRITICAL -> RiskCritical
                RiskLevel.WATCH -> RiskWatch
                RiskLevel.NORMAL -> RiskNormal
                RiskLevel.OFFLINE -> Color(0xFF94A3B8)
            }

            val xOffset = (node.xPercent * maxWidth.value).dp - 24.dp
            val yOffset = (node.yPercent * maxHeight.value).dp - 24.dp

            Box(
                modifier = Modifier
                    .offset(x = xOffset, y = yOffset)
                    .clickable { onNodeClick(node) },
                contentAlignment = Alignment.Center
            ) {
                // Outer glowing halo ring
                val haloSize = if (node.status == RiskLevel.CRITICAL) 48.dp * pulseAnim else 44.dp
                Box(
                    modifier = Modifier
                        .size(haloSize)
                        .clip(CircleShape)
                        .background(nodeColor.copy(alpha = if (node.status == RiskLevel.CRITICAL) 0.35f else 0.25f))
                )

                // Middle ring
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(nodeColor.copy(alpha = 0.65f))
                        .border(
                            width = if (isSelected) 2.5.dp else 1.5.dp,
                            color = if (isSelected) Color.White else nodeColor,
                            shape = CircleShape
                        )
                )

                // Center core dot
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )

                // Node text label badge
                val labelXOffset = if (node.xPercent > 0.65f) 36.dp else (-48).dp
                Box(
                    modifier = Modifier
                        .offset(x = labelXOffset, y = (-4).dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xE60A130E))
                        .border(1.dp, nodeColor.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = node.name,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = node.status.label,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium,
                            color = nodeColor
                        )
                    }
                }
            }
        }
    }
}
