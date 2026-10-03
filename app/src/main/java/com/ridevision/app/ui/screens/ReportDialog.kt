package com.ridevision.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ridevision.app.data.model.Severity
import com.ridevision.app.ui.theme.CockpitCardBorder
import com.ridevision.app.ui.theme.CockpitSurface
import com.ridevision.app.ui.theme.CockpitSurfaceVariant
import com.ridevision.app.ui.theme.CyanAccent
import com.ridevision.app.ui.theme.ModerateOrange
import com.ridevision.app.ui.theme.SevereRed
import com.ridevision.app.ui.theme.TextMuted
import com.ridevision.app.ui.theme.TextPrimary
import com.ridevision.app.ui.theme.TextSecondary

@Composable
fun ReportDialog(
    initialSeverity: Severity,
    currentLat: Double,
    currentLon: Double,
    onDismiss: () -> Unit,
    onSubmit: (address: String, lat: Double, lon: Double, severity: Severity, notes: String) -> Unit
) {
    var address by remember { mutableStateOf("NH 73 near SJEC Gate, Vamanjoor") }
    var severity by remember { mutableStateOf(initialSeverity) }
    var notes by remember { mutableStateOf("Verified via RideVision edge computer vision analyzer.") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CockpitSurface,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = CyanAccent
                )
                Spacer(modifier = Modifier.padding(start = 8.dp))
                Text(
                    text = "Report Road Hazard",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Reports are submitted to the municipal database with 15m spatial deduplication and auto-routed to city grievance channels.",
                    color = TextMuted,
                    fontSize = 12.sp
                )

                // Severity Picker Chips
                Text(
                    text = "Hazard Severity",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Severity.values().forEach { s ->
                        val isSelected = severity == s
                        val color = when (s) {
                            Severity.SEVERE -> SevereRed
                            Severity.MODERATE -> ModerateOrange
                            Severity.MINOR -> Color(0xFFFBBF24)
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) color.copy(alpha = 0.25f) else CockpitSurfaceVariant)
                                .border(1.5.dp, if (isSelected) color else CockpitCardBorder, RoundedCornerShape(10.dp))
                                .clickable { severity = s }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = s.label,
                                color = if (isSelected) color else TextMuted,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                // Road Address
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Road Name / Landmark") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = CockpitCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedLabelColor = CyanAccent,
                        unfocusedLabelColor = TextMuted
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Coordinates Display
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(CockpitSurfaceVariant)
                        .padding(10.dp)
                ) {
                    Text(
                        text = "GPS Coordinates: ${"%.5f".format(currentLat)}, ${"%.5f".format(currentLon)}",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }

                // Commuter Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Description & Notes") },
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = CockpitCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedLabelColor = CyanAccent,
                        unfocusedLabelColor = TextMuted
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSubmit(address, currentLat, currentLon, severity, notes)
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = Color.Black),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Submit & Register", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Cancel", color = TextMuted)
            }
        }
    )
}
