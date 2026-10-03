package com.ridevision.app.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ridevision.app.data.model.Pothole
import com.ridevision.app.data.model.PotholeStatus
import com.ridevision.app.data.model.Severity
import com.ridevision.app.domain.geo.GeoMatchingService
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
fun CivicDeskScreen(
    viewModel: RideVisionViewModel,
    preselectedPothole: Pothole? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val potholes by viewModel.potholes.collectAsState()

    // Default to preselected pothole, or first severe pothole
    var selectedPothole by remember(preselectedPothole) {
        mutableStateOf(preselectedPothole ?: potholes.firstOrNull { it.severity == Severity.SEVERE } ?: potholes.firstOrNull())
    }

    val activeCount = potholes.count { it.status == PotholeStatus.ACTIVE }
    val fixedCount = potholes.count { it.status == PotholeStatus.VERIFIED_FIXED }
    val severeCount = potholes.count { it.severity == Severity.SEVERE }

    val dispatch = selectedPothole?.let { viewModel.getMunicipalDispatch(it) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Municipal Health & Analytics Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CockpitSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, CockpitCardBorder),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "MUNICIPAL CIVIC INTEGRATION & ANALYTICS",
                        color = CyanAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Total Active
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(CockpitSurfaceVariant)
                                .padding(12.dp)
                        ) {
                            Column {
                                Text("Active Hazards", color = TextMuted, fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "$activeCount",
                                    color = SevereRed,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }

                        // Repaired
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(CockpitSurfaceVariant)
                                .padding(12.dp)
                        ) {
                            Column {
                                Text("Verified Fixed", color = TextMuted, fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "$fixedCount",
                                    color = EmeraldSafe,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }

                        // Severe Critical
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(CockpitSurfaceVariant)
                                .padding(12.dp)
                        ) {
                            Column {
                                Text("Severe Hotspots", color = TextMuted, fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "$severeCount",
                                    color = ModerateOrange,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }
            }
        }

        // Official Civic Helplines Directory
        item {
            Text(
                text = "OFFICIAL MUNICIPAL GRIEVANCE CHANNELS",
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CockpitSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, CockpitCardBorder),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Mangaluru MCC Official WhatsApp
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Mangaluru City Corporation (MCC)",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Official WhatsApp: +91 9449007722",
                                color = EmeraldSafe,
                                fontSize = 11.sp
                            )
                        }
                        Button(
                            onClick = {
                                val url = "https://wa.me/919449007722"
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldSafe),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("WhatsApp", fontSize = 11.sp)
                        }
                    }

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(CockpitCardBorder))

                    // Bengaluru BBMP Sahaaya
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "BBMP Bengaluru Pothole Control Room",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "24x7 Helpline: 080-22660000",
                                color = CyanAccent,
                                fontSize = 11.sp
                            )
                        }
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:08022660000"))
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = Color.Black),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Call", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Live Complaint Dispatch Section
        item {
            Text(
                text = "CIVIC GRIEVANCE DISPATCH GENERATOR",
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }

        selectedPothole?.let { pothole ->
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CockpitSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyanAccent.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Target: ${pothole.address}",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(pothole.severity.colorHex.let { Color(it) }.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = pothole.severity.label.uppercase(),
                                    color = pothole.severity.colorHex.let { Color(it) },
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Formatted Message Preview
                        dispatch?.let { d ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(CockpitSurfaceVariant)
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = d.prefilledMessage,
                                    color = TextPrimary,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    lineHeight = 16.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Dispatch Action Buttons
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Button(
                                    onClick = {
                                        try {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(d.actionUrl))
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Could not open channel directly: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (d.channelType == "whatsapp") EmeraldSafe else CyanAccent,
                                        contentColor = if (d.channelType == "whatsapp") Color.White else Color.Black
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1.2f)
                                ) {
                                    Icon(
                                        imageVector = if (d.channelType == "whatsapp") Icons.Default.Send else Icons.Default.Call,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (d.channelType == "whatsapp") "Send to MCC WhatsApp" else "Dial Authority",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                OutlinedButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("RideVision Complaint", d.prefilledMessage))
                                        Toast.makeText(context, "Official grievance text copied to clipboard!", Toast.LENGTH_SHORT).show()
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(0.8f)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Copy", fontSize = 12.sp)
                                }

                                OutlinedButton(
                                    onClick = {
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, d.prefilledMessage)
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "Share Grievance Report"))
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.size(46.dp)
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
