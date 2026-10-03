package com.ridevision.app.data.model

import android.graphics.RectF

enum class Severity(val label: String, val colorHex: Long) {
    MINOR("Minor", 0xFFFBBF24),       // Amber-yellow
    MODERATE("Moderate", 0xFFFF9500), // Orange
    SEVERE("Severe", 0xFFFF3B30)       // Bright Red
}

enum class PotholeStatus(val label: String) {
    ACTIVE("Active Hazard"),
    REPORTED_FIXED("Pending Verification"),
    VERIFIED_FIXED("Repaired / Safe")
}

data class Pothole(
    val id: String,
    val lat: Double,
    val lon: Double,
    val city: String,
    val address: String,
    val severity: Severity,
    val status: PotholeStatus = PotholeStatus.ACTIVE,
    val confirmationCount: Int = 1,
    val fixedConfirmationCount: Int = 0,
    val drawableResId: Int? = null,
    val reportedAt: String,
    val updatedAt: String,
    val notes: String = ""
)

data class Detection(
    val id: String,
    val boxNorm: RectF, // Normalized 0..1 (left, top, right, bottom)
    val confidence: Float,
    val severity: Severity,
    val areaRatio: Float,
    val engine: String = "RideVision Edge-CV"
)

data class DetectionResult(
    val detections: List<Detection>,
    val processingTimeMs: Long,
    val maxConfidence: Float,
    val roadConditionScore: Int, // 0 - 100 (100 = flawless road)
    val summary: String
)

data class HazardWarning(
    val potholeId: String,
    val distanceMeters: Int,
    val angularDeviationDeg: Float,
    val severity: Severity,
    val address: String,
    val confirmationCount: Int,
    val message: String,
    val isUrgent: Boolean
)

data class TripPoint(
    val lat: Double,
    val lon: Double,
    val heading: Float,
    val speedKmh: Float,
    val timestampMs: Long = System.currentTimeMillis()
)

data class PassedHazard(
    val pothole: Pothole,
    val closestDistanceMeters: Float
)

data class CityConfig(
    val cityName: String,
    val authorityName: String,
    val channelType: String, // "whatsapp", "helpline", "email"
    val contactValue: String,
    val instructions: String
)

data class MunicipalDispatch(
    val cityName: String,
    val authorityName: String,
    val channelType: String,
    val contactValue: String,
    val prefilledMessage: String,
    val actionUrl: String, // wa.me, tel:, or mailto:
    val instructions: String
)
