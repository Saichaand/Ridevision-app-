package com.ridevision.app.ui.screens

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ridevision.app.data.model.Severity
import com.ridevision.app.ui.components.DetectionOverlayView
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
import com.ridevision.app.ui.viewmodel.SamplePreset
import android.graphics.BitmapFactory

@Composable
fun DetectorScreen(
    viewModel: RideVisionViewModel,
    onOpenReportDialog: (severity: Severity) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentBitmap by viewModel.currentBitmap.collectAsState()
    val detectionResult by viewModel.detectionResult.collectAsState()
    val selectedPreset by viewModel.selectedPreset.collectAsState()

    // Initialize with Severe sample on first launch if not already loaded
    LaunchedEffect(Unit) {
        if (currentBitmap == null) {
            viewModel.loadPreset(context, SamplePreset.SAMPLE_SEVERE)
        }
    }

    // Camera Capture Launcher
    val takePictureLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bmp: Bitmap? ->
        bmp?.let { viewModel.setCustomBitmap(it) }
    }

    // Photo Picker Launcher
    val pickImageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val stream = context.contentResolver.openInputStream(it)
                val bmp = BitmapFactory.decodeStream(stream)
                stream?.close()
                bmp?.let { validBmp -> viewModel.setCustomBitmap(validBmp) }
            } catch (e: Exception) {
                // Ignore failure
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Section Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(CyanAccent)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "ON-DEVICE COMPUTER VISION",
                color = CyanAccent,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Main Detection Viewport with Bounding Boxes
        DetectionOverlayView(
            bitmap = currentBitmap,
            detections = detectionResult?.detections ?: emptyList()
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Input Selector Bar (Sample Photos & Camera)
        Text(
            text = "Feed Source & Evaluation Test Benches",
            color = TextSecondary,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(SamplePreset.values()) { preset ->
                val isSelected = selectedPreset == preset && preset != SamplePreset.CAMERA_CAPTURE
                val chipBg = if (isSelected) CyanAccent.copy(alpha = 0.2f) else CockpitSurface
                val chipBorder = if (isSelected) CyanAccent else CockpitCardBorder

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(chipBg)
                        .border(1.dp, chipBorder, RoundedCornerShape(20.dp))
                        .clickable {
                            if (preset == SamplePreset.CAMERA_CAPTURE) {
                                takePictureLauncher.launch(null)
                            } else {
                                viewModel.loadPreset(context, preset)
                            }
                        }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = preset.label,
                        color = if (isSelected) CyanAccent else TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Action Buttons: Camera & Pick Photo
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedButton(
                onClick = { takePictureLauncher.launch(null) },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Take Photo", fontSize = 13.sp)
            }

            OutlinedButton(
                onClick = { pickImageLauncher.launch("image/*") },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.FileUpload,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Pick Image", fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Real-Time CV Telemetry Panel
        detectionResult?.let { res ->
            Card(
                colors = CardDefaults.cardColors(containerColor = CockpitSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, CockpitCardBorder),
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
                            text = "DETECTION TELEMETRY",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = CyanAccent,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${res.processingTimeMs} ms (Edge)",
                                color = CyanAccent,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 3 Metric Cards
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Hazards Count
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(CockpitSurfaceVariant)
                                .padding(12.dp)
                        ) {
                            Column {
                                Text("Potholes", color = TextMuted, fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${res.detections.size}",
                                    color = if (res.detections.isNotEmpty()) SevereRed else EmeraldSafe,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }

                        // Max Confidence
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(CockpitSurfaceVariant)
                                .padding(12.dp)
                        ) {
                            Column {
                                Text("Confidence", color = TextMuted, fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (res.detections.isNotEmpty()) "${(res.maxConfidence * 100).toInt()}%" else "Clear",
                                    color = TextPrimary,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }

                        // Road Quality Score
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(CockpitSurfaceVariant)
                                .padding(12.dp)
                        ) {
                            Column {
                                Text("Road Index", color = TextMuted, fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${res.roadConditionScore}/100",
                                    color = if (res.roadConditionScore >= 80) EmeraldSafe else ModerateOrange,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = res.summary,
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )

                    // If potholes were detected, show direct "Report Hazard" button
                    if (res.detections.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        val maxSev = res.detections.maxByOrNull { it.severity.ordinal }?.severity ?: Severity.SEVERE

                        Button(
                            onClick = { onOpenReportDialog(maxSev) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (maxSev == Severity.SEVERE) SevereRed else ModerateOrange
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.ReportProblem,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Report ${maxSev.label.uppercase()} Hazard to Municipality",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
