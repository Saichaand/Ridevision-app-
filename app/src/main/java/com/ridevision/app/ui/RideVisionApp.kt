package com.ridevision.app.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ridevision.app.data.model.Pothole
import com.ridevision.app.data.model.Severity
import com.ridevision.app.ui.components.HazardWarningBanner
import com.ridevision.app.ui.screens.CivicDeskScreen
import com.ridevision.app.ui.screens.DetectorScreen
import com.ridevision.app.ui.screens.RadarMapScreen
import com.ridevision.app.ui.screens.ReportDialog
import com.ridevision.app.ui.screens.TripModeScreen
import com.ridevision.app.ui.theme.CockpitBackground
import com.ridevision.app.ui.theme.CockpitCardBorder
import com.ridevision.app.ui.theme.CockpitSurface
import com.ridevision.app.ui.theme.CockpitSurfaceVariant
import com.ridevision.app.ui.theme.CyanAccent
import com.ridevision.app.ui.theme.TextMuted
import com.ridevision.app.ui.theme.TextPrimary
import com.ridevision.app.ui.viewmodel.AppTab
import com.ridevision.app.ui.viewmodel.RideVisionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RideVisionApp(
    viewModel: RideVisionViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentTab by viewModel.currentTab.collectAsState()
    val activeWarning by viewModel.activeWarning.collectAsState()
    val currentLat by viewModel.currentLat.collectAsState()
    val currentLon by viewModel.currentLon.collectAsState()

    var showReportDialog by remember { mutableStateOf(false) }
    var reportInitialSeverity by remember { mutableStateOf(Severity.SEVERE) }
    var dispatchTargetPothole by remember { mutableStateOf<Pothole?>(null) }

    // Listen for feedback toast notifications
    LaunchedEffect(Unit) {
        viewModel.userFeedback.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        containerColor = CockpitBackground,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(CyanAccent.copy(alpha = 0.2f))
                                .border(1.dp, CyanAccent, RoundedCornerShape(8.dp))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = CyanAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = "RideVision",
                                color = TextPrimary,
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "CV Road Safety & Civic Dispatch",
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        }
                    }
                },
                actions = {
                    // Mangaluru / City jurisdiction chip
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(CockpitSurfaceVariant)
                            .border(1.dp, CockpitCardBorder, RoundedCornerShape(14.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(CyanAccent)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "MCC / BBMP Active",
                                color = TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CockpitSurface
                ),
                windowInsets = WindowInsets.statusBars
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = CockpitSurface,
                contentColor = TextPrimary,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                NavigationBarItem(
                    selected = currentTab == AppTab.SCANNER,
                    onClick = { viewModel.setTab(AppTab.SCANNER) },
                    icon = { Icon(Icons.Default.CameraAlt, contentDescription = "AI Vision") },
                    label = { Text("AI Vision", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = CyanAccent,
                        indicatorColor = CyanAccent,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    )
                )

                NavigationBarItem(
                    selected = currentTab == AppTab.RADAR,
                    onClick = { viewModel.setTab(AppTab.RADAR) },
                    icon = { Icon(Icons.Default.Radar, contentDescription = "Hazard Radar") },
                    label = { Text("Radar", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = CyanAccent,
                        indicatorColor = CyanAccent,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    )
                )

                NavigationBarItem(
                    selected = currentTab == AppTab.TRIP,
                    onClick = { viewModel.setTab(AppTab.TRIP) },
                    icon = { Icon(Icons.Default.DirectionsCar, contentDescription = "Drive HUD") },
                    label = { Text("Drive HUD", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = CyanAccent,
                        indicatorColor = CyanAccent,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    )
                )

                NavigationBarItem(
                    selected = currentTab == AppTab.CIVIC,
                    onClick = { viewModel.setTab(AppTab.CIVIC) },
                    icon = { Icon(Icons.Default.LocationCity, contentDescription = "Civic Desk") },
                    label = { Text("Civic Desk", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = CyanAccent,
                        indicatorColor = CyanAccent,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    )
                )
            }
        },
        floatingActionButton = {
            if (currentTab == AppTab.RADAR || currentTab == AppTab.SCANNER) {
                FloatingActionButton(
                    onClick = {
                        reportInitialSeverity = Severity.SEVERE
                        showReportDialog = true
                    },
                    containerColor = CyanAccent,
                    contentColor = Color.Black,
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Report Road Hazard"
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Proactive 250m Directional Warning Banner
            HazardWarningBanner(
                warning = activeWarning,
                onDismiss = { viewModel.dismissWarning() },
                onViewRadar = { viewModel.setTab(AppTab.RADAR) }
            )

            // Primary Tab Content
            when (currentTab) {
                AppTab.SCANNER -> {
                    DetectorScreen(
                        viewModel = viewModel,
                        onOpenReportDialog = { severity ->
                            reportInitialSeverity = severity
                            showReportDialog = true
                        }
                    )
                }
                AppTab.RADAR -> {
                    RadarMapScreen(
                        viewModel = viewModel,
                        onSelectPotholeForDispatch = { p ->
                            dispatchTargetPothole = p
                            viewModel.setTab(AppTab.CIVIC)
                        }
                    )
                }
                AppTab.TRIP -> {
                    TripModeScreen(viewModel = viewModel)
                }
                AppTab.CIVIC -> {
                    CivicDeskScreen(
                        viewModel = viewModel,
                        preselectedPothole = dispatchTargetPothole
                    )
                }
            }
        }

        // Report Hazard Dialog
        if (showReportDialog) {
            ReportDialog(
                initialSeverity = reportInitialSeverity,
                currentLat = currentLat,
                currentLon = currentLon,
                onDismiss = { showReportDialog = false },
                onSubmit = { address, lat, lon, severity, notes ->
                    viewModel.submitHazardReport(lat, lon, address, severity, notes)
                    showReportDialog = false
                }
            )
        }
    }
}
