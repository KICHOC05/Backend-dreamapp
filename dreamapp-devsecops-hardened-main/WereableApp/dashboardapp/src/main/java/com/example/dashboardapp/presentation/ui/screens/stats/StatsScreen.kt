package com.example.dashboardapp.presentation.ui.screens.stats

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.dashboardapp.domain.model.user.User
import com.example.dashboardapp.presentation.ui.components.user.CardInfoUser
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import android.util.Log
import com.example.dashboardapp.domain.model.auth.UserInfo
import com.example.dashboardapp.domain.utils.formatNameWithPlus
import com.example.dashboardapp.presentation.ui.components.charts.efficiency.ChartEfficiencySleepPredictionNextMonth
import com.example.dashboardapp.presentation.viewmodel.sleep.SleepStatsViewModel
import com.example.dashboardapp.presentation.viewmodel.sleep.SleepStatsUiState
import com.example.dashboardapp.presentation.ui.components.charts.efficiency.ChartsEfficiencySleep
import com.example.dashboardapp.presentation.viewmodel.sleep.SleepPredictionEfficiencyNextMonthViewModel
import com.example.dashboardapp.presentation.ui.components.sleep.CardStatsLastDay

@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(user: User, navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = user.pictureUrl,
                            contentDescription = "Imagen del perfil",
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = formatNameWithPlus(user.name),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            val sleepStatsViewModel: SleepStatsViewModel = hiltViewModel()
            val sleepPredictionEfficiencyNextMonthViewModel: SleepPredictionEfficiencyNextMonthViewModel =
                hiltViewModel()
            val uiState by sleepStatsViewModel.uiState.collectAsState()
            val uiStatePrediction by sleepPredictionEfficiencyNextMonthViewModel.uiState.collectAsState()

            LaunchedEffect(user) {
                sleepStatsViewModel.loadSleepStats(user.id)
                sleepPredictionEfficiencyNextMonthViewModel.loadSleepPredictionEfficiencyNextMonth(
                    user.id
                )
            }

            // Log en formato JSON para ver la respuesta
            LaunchedEffect(uiState) {
                val currentState = uiState // Extraer a variable local para smart cast
                when (currentState) {
                    is SleepStatsUiState.Loading -> {
                        Log.d("SleepStatsJSON", "Loading...")
                    }
                    is SleepStatsUiState.Success -> {
                        val data = currentState.data
                        val jsonLog = """
                        {
                          "sleepStats": {
                            "statsLastDay": {
                              "sleepEfficiency": ${data.statsLastDay.sleepEfficiency},
                              "sleepDuration": ${data.statsLastDay.sleepDuration},
                              "light": ${data.statsLastDay.light},
                              "deep": ${data.statsLastDay.deep},
                              "rem": ${data.statsLastDay.rem},
                              "awake": ${data.statsLastDay.awake},
                              "avgHR": ${data.statsLastDay.avgHR},
                              "awakenings": ${data.statsLastDay.awakenings}
                            },
                            "averagesLastWeek": {
                              "sleepEfficiency": ${data.averagesLastWeek.sleepEfficiency},
                              "sleepDuration": ${data.averagesLastWeek.sleepDuration},
                              "light": ${data.averagesLastWeek.light},
                              "deep": ${data.averagesLastWeek.deep},
                              "rem": ${data.averagesLastWeek.rem},
                              "awake": ${data.averagesLastWeek.awake},
                              "avgHR": ${data.averagesLastWeek.avgHR},
                              "awakenings": ${data.averagesLastWeek.awakenings}
                            },
                            "quality": {
                              "good": ${data.quality.good},
                              "fair": ${data.quality.fair},
                              "poor": ${data.quality.poor},
                              "excellent": ${data.quality.excellent}
                            },
                            "efficiency": {
                              "last7DaysCount": ${data.efficiency.last7Days.size},
                              "lastMonthCount": ${data.efficiency.lastMonth.size},
                              "last6MonthsCount": ${data.efficiency.last6Months.size},
                              "lastYearCount": ${data.efficiency.lastYear.size}
                            }
                          }
                        }
                        """.trimIndent()
                        Log.d("SleepStatsJSON", jsonLog)
                    }
                    is SleepStatsUiState.Error -> {
                        Log.e("SleepStatsJSON", """{"error": "${currentState.message}"}""")
                    }
                }
            }

            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val isTablet = maxWidth > 600.dp

                LazyColumn(modifier = Modifier.fillMaxSize()) {

                    if (isTablet) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxSize().padding(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    CardInfoUser(user)
                                }
                                Box(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    CardStatsLastDay(uiState)
                                }
                            }
                        }
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                ) {
                                    ChartsEfficiencySleep(uiState)
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                ) {
                                    ChartEfficiencySleepPredictionNextMonth(uiStatePrediction)
                                }
                            }
                        }
                    } else {
                        item { CardInfoUser(user) }
                        item { CardStatsLastDay(uiState) }
                        item { ChartsEfficiencySleep(uiState) }
                        item { ChartEfficiencySleepPredictionNextMonth(uiStatePrediction) }
                    }
                }
            }
        }
    }
}