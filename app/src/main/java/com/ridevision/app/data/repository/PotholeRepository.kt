package com.ridevision.app.data.repository

import com.ridevision.app.R
import com.ridevision.app.data.model.Pothole
import com.ridevision.app.data.model.PotholeStatus
import com.ridevision.app.data.model.Severity
import com.ridevision.app.domain.geo.GeoMatchingService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

sealed class ReportResult {
    data class Created(val pothole: Pothole) : ReportResult()
    data class MergedExisting(val pothole: Pothole, val distanceMeters: Int) : ReportResult()
}

sealed class VoteResult {
    data class Success(val updatedPothole: Pothole, val message: String) : VoteResult()
    data class CooldownActive(val message: String) : VoteResult()
}

class PotholeRepository {

    private val userVoteHistory = mutableMapOf<String, Long>() // "$userId-$potholeId" -> timestamp

    private val _potholes = MutableStateFlow<List<Pothole>>(createInitialSeedData())
    val potholes: StateFlow<List<Pothole>> = _potholes.asStateFlow()

    private fun getNowIso(): String {
        return SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
    }

    private fun createInitialSeedData(): List<Pothole> {
        val now = getNowIso()
        return listOf(
            // Mangaluru Hazards
            Pothole(
                id = "pothole-mng-001",
                lat = 12.9152,
                lon = 74.8988,
                city = "Mangaluru",
                address = "NH 73 near SJEC Gate, Vamanjoor",
                severity = Severity.SEVERE,
                status = PotholeStatus.ACTIVE,
                confirmationCount = 7,
                fixedConfirmationCount = 0,
                drawableResId = R.drawable.sample_severe_pothole,
                reportedAt = now,
                updatedAt = now,
                notes = "Deep crater on right wheel track immediately outside engineering college main entrance."
            ),
            Pothole(
                id = "pothole-mng-002",
                lat = 12.8715,
                lon = 74.8564,
                city = "Mangaluru",
                address = "Kankanady Bypass Road near Father Muller",
                severity = Severity.SEVERE,
                status = PotholeStatus.ACTIVE,
                confirmationCount = 9,
                fixedConfirmationCount = 0,
                drawableResId = R.drawable.sample_severe_pothole,
                reportedAt = now,
                updatedAt = now,
                notes = "Multiple adjoining edge cavities causing severe deceleration on curve."
            ),
            Pothole(
                id = "pothole-mng-003",
                lat = 12.8798,
                lon = 74.8532,
                city = "Mangaluru",
                address = "Kadri Temple Road, Mallikatte Junction",
                severity = Severity.MODERATE,
                status = PotholeStatus.ACTIVE,
                confirmationCount = 5,
                fixedConfirmationCount = 1,
                drawableResId = R.drawable.sample_moderate_pothole,
                reportedAt = now,
                updatedAt = now,
                notes = "Asphalt depression near rainwater drain grating."
            ),
            Pothole(
                id = "pothole-mng-004",
                lat = 12.8682,
                lon = 74.8427,
                city = "Mangaluru",
                address = "Hampankatta Circle near City Bus Stand",
                severity = Severity.MINOR,
                status = PotholeStatus.VERIFIED_FIXED,
                confirmationCount = 3,
                fixedConfirmationCount = 4,
                drawableResId = R.drawable.sample_clean_road,
                reportedAt = now,
                updatedAt = now,
                notes = "Patched by MCC maintenance crew on recent civic drive."
            ),
            Pothole(
                id = "pothole-mng-005",
                lat = 12.8615,
                lon = 74.8650,
                city = "Mangaluru",
                address = "Pumpwell Flyover Service Road",
                severity = Severity.SEVERE,
                status = PotholeStatus.ACTIVE,
                confirmationCount = 11,
                fixedConfirmationCount = 0,
                drawableResId = R.drawable.sample_severe_pothole,
                reportedAt = now,
                updatedAt = now,
                notes = "High-impact trench on busy junction merge corridor."
            ),
            // Bengaluru Hazards
            Pothole(
                id = "pothole-blr-001",
                lat = 12.9719,
                lon = 77.6412,
                city = "Bengaluru",
                address = "100 Feet Road, HAL 2nd Stage, Indiranagar",
                severity = Severity.SEVERE,
                status = PotholeStatus.ACTIVE,
                confirmationCount = 14,
                fixedConfirmationCount = 1,
                drawableResId = R.drawable.sample_severe_pothole,
                reportedAt = now,
                updatedAt = now,
                notes = "Wide pit on center lane near 12th Main crossing."
            ),
            Pothole(
                id = "pothole-blr-002",
                lat = 12.9352,
                lon = 77.6245,
                city = "Bengaluru",
                address = "80 Feet Road, 4th Block, Koramangala",
                severity = Severity.MODERATE,
                status = PotholeStatus.ACTIVE,
                confirmationCount = 6,
                fixedConfirmationCount = 2,
                drawableResId = R.drawable.sample_moderate_pothole,
                reportedAt = now,
                updatedAt = now,
                notes = "Road crumbling around manhole cover."
            ),
            Pothole(
                id = "pothole-blr-003",
                lat = 12.9176,
                lon = 77.6238,
                city = "Bengaluru",
                address = "Silk Board Junction Flyover Ramp",
                severity = Severity.SEVERE,
                status = PotholeStatus.ACTIVE,
                confirmationCount = 18,
                fixedConfirmationCount = 0,
                drawableResId = R.drawable.sample_severe_pothole,
                reportedAt = now,
                updatedAt = now,
                notes = "Severe hazard on flyover ingress ramp causing traffic bottleneck."
            ),
            // Udupi Hazard
            Pothole(
                id = "pothole-udp-001",
                lat = 13.3525,
                lon = 74.7865,
                city = "Udupi",
                address = "Manipal Tiger Circle, MIT Road",
                severity = Severity.MODERATE,
                status = PotholeStatus.ACTIVE,
                confirmationCount = 4,
                fixedConfirmationCount = 0,
                drawableResId = R.drawable.sample_moderate_pothole,
                reportedAt = now,
                updatedAt = now,
                notes = "Cavity near university main circle pedestrian crossing."
            )
        )
    }

    /**
     * Submits a report with 15m spatial deduplication guard.
     */
    fun submitReport(
        lat: Double,
        lon: Double,
        address: String,
        severity: Severity,
        notes: String,
        drawableResId: Int? = null
    ): ReportResult {
        val currentList = _potholes.value
        val now = getNowIso()

        // 15-meter spatial deduplication rule
        val nearbyExisting = currentList.firstOrNull {
            GeoMatchingService.haversineDistanceM(lat, lon, it.lat, it.lon) <= 15.0
        }

        return if (nearbyExisting != null) {
            val dist = GeoMatchingService.haversineDistanceM(lat, lon, nearbyExisting.lat, nearbyExisting.lon).toInt()
            val updated = nearbyExisting.copy(
                confirmationCount = nearbyExisting.confirmationCount + 1,
                updatedAt = now,
                severity = if (severity == Severity.SEVERE) Severity.SEVERE else nearbyExisting.severity
            )
            _potholes.value = currentList.map { if (it.id == updated.id) updated else it }
            ReportResult.MergedExisting(updated, dist)
        } else {
            val city = GeoMatchingService.detectCity(lat, lon)
            val newPothole = Pothole(
                id = "pothole-${UUID.randomUUID().toString().take(8)}",
                lat = lat,
                lon = lon,
                city = city,
                address = address.ifBlank { "Road near $lat, $lon ($city)" },
                severity = severity,
                status = PotholeStatus.ACTIVE,
                confirmationCount = 1,
                fixedConfirmationCount = 0,
                drawableResId = drawableResId ?: R.drawable.sample_severe_pothole,
                reportedAt = now,
                updatedAt = now,
                notes = notes
            )
            _potholes.value = listOf(newPothole) + currentList
            ReportResult.Created(newPothole)
        }
    }

    /**
     * Confirms "Still There" with 7-day anti-gaming cooldown guard.
     */
    fun confirmStillThere(potholeId: String, userId: String = "commuter-user-1"): VoteResult {
        val key = "$userId-$potholeId"
        val lastVote = userVoteHistory[key]
        val nowMs = System.currentTimeMillis()
        val cooldownMs = 7L * 24 * 60 * 60 * 1000 // 7 days

        if (lastVote != null && (nowMs - lastVote) < cooldownMs) {
            val remainingHours = ((cooldownMs - (nowMs - lastVote)) / (1000 * 60 * 60)).coerceAtLeast(1)
            return VoteResult.CooldownActive(
                "Cooldown Active: You already confirmed this hazard recently. Verification unlocked in $remainingHours hours."
            )
        }

        val target = _potholes.value.firstOrNull { it.id == potholeId }
            ?: return VoteResult.CooldownActive("Hazard not found.")

        userVoteHistory[key] = nowMs
        val updated = target.copy(
            confirmationCount = target.confirmationCount + 1,
            status = PotholeStatus.ACTIVE,
            updatedAt = getNowIso()
        )
        _potholes.value = _potholes.value.map { if (it.id == potholeId) updated else it }
        return VoteResult.Success(updated, "Verified! +1 confirmation recorded for road authorities.")
    }

    /**
     * Confirms "Fixed / Repaired". Transitions to VERIFIED_FIXED after 3 repair votes.
     */
    fun confirmFixed(potholeId: String, userId: String = "commuter-user-1"): VoteResult {
        val key = "$userId-fix-$potholeId"
        val lastVote = userVoteHistory[key]
        val nowMs = System.currentTimeMillis()
        val cooldownMs = 7L * 24 * 60 * 60 * 1000

        if (lastVote != null && (nowMs - lastVote) < cooldownMs) {
            return VoteResult.CooldownActive("Cooldown Active: You have already voted on this repair.")
        }

        val target = _potholes.value.firstOrNull { it.id == potholeId }
            ?: return VoteResult.CooldownActive("Hazard not found.")

        userVoteHistory[key] = nowMs
        val newFixedCount = target.fixedConfirmationCount + 1
        val newStatus = if (newFixedCount >= 3) {
            PotholeStatus.VERIFIED_FIXED
        } else {
            PotholeStatus.REPORTED_FIXED
        }

        val updated = target.copy(
            fixedConfirmationCount = newFixedCount,
            status = newStatus,
            updatedAt = getNowIso()
        )
        _potholes.value = _potholes.value.map { if (it.id == potholeId) updated else it }

        val msg = if (newStatus == PotholeStatus.VERIFIED_FIXED) {
            "Verified Repaired! Status updated to Safe/Repaired across commuter network."
        } else {
            "Repair vote logged ($newFixedCount/3 needed to verify safe)."
        }
        return VoteResult.Success(updated, msg)
    }
}
