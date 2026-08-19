package com.example.dashboardapp.presentation.ui.components.charts.efficiency

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.compose.common.shader.verticalGradient
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.core.common.shader.ShaderProvider
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.example.dashboardapp.presentation.ui.components.charts.rememberReusableMarker
import java.text.DecimalFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val YDecimalFormat = DecimalFormat("#.##'%'")
private val StartAxisValueFormatter = CartesianValueFormatter.decimal(YDecimalFormat)
private val MarkerValueFormatter = DefaultCartesianMarker.ValueFormatter.default(YDecimalFormat)

@Composable
fun LineChart(
    x: List<String>,
    y: List<Float>,
    title: String = "",
    description: String = "",
    modifier: Modifier = Modifier,
    xValueFormatter: ((List<String>, Float) -> String)? = null,
) {
    val lineColor = MaterialTheme.colorScheme.primary
    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(x, y) {
        modelProducer.runTransaction {
            lineSeries { series(x.indices.toList(), y) }
        }
    }

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {

        if (title.isNotBlank()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = "Sleep Stats",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
        }

        if (description.isNotBlank()) {
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        CartesianChartHost(
            rememberCartesianChart(
                rememberLineCartesianLayer(
                    lineProvider =
                        LineCartesianLayer.LineProvider.series(
                            LineCartesianLayer.rememberLine(
                                fill = LineCartesianLayer.LineFill.single(fill(lineColor)),
                                areaFill =
                                    LineCartesianLayer.AreaFill.single(
                                        fill(
                                            ShaderProvider.verticalGradient(
                                                arrayOf(lineColor.copy(alpha = 0.4f), Color.Transparent)
                                            )
                                        )
                                    ),
                            )
                        ),
                    rangeProvider = CartesianLayerRangeProvider.fixed(minY = 0.0, maxY = 100.0),
                ),
                startAxis = VerticalAxis.rememberStart(valueFormatter = StartAxisValueFormatter),
                bottomAxis = HorizontalAxis.rememberBottom(
                    valueFormatter = CartesianValueFormatter { _, value, _ ->
                        val index = when (value) {
                            is Float -> value.toInt()
                            is Double -> value.toInt()
                            else -> value.toString().toIntOrNull() ?: 0
                        }
                        val label = if (xValueFormatter != null) {
                            xValueFormatter(x, index.toFloat())
                        } else {
                            x.getOrNull(index)?.let { dateStr ->
                                try {
                                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                                    val date = LocalDate.parse(dateStr, formatter)
                                    date.dayOfWeek.name.substring(0, 3).lowercase().replaceFirstChar { it.uppercase() }
                                } catch (e: Exception) {
                                    "-"
                                }
                            } ?: "-"
                        }
                        if (label.isBlank()) "-" else label
                    }
                ),
                marker = rememberReusableMarker(MarkerValueFormatter),
            ),
            modelProducer,
            Modifier.height(220.dp),
            rememberVicoScrollState(scrollEnabled = false),
        )
    }
}