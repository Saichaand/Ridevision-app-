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

    private val userVoteHistory = mutableMapOf<String, Long>()

    private val _potholes = MutableStateFlow<List<Pothole>>(createInitialSeedData())
    val potholes: StateFlow<List<Pothole>> = _potholes.asStateFlow()

    private fun getNowIso(): String {
        return SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
    }

    private fun createInitialSeedData(): List<Pothole> {
        val now = getNowIso()
        return listOf(
            // 1. Critical Pending Report from screenshot
            Pothole(
                id = "pothole-88219",
                ticketNumber = "#RV-88219",
                lat = 37.7749,
                lon = -122.4194,
                city = "Metro City",
                address = "Grand Ave & 8th St",
                laneInfo = "Lane 2, Northbound Edge",
                severity = Severity.SEVERE,
                status = PotholeStatus.ACTIVE,
                confirmationCount = 24,
                fixedConfirmationCount = 0,
                drawableResId = R.drawable.sample_severe_pothole,
                reportedAt = "Oct 22, 2024",
                updatedAt = now,
                notes = "Deep asphalt fissure road hazard with jagged rim and water pooled inside.",
                depthCm = 14.2f,
                distanceDisplay = "0.4 mi away",
                statusNote = "Under Investigation by City Works"
            ),
            // 2. Moderate In-Progress Report from screenshot
            Pothole(
                id = "pothole-77402",
                ticketNumber = "#RV-77402",
                lat = 37.7812,
                lon = -122.4069,
                city = "Metro City",
                address = "Keele St at St Clair",
                laneInfo = "Bike Corridor Center",
                severity = Severity.MODERATE,
                status = PotholeStatus.IN_PROGRESS,
                confirmationCount = 41,
                fixedConfirmationCount = 2,
                drawableResId = R.drawable.sample_moderate_pothole,
                reportedAt = "Oct 19, 2024",
                updatedAt = now,
                notes = "Roadway cavern and circular depression on avenue marked by amber spray paint.",
                depthCm = 8.5f,
                distanceDisplay = "1.8 mi away",
                statusNote = "Scheduled for Patching • Nov 02"
            ),
            // 3. Repaired & Verified Report from screenshot
            Pothole(
                id = "pothole-69120",
                ticketNumber = "#RV-69120",
                lat = 37.7885,
                lon = -122.4005,
                city = "Metro City",
                address = "Bay St & Richmond West",
                laneInfo = "Verified by 89 Telemetry Sweeps",
                severity = Severity.MINOR,
                status = PotholeStatus.VERIFIED_FIXED,
                confirmationCount = 89,
                fixedConfirmationCount = 14,
                drawableResId = R.drawable.sample_clean_road,
                reportedAt = "Oct 14, 2024",
                updatedAt = now,
                notes = "Freshly paved smooth bitumen asphalt patch over previously fractured roadway.",
                depthCm = 0.0f,
                distanceDisplay = "3.1 mi away",
                statusNote = "Resolved & Verified by Riders"
            ),
            // Additional In-Progress & Repaired cases to reach 18 total (12 repaired, 5 progress, 1 critical)
            Pothole(
                id = "pothole-77403",
                ticketNumber = "#RV-77301",
                lat = 37.7830,
                lon = -122.4120,
                city = "Metro City",
                address = "Market St & 4th Ave, Downtown",
                laneInfo = "Lane 1 Center Track",
                severity = Severity.MODERATE,
                status = PotholeStatus.IN_PROGRESS,
                confirmationCount = 32,
                fixedConfirmationCount = 1,
                drawableResId = R.drawable.sample_moderate_pothole,
                reportedAt = "Oct 17, 2024",
                updatedAt = now,
                notes = "Edge erosion along light rail track corridor.",
                depthCm = 7.0f,
                distanceDisplay = "0.9 mi away",
                statusNote = "Assigned to Municipal Rapid Patch Unit"
            ),
            Pothole(
                id = "pothole-77404",
                ticketNumber = "#RV-77119",
                lat = 37.7790,
                lon = -122.4180,
                city = "Metro City",
                address = "Mission St & 3rd Ave, South of Market",
                laneInfo = "Right Turn Ingress Lane",
                severity = Severity.MODERATE,
                status = PotholeStatus.IN_PROGRESS,
                confirmationCount = 19,
                fixedConfirmationCount = 1,
                drawableResId = R.drawable.sample_moderate_pothole,
                reportedAt = "Oct 16, 2024",
                updatedAt = now,
                notes = "Cracked manhole transition depression.",
                depthCm = 6.2f,
                distanceDisplay = "1.2 mi away",
                statusNote = "Scheduled for Patching • Oct 30"
            ),
            Pothole(
                id = "pothole-77405",
                ticketNumber = "#RV-76890",
                lat = 37.7710,
                lon = -122.4240,
                city = "Metro City",
                address = "Valencia St at 16th St",
                laneInfo = "Bicycle Buffer Lane",
                severity = Severity.MODERATE,
                status = PotholeStatus.IN_PROGRESS,
                confirmationCount = 27,
                fixedConfirmationCount = 2,
                drawableResId = R.drawable.sample_moderate_pothole,
                reportedAt = "Oct 15, 2024",
                updatedAt = now,
                notes = "Drainage grating asphalt cavity.",
                depthCm = 7.8f,
                distanceDisplay = "2.1 mi away",
                statusNote = "Work Order Created by Public Works"
            ),
            Pothole(
                id = "pothole-77406",
                ticketNumber = "#RV-76722",
                lat = 37.7650,
                lon = -122.4300,
                city = "Metro City",
                address = "Castro St & Market",
                laneInfo = "Crosswalk approach",
                severity = Severity.MODERATE,
                status = PotholeStatus.IN_PROGRESS,
                confirmationCount = 18,
                fixedConfirmationCount = 1,
                drawableResId = R.drawable.sample_moderate_pothole,
                reportedAt = "Oct 13, 2024",
                updatedAt = now,
                notes = "Subsurface aggregate degradation.",
                depthCm = 8.1f,
                distanceDisplay = "2.7 mi away",
                statusNote = "Scheduled for Patching • Nov 05"
            ),
            // Repaired items (completing 12 repaired)
            Pothole(
                id = "pothole-69121",
                ticketNumber = "#RV-69001",
                lat = 37.7900,
                lon = -122.4050,
                city = "Metro City",
                address = "Pine Street Diagonal",
                laneInfo = "Dual Lane Centerline",
                severity = Severity.MINOR,
                status = PotholeStatus.VERIFIED_FIXED,
                confirmationCount = 64,
                fixedConfirmationCount = 12,
                drawableResId = R.drawable.sample_clean_road,
                reportedAt = "Oct 10, 2024",
                updatedAt = now,
                notes = "Hot asphalt inlay completed.",
                distanceDisplay = "3.4 mi away",
                statusNote = "Resolved & Verified by Riders"
            ),
            Pothole(
                id = "pothole-69122",
                ticketNumber = "#RV-68950",
                lat = 37.7920,
                lon = -122.4100,
                city = "Metro City",
                address = "California St at Powell",
                laneInfo = "Cable Car Intersection",
                severity = Severity.MINOR,
                status = PotholeStatus.VERIFIED_FIXED,
                confirmationCount = 78,
                fixedConfirmationCount = 15,
                drawableResId = R.drawable.sample_clean_road,
                reportedAt = "Oct 08, 2024",
                updatedAt = now,
                notes = "Pavement leveled and sealed.",
                distanceDisplay = "3.8 mi away",
                statusNote = "Resolved & Verified by Riders"
            ),
            Pothole(
                id = "pothole-69123",
                ticketNumber = "#RV-68810",
                lat = 37.7950,
                lon = -122.4150,
                city = "Metro City",
                address = "Van Ness Ave Service Corridor",
                laneInfo = "Rapid Transit Adjacent",
                severity = Severity.MINOR,
                status = PotholeStatus.VERIFIED_FIXED,
                confirmationCount = 52,
                fixedConfirmationCount = 9,
                drawableResId = R.drawable.sample_clean_road,
                reportedAt = "Oct 05, 2024",
                updatedAt = now,
                notes = "Structural seal applied.",
                distanceDisplay = "4.1 mi away",
                statusNote = "Resolved & Verified by Riders"
            ),
            Pothole(
                id = "pothole-69124",
                ticketNumber = "#RV-68700",
                lat = 37.8000,
                lon = -122.4200,
                city = "Metro City",
                address = "Lombard St Commercial strip",
                laneInfo = "Westbound Curb",
                severity = Severity.MINOR,
                status = PotholeStatus.VERIFIED_FIXED,
                confirmationCount = 71,
                fixedConfirmationCount = 11,
                drawableResId = R.drawable.sample_clean_road,
                reportedAt = "Oct 03, 2024",
                updatedAt = now,
                notes = "Deep patch repair verified smooth.",
                distanceDisplay = "4.5 mi away",
                statusNote = "Resolved & Verified by Riders"
            ),
            Pothole(
                id = "pothole-69125",
                ticketNumber = "#RV-68620",
                lat = 37.8050,
                lon = -122.4250,
                city = "Metro City",
                address = "Chestnut St & Pierce",
                laneInfo = "Intersection Apron",
                severity = Severity.MINOR,
                status = PotholeStatus.VERIFIED_FIXED,
                confirmationCount = 60,
                fixedConfirmationCount = 10,
                drawableResId = R.drawable.sample_clean_road,
                reportedAt = "Oct 01, 2024",
                updatedAt = now,
                notes = "Full road resurfacing completed.",
                distanceDisplay = "4.9 mi away",
                statusNote = "Resolved & Verified by Riders"
            ),
            Pothole(
                id = "pothole-69126",
                ticketNumber = "#RV-68500",
                lat = 37.7600,
                lon = -122.4350,
                city = "Metro City",
                address = "17th St & Church",
                laneInfo = "Bicycle Lane Edge",
                severity = Severity.MINOR,
                status = PotholeStatus.VERIFIED_FIXED,
                confirmationCount = 43,
                fixedConfirmationCount = 8,
                drawableResId = R.drawable.sample_clean_road,
                reportedAt = "Sep 28, 2024",
                updatedAt = now,
                notes = "Thermoplastic marked and leveled.",
                distanceDisplay = "5.2 mi away",
                statusNote = "Resolved & Verified by Riders"
            ),
            Pothole(
                id = "pothole-69127",
                ticketNumber = "#RV-68420",
                lat = 37.7550,
                lon = -122.4400,
                city = "Metro City",
                address = "Portola Dr Curved Descent",
                laneInfo = "High-speed descent lane",
                severity = Severity.MINOR,
                status = PotholeStatus.VERIFIED_FIXED,
                confirmationCount = 95,
                fixedConfirmationCount = 18,
                drawableResId = R.drawable.sample_clean_road,
                reportedAt = "Sep 25, 2024",
                updatedAt = now,
                notes = "Emergency patch completed within 48h.",
                distanceDisplay = "5.6 mi away",
                statusNote = "Resolved & Verified by Riders"
            ),
            Pothole(
                id = "pothole-69128",
                ticketNumber = "#RV-68310",
                lat = 37.7500,
                lon = -122.4450,
                city = "Metro City",
                address = "Ocean Ave Eastbound",
                laneInfo = "T-junction merge",
                severity = Severity.MINOR,
                status = PotholeStatus.VERIFIED_FIXED,
                confirmationCount = 57,
                fixedConfirmationCount = 10,
                drawableResId = R.drawable.sample_clean_road,
                reportedAt = "Sep 22, 2024",
                updatedAt = now,
                notes = "Road crew asphalt fill verified.",
                distanceDisplay = "6.1 mi away",
                statusNote = "Resolved & Verified by Riders"
            ),
            Pothole(
                id = "pothole-69129",
                ticketNumber = "#RV-68200",
                lat = 37.7450,
                lon = -122.4500,
                city = "Metro City",
                address = "Sloat Blvd Expressway",
                laneInfo = "Right lane median edge",
                severity = Severity.MINOR,
                status = PotholeStatus.VERIFIED_FIXED,
                confirmationCount = 81,
                fixedConfirmationCount = 14,
                drawableResId = R.drawable.sample_clean_road,
                reportedAt = "Sep 20, 2024",
                updatedAt = now,
                notes = "Cold mix replaced with permanent bitumen.",
                distanceDisplay = "6.5 mi away",
                statusNote = "Resolved & Verified by Riders"
            ),
            Pothole(
                id = "pothole-69130",
                ticketNumber = "#RV-68110",
                lat = 37.7400,
                lon = -122.4550,
                city = "Metro City",
                address = "Sunset Blvd Corridor",
                laneInfo = "Median crossover",
                severity = Severity.MINOR,
                status = PotholeStatus.VERIFIED_FIXED,
                confirmationCount = 68,
                fixedConfirmationCount = 11,
                drawableResId = R.drawable.sample_clean_road,
                reportedAt = "Sep 18, 2024",
                updatedAt = now,
                notes = "Re-graded and verified smooth.",
                distanceDisplay = "7.0 mi away",
                statusNote = "Resolved & Verified by Riders"
            ),
            Pothole(
                id = "pothole-69131",
                ticketNumber = "#RV-68005",
                lat = 37.7350,
                lon = -122.4600,
                city = "Metro City",
                address = "Great Highway Coastal Parkway",
                laneInfo = "Southbound Coast Lane",
                severity = Severity.MINOR,
                status = PotholeStatus.VERIFIED_FIXED,
                confirmationCount = 110,
                fixedConfirmationCount = 22,
                drawableResId = R.drawable.sample_clean_road,
                reportedAt = "Sep 15, 2024",
                updatedAt = now,
                notes = "Coastal erosion damage repaired.",
                distanceDisplay = "7.5 mi away",
                statusNote = "Resolved & Verified by Riders"
            )
        )
    }

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
            val randomTicket = "#RV-${(88220..88999).random()}"
            val newPothole = Pothole(
                id = "pothole-${UUID.randomUUID().toString().take(8)}",
                ticketNumber = randomTicket,
                lat = lat,
                lon = lon,
                city = GeoMatchingService.detectCity(lat, lon),
                address = address.ifBlank { "Market St & 4th Ave, Downtown" },
                laneInfo = "Lane 1, Center Track",
                severity = severity,
                status = PotholeStatus.ACTIVE,
                confirmationCount = 1,
                fixedConfirmationCount = 0,
                drawableResId = drawableResId ?: R.drawable.sample_severe_pothole,
                reportedAt = "Oct 24, 2024",
                updatedAt = now,
                notes = notes,
                depthCm = 14.2f,
                distanceDisplay = "0.1 mi away",
                statusNote = "Under Investigation by City Works"
            )
            _potholes.value = listOf(newPothole) + currentList
            ReportResult.Created(newPothole)
        }
    }

    fun confirmStillThere(potholeId: String, userId: String = "commuter-user-1"): VoteResult {
        val key = "$userId-$potholeId"
        val lastVote = userVoteHistory[key]
        val nowMs = System.currentTimeMillis()
        val cooldownMs = 7L * 24 * 60 * 60 * 1000

        if (lastVote != null && (nowMs - lastVote) < cooldownMs) {
            return VoteResult.CooldownActive("Cooldown: You already confirmed this hazard recently.")
        }

        val target = _potholes.value.firstOrNull { it.id == potholeId }
            ?: return VoteResult.CooldownActive("Hazard not found.")

        userVoteHistory[key] = nowMs
        val updated = target.copy(
            confirmationCount = target.confirmationCount + 1,
            updatedAt = getNowIso()
        )
        _potholes.value = _potholes.value.map { if (it.id == potholeId) updated else it }
        return VoteResult.Success(updated, "Verified! +1 confirmation recorded for road authorities.")
    }

    fun confirmFixed(potholeId: String, userId: String = "commuter-user-1"): VoteResult {
        val key = "$userId-fix-$potholeId"
        val lastVote = userVoteHistory[key]
        val nowMs = System.currentTimeMillis()
        val cooldownMs = 7L * 24 * 60 * 60 * 1000

        if (lastVote != null && (nowMs - lastVote) < cooldownMs) {
            return VoteResult.CooldownActive("Cooldown: You already voted on this repair.")
        }

        val target = _potholes.value.firstOrNull { it.id == potholeId }
            ?: return VoteResult.CooldownActive("Hazard not found.")

        userVoteHistory[key] = nowMs
        val newFixedCount = target.fixedConfirmationCount + 1
        val newStatus = if (newFixedCount >= 3) {
            PotholeStatus.VERIFIED_FIXED
        } else {
            PotholeStatus.IN_PROGRESS
        }

        val updated = target.copy(
            fixedConfirmationCount = newFixedCount,
            status = newStatus,
            updatedAt = getNowIso()
        )
        _potholes.value = _potholes.value.map { if (it.id == potholeId) updated else it }
        return VoteResult.Success(updated, "Repair vote logged ($newFixedCount/3).")
    }
}
