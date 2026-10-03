package com.ridevision.app.ui.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ridevision.app.data.model.Pothole
import com.ridevision.app.data.model.PotholeStatus
import com.ridevision.app.data.model.Severity
import com.ridevision.app.domain.geo.GeoMatchingService
import com.ridevision.app.ui.components.RadarCompassView
import com.ridevision.app.ui.theme.CockpitCardBorder
import com.ridevision.app.ui.theme.CockpitSurface
import com.ridevision.app.ui.theme.CockpitSurfaceVariant
import com.ridevision.app.ui.theme.CyanAccent
import com.ridevision.app.ui.theme.EmeraldSafe
import com.ridevision.app.ui.theme.ModerateOrange
import com.ridevision.app.ui.theme.SevereRed
import com.ridevision.app.ui.theme.TextMuted
import com.ridevision.app.ui.theme.TextPrimary
import com.ridevision.app.ui.theme.TextSecondary
import com.ridevision.app.ui.viewmodel.RideVisionViewModel

@Composable
fun RadarMapScreen(
    viewModel: RideVisionViewModel,
    onSelectPotholeForDispatch: (Pothole) -> Unit,
    modifier: Modifier = Modifier
) {
    val potholes by viewModel.potholes.collectAsState()
    val currentLat by viewModel.currentLat.collectAsState()
    val currentLon by viewModel.currentLon.collectAsState()
    val currentHeading by viewModel.currentHeading.collectAsState()
    val currentSpeed by viewModel.currentSpeedKmh.collectAsState()
    val isSimulating by viewModel.isSimulationPlaying.collectAsState()
    val cityFilter by viewModel.cityFilter.collectAsState()
    val severityFilter by viewModel.severityFilter.collectAsState()

    val cities = listOf("All", "Mangaluru", "Bengaluru", "Udupi", "Mysuru")
    val severities = listOf("All", "Severe", "Moderate", "Minor")

    val filteredPotholes = potholes.filter { p ->
        (cityFilter == "All" || p.city.equals(cityFilter, ignoreCase = true)) &&
                (severityFilter == "All" || p.severity.label.equals(severityFilter, ignoreCase = true))
    }.sortedBy {
        GeoMatchingService.haversineDistanceM(currentLat, currentLon, it.lat, it.lon)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Telemetry & Simulation Controls Bar
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CockpitSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, CockpitCardBorder),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = CyanAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "GPS: ${"%.4f".format(currentLat)}, ${"%.4f".format(currentLon)}",
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Speed: ${currentSpeed.toInt()} km/h • Heading: ${currentHeading.toInt()}°",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(
                            onClick = { viewModel.toggleSimulation() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSimulating) ModerateOrange else CyanAccent.copy(alpha = 0.2f),
                                contentColor = if (isSimulating) Color.White else CyanAccent
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(36.dp)
                        ) {
                            Icon(
                                imageVector = if (isSimulating) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isSimulating) "Pause" else "Drive Sim", fontSize = 11.sp)
                        }

                        IconButton(
                            onClick = { viewModel.stepSimulation() },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(CockpitSurfaceVariant)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipNext,
                                contentDescription = "Step Forward",
                                tint = TextPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // 250m Directional Radar Canvas
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                RadarCompassView(
                    currentLat = currentLat,
                    currentLon = currentLon,
                    currentHeading = currentHeading,
                    potholes = filteredPotholes,
                    modifier = Modifier.height(260.dp)
                )
            }
        }

        // City Filters
        item {
            Text(
                text = "City Jurisdiction",
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(cities) { city ->
                    val isSelected = cityFilter == city
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) CyanAccent.copy(alpha = 0.2f) else CockpitSurface)
                            .border(1.dp, if (isSelected) CyanAccent else CockpitCardBorder, RoundedCornerShape(16.dp))
                            .clickable { viewModel.setCityFilter(city) }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = city,
                            color = if (isSelected) CyanAccent else TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        // Severity Filters
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(severities) { sev ->
                    val isSelected = severityFilter == sev
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) CockpitSurfaceVariant else CockpitSurface)
                            .border(1.dp, if (isSelected) CyanAccent.copy(alpha = 0.5f) else CockpitCardBorder, RoundedCornerShape(16.dp))
                            .clickable { viewModel.setSeverityFilter(sev) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = sev,
                            color = if (isSelected) CyanAccent else TextMuted,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        // Hazards List Header
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "ROAD HAZARD RADAR FEED (${filteredPotholes.size})",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }

        // Hazard Cards
        items(filteredPotholes, key = { it.id }) { pothole ->
            val distance = GeoMatchingService.haversineDistanceM(
                currentLat, currentLon, pothole.lat, pothole.lon
            ).toInt()

            val sevColor = when (pothole.severity) {
                Severity.SEVERE -> SevereRed
                Severity.MODERATE -> ModerateOrange
                Severity.MINOR -> Color(0xFFFBBF24)
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = CockpitSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, CockpitCardBorder),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Severity & Distance Badge
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(sevColor.copy(alpha = 0.2f))
                                    .border(1.dp, sevColor, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = pothole.severity.label.uppercase(),
                                    color = sevColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = "${distance}m away",
                                color = CyanAccent,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Status Badge
                        val isFixed = pothole.status == PotholeStatus.VERIFIED_FIXED
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isFixed) EmeraldSafe.copy(alpha = 0.2f) else CockpitSurfaceVariant)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = pothole.status.label,
                                color = if (isFixed) EmeraldSafe else TextMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = pothole.address,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    if (pothole.notes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = pothole.notes,
                            color = TextMuted,
                            fontSize = 12.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Crowd Confirmation Metrics & Actions
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "👍 ${pothole.confirmationCount} Still There • 🛠️ ${pothole.fixedConfirmationCount} Fix Votes",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            // +1 Still There Button
                            OutlinedButton(
                                onClick = { viewModel.voteStillThere(pothole.id) },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ThumbUp,
                                    contentDescription = null,
                                    tint = CyanAccent,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Still There", fontSize = 10.sp, color = CyanAccent)
                            }

                            // Mark Fixed Button
                            OutlinedButton(
                                onClick = { viewModel.voteFixed(pothole.id) },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Build,
                                    contentDescription = null,
                                    tint = EmeraldSafe,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Fixed", fontSize = 10.sp, color = EmeraldSafe)
                            }

                            // Civic Dispatch Button
                            IconButton(
                                onClick = { onSelectPotholeForDispatch(pothole) },
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(CyanAccent.copy(alpha = 0.2f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = "Dispatch Complaint",
                                    tint = CyanAccent,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
