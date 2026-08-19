import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import com.example.wereableapp.presentation.presentation.viewmodel.SleepMonitorViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import com.example.wereableapp.presentation.data.repository.UserRepository


fun Double.format(digits: Int) = "%.${digits}f".format(this)

@Composable
fun SleepMonitorScreen(viewModel: SleepMonitorViewModel) {
    val hr by viewModel.heartRate.collectAsState()
    val user by UserRepository.userData.collectAsState()
    val accelerometer by viewModel.accelerometer.collectAsState()
    var isMonitoring by remember { mutableStateOf(false) }
    val phase by viewModel.sleepPhase.collectAsState()
    val hrv by viewModel.hrv.collectAsState()


    Scaffold {
        ScalingLazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.onBackground),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {



            item {
                Text(text = "Fase: ${phase.name}")
            }

            item {
                Text(
                    text = "Heart Rate: ${hr?.bpm ?: "--"} BPM",
                    style = MaterialTheme.typography.body1
                )
            }

            item {
                Text(
                    text = if (hrv != null) {
                        "HRV → RMSSD: ${hrv?.rmssd?.format(1)} ms | SDNN: ${hrv?.sdnn?.format(1)} ms"
                    } else {
                        "HRV: --"
                    },
                    style = MaterialTheme.typography.body1
                )
            }


            item {
                Text(
                    text = "Accelerometer: X=${accelerometer?.x ?: "--"}, " +
                            "Y=${accelerometer?.y ?: "--"}, Z=${accelerometer?.z ?: "--"}",
                    style = MaterialTheme.typography.body1
                )
            }

            item {
                Chip(
                    onClick = {
                        if (isMonitoring) {
                            viewModel.stopMonitoring()
                        } else {
                            viewModel.startMonitoring()
                        }
                        isMonitoring = !isMonitoring
                    },
                    label = {
                        Text(
                            text = if (isMonitoring) "Detener" else "Iniciar",
                            style = MaterialTheme.typography.button
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = if (isMonitoring) Icons.Filled.Clear else Icons.Filled.PlayArrow,
                            contentDescription = null // ya está claro por el texto
                        )
                    },
                    colors = ChipDefaults.chipColors(
                        backgroundColor = if (isMonitoring) MaterialTheme.colors.secondary else MaterialTheme.colors.primary
                    )
                )
            }

            // Test button to send hardcoded data for communication diagnostics
            item {
                Chip(
                    onClick = {
                        viewModel.sendTestData()
                    },
                    label = {
                        Text(
                            text = "Enviar Test",
                            style = MaterialTheme.typography.button
                        )
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.Send,
                            contentDescription = null
                        )
                    },
                    colors = ChipDefaults.chipColors(
                        backgroundColor = MaterialTheme.colors.surface
                    )
                )
                Chip(
                    onClick = { viewModel.sendFakeSleepCycle() },
                    label = { Text("Enviar Ciclo", style = MaterialTheme.typography.button) },
                    icon = { Icon(imageVector = Icons.Filled.Send, contentDescription = null) },
                    colors = ChipDefaults.chipColors(backgroundColor = MaterialTheme.colors.surface)
                )

            }
            item {
                androidx.compose.foundation.layout.Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "👤 Usuario", style = MaterialTheme.typography.title3)
                    Text(text = "Edad: ${user?.edad ?: "--"}", style = MaterialTheme.typography.body1)
                    Text(text = "Peso: ${user?.peso ?: "--"} kg", style = MaterialTheme.typography.body1)
                    Text(text = "Estatura: ${user?.estatura ?: "--"} cm", style = MaterialTheme.typography.body1)
                    Text(text = "Sexo: ${user?.sexo ?: "--"}", style = MaterialTheme.typography.body1)
                }
            }

        }
    }
}