package com.ridevision.app.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ridevision.app.data.model.Detection
import com.ridevision.app.data.model.Severity
import com.ridevision.app.ui.theme.CockpitCardBorder
import com.ridevision.app.ui.theme.CockpitSurface
import com.ridevision.app.ui.theme.ModerateOrange
import com.ridevision.app.ui.theme.SevereRed
import com.ridevision.app.ui.theme.TextMuted

@Composable
fun DetectionOverlayView(
    bitmap: Bitmap?,
    detections: List<Detection>,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(4f / 3f)
            .clip(RoundedCornerShape(16.dp))
            .background(CockpitSurface)
            .border(1.dp, CockpitCardBorder, RoundedCornerShape(16.dp))
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Road Camera Feed",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Canvas drawing bounding boxes on normalized coordinates (0..1)
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasW = size.width
                val canvasH = size.height

                detections.forEach { det ->
                    val box = det.boxNorm
                    val left = box.left * canvasW
                    val top = box.top * canvasH
                    val width = box.width() * canvasW
                    val height = box.height() * canvasH

                    val boxColor = when (det.severity) {
                        Severity.SEVERE -> SevereRed
                        Severity.MODERATE -> ModerateOrange
                        Severity.MINOR -> Color(0xFFFBBF24)
                    }

                    // Translucent fill
                    drawRect(
                        color = boxColor.copy(alpha = 0.15f),
                        topLeft = Offset(left, top),
                        size = Size(width, height)
                    )

                    // Crisp high-vis border
                    drawRect(
                        color = boxColor,
                        topLeft = Offset(left, top),
                        size = Size(width, height),
                        style = Stroke(width = 3.dp.toPx())
                    )

                    // Corner brackets for military/HUD aesthetic
                    val cornerLen = 14.dp.toPx()
                    val strokeW = 4.dp.toPx()
                    // Top-Left
                    drawLine(boxColor, Offset(left, top), Offset(left + cornerLen, top), strokeW)
                    drawLine(boxColor, Offset(left, top), Offset(left, top + cornerLen), strokeW)
                    // Top-Right
                    drawLine(boxColor, Offset(left + width, top), Offset(left + width - cornerLen, top), strokeW)
                    drawLine(boxColor, Offset(left + width, top), Offset(left + width, top + cornerLen), strokeW)
                    // Bottom-Left
                    drawLine(boxColor, Offset(left, top + height), Offset(left + cornerLen, top + height), strokeW)
                    drawLine(boxColor, Offset(left, top + height), Offset(left, top + height - cornerLen), strokeW)
                    // Bottom-Right
                    drawLine(boxColor, Offset(left + width, top + height), Offset(left + width - cornerLen, top + height), strokeW)
                    drawLine(boxColor, Offset(left + width, top + height), Offset(left + width, top + height - cornerLen), strokeW)
                }
            }
        } else {
            Text(
                text = "No Road Video / Frame Selected",
                color = TextMuted,
                fontSize = 14.sp
            )
        }
    }
}
