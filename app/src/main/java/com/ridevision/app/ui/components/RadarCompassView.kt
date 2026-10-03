package com.ridevision.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ridevision.app.data.model.Pothole
import com.ridevision.app.data.model.PotholeStatus
import com.ridevision.app.data.model.Severity
import com.ridevision.app.domain.geo.GeoMatchingService
import com.ridevision.app.ui.theme.CockpitSurface
import com.ridevision.app.ui.theme.CyanAccent
import com.ridevision.app.ui.theme.EmeraldSafe
import com.ridevision.app.ui.theme.ModerateOrange
import com.ridevision.app.ui.theme.SevereRed
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RadarCompassView(
    currentLat: Double,
    currentLon: Double,
    currentHeading: Float,
    potholes: List<Pothole>,
    radarRangeMeters: Double = 300.0,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(16.dp)
            .clip(CircleShape)
            .background(CockpitSurface)
            .border(2.dp, CyanAccent.copy(alpha = 0.35f), CircleShape)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val maxRadius = (size.width / 2f) * 0.90f

            // Concentric range circles: 100m, 200m, 300m
            val ranges = listOf(100.0, 200.0, 300.0)
            ranges.forEach { range ->
                val r = (range / radarRangeMeters).toFloat() * maxRadius
                drawCircle(
                    color = CyanAccent.copy(alpha = 0.18f),
                    radius = r,
                    center = center,
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }

            // Crosshair lines
            drawLine(
                color = CyanAccent.copy(alpha = 0.22f),
                start = Offset(center.x, center.y - maxRadius),
                end = Offset(center.x, center.y + maxRadius),
                strokeWidth = 1.dp.toPx()
            )
            drawLine(
                color = CyanAccent.copy(alpha = 0.22f),
                start = Offset(center.x - maxRadius, center.y),
                end = Offset(center.x + maxRadius, center.y),
                strokeWidth = 1.dp.toPx()
            )

            // 250m Directional Warning Cone (+/- 45° around heading relative to top)
            // The top of the radar represents current vehicle heading
            val coneStartAngle = -90f - 45f
            val coneSweep = 90f
            val coneRadius = (250.0 / radarRangeMeters).toFloat() * maxRadius

            drawArc(
                brush = Brush.radialGradient(
                    colors = listOf(
                        CyanAccent.copy(alpha = 0.32f),
                        CyanAccent.copy(alpha = 0.05f)
                    ),
                    center = center,
                    radius = coneRadius
                ),
                startAngle = coneStartAngle,
                sweepAngle = coneSweep,
                useCenter = true,
                topLeft = Offset(center.x - coneRadius, center.y - coneRadius),
                size = Size(coneRadius * 2, coneRadius * 2)
            )

            drawArc(
                color = CyanAccent.copy(alpha = 0.75f),
                startAngle = coneStartAngle,
                sweepAngle = coneSweep,
                useCenter = false,
                topLeft = Offset(center.x - coneRadius, center.y - coneRadius),
                size = Size(coneRadius * 2, coneRadius * 2),
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )

            // Plot nearby potholes relative to heading (top = vehicle direction)
            potholes.forEach { pothole ->
                val distance = GeoMatchingService.haversineDistanceM(
                    currentLat, currentLon, pothole.lat, pothole.lon
                )
                if (distance <= radarRangeMeters) {
                    val bearing = GeoMatchingService.bearingDeg(
                        currentLat, currentLon, pothole.lat, pothole.lon
                    )
                    // Relative bearing to current vehicle heading (0 = straight ahead = up)
                    val relativeBearing = (bearing - currentHeading + 360f) % 360f
                    // Convert relative bearing to canvas math angle (-90 deg is top)
                    val mathAngleRad = Math.toRadians((relativeBearing - 90.0))

                    val r = (distance / radarRangeMeters).toFloat() * maxRadius
                    val px = center.x + (r * cos(mathAngleRad)).toFloat()
                    val py = center.y + (r * sin(mathAngleRad)).toFloat()

                    val dotColor = when {
                        pothole.status == PotholeStatus.VERIFIED_FIXED -> EmeraldSafe
                        pothole.severity == Severity.SEVERE -> SevereRed
                        pothole.severity == Severity.MODERATE -> ModerateOrange
                        else -> Color(0xFFFBBF24)
                    }

                    // Hazard Glow & Dot
                    drawCircle(
                        color = dotColor.copy(alpha = 0.35f),
                        radius = 12.dp.toPx(),
                        center = Offset(px, py)
                    )
                    drawCircle(
                        color = dotColor,
                        radius = 6.dp.toPx(),
                        center = Offset(px, py)
                    )
                }
            }

            // Vehicle center indicator
            val vehiclePath = Path().apply {
                moveTo(center.x, center.y - 12.dp.toPx())
                lineTo(center.x - 8.dp.toPx(), center.y + 10.dp.toPx())
                lineTo(center.x, center.y + 5.dp.toPx())
                lineTo(center.x + 8.dp.toPx(), center.y + 10.dp.toPx())
                close()
            }
            drawPath(vehiclePath, CyanAccent)
        }

        // Radar Distance Markings
        Text(
            text = "250m Cone",
            color = CyanAccent.copy(alpha = 0.85f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 18.dp)
        )
        Text(
            text = "Vehicle Heading: ${currentHeading.toInt()}°",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 10.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
        )
    }
}
