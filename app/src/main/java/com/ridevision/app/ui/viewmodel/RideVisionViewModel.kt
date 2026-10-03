package com.ridevision.app.ui.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ridevision.app.R
import com.ridevision.app.data.model.DetectionResult
import com.ridevision.app.data.model.HazardWarning
import com.ridevision.app.data.model.MunicipalDispatch
import com.ridevision.app.data.model.PassedHazard
import com.ridevision.app.data.model.Pothole
import com.ridevision.app.data.model.PotholeStatus
import com.ridevision.app.data.model.Severity
import com.ridevision.app.data.model.TripPoint
import com.ridevision.app.data.repository.PotholeRepository
import com.ridevision.app.data.repository.ReportResult
import com.ridevision.app.data.repository.VoteResult
import com.ridevision.app.domain.detector.RoadHazardDetector
import com.ridevision.app.domain.geo.GeoMatchingService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class AppTab(val label: String) {
    SCANNER("AI Vision"),
    RADAR("Hazard Radar"),
    TRIP("Drive HUD"),
    CIVIC("Civic Desk")
}

enum class SamplePreset(val label: String, val resId: Int?) {
    SAMPLE_SEVERE("Severe Hazard", R.drawable.sample_severe_pothole),
    SAMPLE_MODERATE("Moderate Hazard", R.drawable.sample_moderate_pothole),
    SAMPLE_CLEAN("Clean Road", R.drawable.sample_clean_road),
    CAMERA_CAPTURE("Camera / Pick", null)
}

data class CommuteRouteStep(
    val name: String,
    val lat: Double,
    val lon: Double,
    val heading: Float,
    val speedKmh: Float
)

class RideVisionViewModel(
    private val repository: PotholeRepository = PotholeRepository()
) : ViewModel() {

    val potholes: StateFlow<List<Pothole>> = repository.potholes

    private val _currentTab = MutableStateFlow(AppTab.SCANNER)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    // Location & Vehicle Telemetry
    private val _currentLat = MutableStateFlow(12.9130) // Near SJEC Mangaluru
    val currentLat: StateFlow<Double> = _currentLat.asStateFlow()

    private val _currentLon = MutableStateFlow(74.8970)
    val currentLon: StateFlow<Double> = _currentLon.asStateFlow()

    private val _currentHeading = MutableStateFlow(45f) // Heading Northeast towards SJEC gate
    val currentHeading: StateFlow<Float> = _currentHeading.asStateFlow()

    private val _currentSpeedKmh = MutableStateFlow(42f)
    val currentSpeedKmh: StateFlow<Float> = _currentSpeedKmh.asStateFlow()

    private val _activeWarning = MutableStateFlow<HazardWarning?>(null)
    val activeWarning: StateFlow<HazardWarning?> = _activeWarning.asStateFlow()

    // Vision Detection State
    private val _selectedPreset = MutableStateFlow(SamplePreset.SAMPLE_SEVERE)
    val selectedPreset: StateFlow<SamplePreset> = _selectedPreset.asStateFlow()

    private val _currentBitmap = MutableStateFlow<Bitmap?>(null)
    val currentBitmap: StateFlow<Bitmap?> = _currentBitmap.asStateFlow()

    private val _detectionResult = MutableStateFlow<DetectionResult?>(null)
    val detectionResult: StateFlow<DetectionResult?> = _detectionResult.asStateFlow()

    // Filters
    private val _cityFilter = MutableStateFlow("All")
    val cityFilter: StateFlow<String> = _cityFilter.asStateFlow()

    private val _severityFilter = MutableStateFlow("All")
    val severityFilter: StateFlow<String> = _severityFilter.asStateFlow()

    // Trip Session
    private val _isTripActive = MutableStateFlow(false)
    val isTripActive: StateFlow<Boolean> = _isTripActive.asStateFlow()

    private val _tripDistanceMeters = MutableStateFlow(0.0)
    val tripDistanceMeters: StateFlow<Double> = _tripDistanceMeters.asStateFlow()

    private val _tripPassedHazards = MutableStateFlow<List<PassedHazard>>(emptyList())
    val tripPassedHazards: StateFlow<List<PassedHazard>> = _tripPassedHazards.asStateFlow()

    // Simulation
    private val _isSimulationPlaying = MutableStateFlow(false)
    val isSimulationPlaying: StateFlow<Boolean> = _isSimulationPlaying.asStateFlow()

    private var simulationJob: Job? = null
    private var simulationIndex = 0

    // Notification / Toast Events
    private val _userFeedback = MutableSharedFlow<String>()
    val userFeedback: SharedFlow<String> = _userFeedback.asSharedFlow()

    // Mangaluru simulation route approaching NH 73 SJEC gate
    private val mangaluruRoute = listOf(
        CommuteRouteStep("Vamanjoor Junction Approach", 12.9110, 74.8945, 45f, 48f),
        CommuteRouteStep("NH 73 Corridor (230m from hazard)", 12.9135, 74.8970, 42f, 44f),
        CommuteRouteStep("NH 73 SJEC Gate Hazard Proximity", 12.9150, 74.8986, 40f, 25f),
        CommuteRouteStep("Vamanjoor Post Office Pass", 12.9168, 74.9002, 45f, 38f),
        CommuteRouteStep("Moodbidri Highway Acceleration", 12.9190, 74.9025, 50f, 52f)
    )

    init {
        // Initial warning check
        checkWarning()
    }

    fun setTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun setCityFilter(city: String) {
        _cityFilter.value = city
    }

    fun setSeverityFilter(severity: String) {
        _severityFilter.value = severity
    }

    fun updateLocation(lat: Double, lon: Double, heading: Float, speed: Float) {
        _currentLat.value = lat
        _currentLon.value = lon
        _currentHeading.value = heading
        _currentSpeedKmh.value = speed
        checkWarning()
        if (_isTripActive.value) {
            checkPassedHazards(lat, lon)
        }
    }

    private fun checkWarning() {
        val warning = GeoMatchingService.checkWarningAhead(
            currentLat = _currentLat.value,
            currentLon = _currentLon.value,
            headingDeg = _currentHeading.value,
            potholes = repository.potholes.value
        )
        _activeWarning.value = warning
    }

    fun dismissWarning() {
        _activeWarning.value = null
    }

    // Vision Detection
    fun loadPreset(context: Context, preset: SamplePreset) {
        _selectedPreset.value = preset
        preset.resId?.let { resId ->
            val bmp = BitmapFactory.decodeResource(context.resources, resId)
            _currentBitmap.value = bmp
            val isClean = (preset == SamplePreset.SAMPLE_CLEAN)
            runDetection(bmp, isClean)
        }
    }

    fun setCustomBitmap(bitmap: Bitmap) {
        _selectedPreset.value = SamplePreset.CAMERA_CAPTURE
        _currentBitmap.value = bitmap
        runDetection(bitmap, false)
    }

    fun runDetection(bitmap: Bitmap, isClean: Boolean = false) {
        viewModelScope.launch {
            val result = RoadHazardDetector.analyzeBitmap(bitmap, isClean)
            _detectionResult.value = result
        }
    }

    // Voting & Crowdsourcing
    fun voteStillThere(potholeId: String) {
        viewModelScope.launch {
            when (val res = repository.confirmStillThere(potholeId)) {
                is VoteResult.Success -> {
                    _userFeedback.emit(res.message)
                    checkWarning()
                }
                is VoteResult.CooldownActive -> {
                    _userFeedback.emit(res.message)
                }
            }
        }
    }

    fun voteFixed(potholeId: String) {
        viewModelScope.launch {
            when (val res = repository.confirmFixed(potholeId)) {
                is VoteResult.Success -> {
                    _userFeedback.emit(res.message)
                    checkWarning()
                }
                is VoteResult.CooldownActive -> {
                    _userFeedback.emit(res.message)
                }
            }
        }
    }

    // Reporting
    fun submitHazardReport(
        lat: Double,
        lon: Double,
        address: String,
        severity: Severity,
        notes: String
    ) {
        viewModelScope.launch {
            val res = repository.submitReport(lat, lon, address, severity, notes)
            when (res) {
                is ReportResult.Created -> {
                    _userFeedback.emit("Hazard Registered! ID: ${res.pothole.id} (${res.pothole.city})")
                }
                is ReportResult.MergedExisting -> {
                    _userFeedback.emit("15m Deduplication: Merged with existing hazard ${res.distanceMeters}m away (+1 verified)!")
                }
            }
            checkWarning()
        }
    }

    // Trip Session Management
    fun toggleTrip() {
        val willBeActive = !_isTripActive.value
        _isTripActive.value = willBeActive
        if (willBeActive) {
            _tripDistanceMeters.value = 0.0
            _tripPassedHazards.value = emptyList()
            viewModelScope.launch {
                _userFeedback.emit("Commuter Drive Mode Started — 250m Radar Active")
            }
        } else {
            viewModelScope.launch {
                _userFeedback.emit("Trip Completed. Passed ${_tripPassedHazards.value.size} registered hazard(s).")
            }
        }
    }

    private fun checkPassedHazards(lat: Double, lon: Double) {
        val all = repository.potholes.value
        val passed = _tripPassedHazards.value.toMutableList()

        for (p in all) {
            val dist = GeoMatchingService.haversineDistanceM(lat, lon, p.lat, p.lon).toFloat()
            if (dist <= 28f && passed.none { it.pothole.id == p.id }) {
                passed.add(PassedHazard(p, dist))
            }
        }
        _tripPassedHazards.value = passed
    }

    // Route Simulation
    fun toggleSimulation() {
        if (_isSimulationPlaying.value) {
            stopSimulation()
        } else {
            startSimulation()
        }
    }

    private fun startSimulation() {
        _isSimulationPlaying.value = true
        simulationJob?.cancel()
        simulationJob = viewModelScope.launch {
            _userFeedback.emit("Simulation active: Commuter driving on NH 73 Mangaluru corridor")
            while (isActive && _isSimulationPlaying.value) {
                val step = mangaluruRoute[simulationIndex]
                _currentLat.value = step.lat
                _currentLon.value = step.lon
                _currentHeading.value = step.heading
                _currentSpeedKmh.value = step.speedKmh

                if (_isTripActive.value) {
                    _tripDistanceMeters.value += 120.0
                    checkPassedHazards(step.lat, step.lon)
                }

                checkWarning()

                delay(3000)
                simulationIndex = (simulationIndex + 1) % mangaluruRoute.size
            }
        }
    }

    fun stopSimulation() {
        _isSimulationPlaying.value = false
        simulationJob?.cancel()
        simulationJob = null
    }

    fun stepSimulation() {
        simulationIndex = (simulationIndex + 1) % mangaluruRoute.size
        val step = mangaluruRoute[simulationIndex]
        updateLocation(step.lat, step.lon, step.heading, step.speedKmh)
    }

    fun getMunicipalDispatch(pothole: Pothole): MunicipalDispatch {
        return GeoMatchingService.routeComplaint(
            city = pothole.city,
            address = pothole.address,
            lat = pothole.lat,
            lon = pothole.lon,
            severity = pothole.severity,
            notes = pothole.notes
        )
    }

    override fun onCleared() {
        super.onCleared()
        simulationJob?.cancel()
    }
}
