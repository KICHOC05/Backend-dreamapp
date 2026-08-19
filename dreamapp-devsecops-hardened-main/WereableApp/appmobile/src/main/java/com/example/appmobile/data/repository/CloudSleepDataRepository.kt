package com.example.appmobile.data.repository

import android.util.Log
import com.example.appmobile.data.remote.SleepApiClient
import com.example.appmobile.domain.model.SleepDataUpload
import com.example.appmobile.domain.model.SleepPhaseData
import com.example.appmobile.domain.model.SleepUploadError
import com.example.appmobile.domain.model.SleepUploadResponse
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

class CloudSleepDataRepository {
    
    private val apiService = SleepApiClient.apiService
    private val gson = Gson()
    
    /**
     * Sube datos de sueño simulados a la nube
     */
    suspend fun uploadSampleSleepData(
        userId: String,
        dataType: SleepDataType
    ): Result<SleepUploadResponse> = withContext(Dispatchers.IO) {
        try {
            Log.d("CloudSleepDataRepository", "Subiendo datos de tipo: $dataType para usuario: $userId")
            
            val sleepData = generateSampleData(userId, dataType)
            
            Log.d("CloudSleepDataRepository", "Datos generados: ${gson.toJson(sleepData)}")
            
            val response = apiService.uploadSleepData(sleepData)
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Log.d("CloudSleepDataRepository", "Datos subidos exitosamente: ${gson.toJson(body)}")
                    Result.success(body)
                } else {
                    Log.e("CloudSleepDataRepository", "Respuesta vacía del servidor")
                    Result.failure(Exception("Respuesta vacía del servidor"))
                }
            } else {
                Log.e("CloudSleepDataRepository", "Error del servidor: ${response.code()} - ${response.message()}")
                
                // Manejar errores específicos
                val errorBody = response.errorBody()?.string()
                if (errorBody != null) {
                    try {
                        val error = gson.fromJson(errorBody, SleepUploadError::class.java)
                        Log.e("CloudSleepDataRepository", "Error específico: ${gson.toJson(error)}")
                        
                        when (response.code()) {
                            409 -> Result.failure(Exception("Ya existe una sesión de sueño para esta fecha: ${error.date}"))
                            400 -> Result.failure(Exception("Datos inválidos: ${error.message}"))
                            403 -> Result.failure(Exception("Permisos insuficientes"))
                            else -> Result.failure(Exception("Error del servidor: ${error.message}"))
                        }
                    } catch (e: Exception) {
                        Log.e("CloudSleepDataRepository", "Error parseando respuesta de error", e)
                        Result.failure(Exception("Error del servidor: ${response.message()}"))
                    }
                } else {
                    Result.failure(Exception("Error del servidor: ${response.message()}"))
                }
            }
        } catch (e: Exception) {
            Log.e("CloudSleepDataRepository", "Error de red o inesperado", e)
            Result.failure(e)
        }
    }
    
    /**
     * Genera datos de muestra según el tipo especificado
     */
    private fun generateSampleData(userId: String, dataType: SleepDataType): SleepDataUpload {
        return when (dataType) {
            SleepDataType.POOR_QUALITY -> generatePoorQualitySleepData(userId)
            SleepDataType.FAIR_QUALITY -> generateFairQualitySleepData(userId)
            SleepDataType.GOOD_QUALITY -> generateGoodQualitySleepData(userId)
            SleepDataType.EXCELLENT_QUALITY -> generateExcellentQualitySleepData(userId)
            else -> generateBasicSampleData(userId, dataType)
        }
    }
    
    /**
     * Genera datos básicos para TODAY, YESTERDAY, RANDOM_PAST
     */
    private fun generateBasicSampleData(userId: String, dataType: SleepDataType): SleepDataUpload {
        val calendar = Calendar.getInstance()
        
        // Ajustar la fecha según el tipo de datos
        when (dataType) {
            SleepDataType.TODAY -> {
                // Hoy (puede dar conflicto si ya existe)
            }
            SleepDataType.YESTERDAY -> {
                calendar.add(Calendar.DAY_OF_YEAR, -1)
            }
            SleepDataType.RANDOM_PAST -> {
                // Fecha aleatoria en los últimos 7 días
                val randomDays = -Random.nextInt(1, 7)
                calendar.add(Calendar.DAY_OF_YEAR, randomDays)
            }
            else -> {
                // Fecha aleatoria por defecto
                calendar.add(Calendar.DAY_OF_YEAR, -Random.nextInt(1, 3))
            }
        }
        
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        dateTimeFormat.timeZone = TimeZone.getTimeZone("UTC")
        
        val date = dateFormat.format(calendar.time)
        
        // Hora de inicio (10:30 PM del día anterior)
        val startCalendar = calendar.clone() as Calendar
        startCalendar.add(Calendar.DAY_OF_YEAR, -1)
        startCalendar.set(Calendar.HOUR_OF_DAY, 22)
        startCalendar.set(Calendar.MINUTE, 30)
        startCalendar.set(Calendar.SECOND, 0)
        startCalendar.set(Calendar.MILLISECOND, 0)
        
        // Hora de fin (6:45 AM)
        val endCalendar = calendar.clone() as Calendar
        endCalendar.set(Calendar.HOUR_OF_DAY, 6)
        endCalendar.set(Calendar.MINUTE, 45)
        endCalendar.set(Calendar.SECOND, 0)
        endCalendar.set(Calendar.MILLISECOND, 0)
        
        val startTime = dateTimeFormat.format(startCalendar.time)
        val endTime = dateTimeFormat.format(endCalendar.time)
        
        // Generar datos de fases de sueño
        val sleepPhaseData = generateSleepPhaseData(startCalendar, endCalendar, dateTimeFormat)
        
        return SleepDataUpload(
            uidUser = userId,
            deviceId = "android_mobile_${System.currentTimeMillis()}",
            date = date,
            startTime = startTime,
            endTime = endTime,
            timezone = "America/Mexico_City",
            totalDuration = 480, // 8 horas
            sleepDuration = 420, // 7 horas
            lightSleepMinutes = 200,
            deepSleepMinutes = 120,
            remSleepMinutes = 100,
            awakeDuration = 60,
            sleepEfficiency = 87.5,
            awakeningsCount = 3,
            quality = "GOOD",
            avgHeartRate = Random.nextInt(60, 70),
            minHeartRate = Random.nextInt(45, 55),
            maxHeartRate = Random.nextInt(75, 85),
            avgMovement = Random.nextInt(10, 20),
            avgRmssd = Random.nextDouble(40.0, 50.0),
            avgSdnn = Random.nextDouble(50.0, 60.0),
            sleepPhaseData = sleepPhaseData,
            createdAt = System.currentTimeMillis(),
            dataVersion = "1.0"
        )
    }

    /**
     * Generate sleep data with POOR quality (sleepDataUserSchema.quality = "POOR")
     * Usa la fecha de hoy para consistencia con otros datos
     */
    private fun generatePoorQualitySleepData(userId: String): SleepDataUpload {
        val calendar = Calendar.getInstance() // Fecha de hoy
        
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        dateTimeFormat.timeZone = TimeZone.getTimeZone("UTC")
        
        val date = dateFormat.format(calendar.time)
        
        // Poor sleep: shorter duration, low efficiency
        val totalDurationMinutes = Random.nextInt(360, 480) // 6-8 hours
        val sleepDurationMinutes = (totalDurationMinutes * Random.nextDouble(0.4, 0.6)).toInt() // Low efficiency
        val awakeDurationMinutes = totalDurationMinutes - sleepDurationMinutes
        
        // Poor sleep distribution - mostly light sleep, very little deep/REM
        val lightSleepMinutes = (sleepDurationMinutes * Random.nextDouble(0.70, 0.85)).toInt()
        val deepSleepMinutes = (sleepDurationMinutes * Random.nextDouble(0.05, 0.12)).toInt()
        val remSleepMinutes = sleepDurationMinutes - lightSleepMinutes - deepSleepMinutes
        
        val sleepEfficiency = ((sleepDurationMinutes.toDouble() / totalDurationMinutes.toDouble()) * 100)
        
        // Poor quality physiological indicators
        val avgHeartRate = Random.nextInt(65, 85) // Higher than normal
        val minHeartRate = avgHeartRate - Random.nextInt(3, 8) // Less variation
        val maxHeartRate = avgHeartRate + Random.nextInt(20, 35) // High spikes
        
        // Horarios de sueño para hoy
        val startCalendar = calendar.clone() as Calendar
        startCalendar.add(Calendar.DAY_OF_YEAR, -1)
        startCalendar.set(Calendar.HOUR_OF_DAY, 23)
        startCalendar.set(Calendar.MINUTE, Random.nextInt(0, 59))
        
        val endCalendar = startCalendar.clone() as Calendar
        endCalendar.add(Calendar.MINUTE, totalDurationMinutes)
        
        val sleepPhaseData = generateQualityBasedSleepPhaseData(
            startCalendar, endCalendar, dateTimeFormat, "POOR"
        )
        
        return SleepDataUpload(
            uidUser = userId,
            deviceId = "android_mobile_${System.currentTimeMillis()}",
            date = date,
            startTime = dateTimeFormat.format(startCalendar.time),
            endTime = dateTimeFormat.format(endCalendar.time),
            timezone = "America/Mexico_City",
            totalDuration = totalDurationMinutes,
            sleepDuration = sleepDurationMinutes,
            lightSleepMinutes = lightSleepMinutes,
            deepSleepMinutes = deepSleepMinutes,
            remSleepMinutes = remSleepMinutes,
            awakeDuration = awakeDurationMinutes,
            sleepEfficiency = sleepEfficiency,
            awakeningsCount = Random.nextInt(8, 16), // Many awakenings
            quality = "POOR", // Fixed POOR quality
            avgHeartRate = avgHeartRate,
            minHeartRate = minHeartRate,
            maxHeartRate = maxHeartRate,
            avgMovement = Random.nextInt(40, 70), // High movement
            avgRmssd = Random.nextDouble(15.0, 35.0), // Low HRV
            avgSdnn = Random.nextDouble(20.0, 40.0), // Low HRV
            sleepPhaseData = sleepPhaseData,
            createdAt = System.currentTimeMillis(),
            dataVersion = "1.0"
        )
    }

    /**
     * Generate sleep data with FAIR quality (sleepDataUserSchema.quality = "FAIR")
     * Usa la fecha de hoy para consistencia con otros datos
     */
    private fun generateFairQualitySleepData(userId: String): SleepDataUpload {
        val calendar = Calendar.getInstance() // Fecha de hoy
        
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        dateTimeFormat.timeZone = TimeZone.getTimeZone("UTC")
        
        val date = dateFormat.format(calendar.time)
        
        // Fair sleep: moderate duration and efficiency
        val totalDurationMinutes = Random.nextInt(420, 540) // 7-9 hours
        val sleepDurationMinutes = (totalDurationMinutes * Random.nextDouble(0.6, 0.75)).toInt()
        val awakeDurationMinutes = totalDurationMinutes - sleepDurationMinutes
        
        // Fair sleep distribution - normal but not optimal
        val lightSleepMinutes = (sleepDurationMinutes * Random.nextDouble(0.55, 0.65)).toInt()
        val deepSleepMinutes = (sleepDurationMinutes * Random.nextDouble(0.12, 0.18)).toInt()
        val remSleepMinutes = sleepDurationMinutes - lightSleepMinutes - deepSleepMinutes
        
        val sleepEfficiency = ((sleepDurationMinutes.toDouble() / totalDurationMinutes.toDouble()) * 100)
        
        // Fair quality physiological indicators
        val avgHeartRate = Random.nextInt(58, 72)
        val minHeartRate = avgHeartRate - Random.nextInt(8, 15)
        val maxHeartRate = avgHeartRate + Random.nextInt(15, 25)
        
        // Horarios de sueño para hoy
        val startCalendar = calendar.clone() as Calendar
        startCalendar.add(Calendar.DAY_OF_YEAR, -1)
        startCalendar.set(Calendar.HOUR_OF_DAY, 22)
        startCalendar.set(Calendar.MINUTE, Random.nextInt(30, 59))
        
        val endCalendar = startCalendar.clone() as Calendar
        endCalendar.add(Calendar.MINUTE, totalDurationMinutes)
        
        val sleepPhaseData = generateQualityBasedSleepPhaseData(
            startCalendar, endCalendar, dateTimeFormat, "FAIR"
        )
        
        return SleepDataUpload(
            uidUser = userId,
            deviceId = "android_mobile_${System.currentTimeMillis()}",
            date = date,
            startTime = dateTimeFormat.format(startCalendar.time),
            endTime = dateTimeFormat.format(endCalendar.time),
            timezone = "America/Mexico_City",
            totalDuration = totalDurationMinutes,
            sleepDuration = sleepDurationMinutes,
            lightSleepMinutes = lightSleepMinutes,
            deepSleepMinutes = deepSleepMinutes,
            remSleepMinutes = remSleepMinutes,
            awakeDuration = awakeDurationMinutes,
            sleepEfficiency = sleepEfficiency,
            awakeningsCount = Random.nextInt(4, 9), // Moderate awakenings
            quality = "FAIR", // Fixed FAIR quality
            avgHeartRate = avgHeartRate,
            minHeartRate = minHeartRate,
            maxHeartRate = maxHeartRate,
            avgMovement = Random.nextInt(20, 40), // Moderate movement
            avgRmssd = Random.nextDouble(30.0, 50.0), // Moderate HRV
            avgSdnn = Random.nextDouble(35.0, 55.0), // Moderate HRV
            sleepPhaseData = sleepPhaseData,
            createdAt = System.currentTimeMillis(),
            dataVersion = "1.0"
        )
    }

    /**
     * Generate sleep data with GOOD quality (sleepDataUserSchema.quality = "GOOD")
     * Usa la fecha de hoy para consistencia con otros datos
     */
    private fun generateGoodQualitySleepData(userId: String): SleepDataUpload {
        val calendar = Calendar.getInstance() // Fecha de hoy
        
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        dateTimeFormat.timeZone = TimeZone.getTimeZone("UTC")
        
        val date = dateFormat.format(calendar.time)
        
        // Good sleep: good duration and efficiency
        val totalDurationMinutes = Random.nextInt(450, 540) // 7.5-9 hours
        val sleepDurationMinutes = (totalDurationMinutes * Random.nextDouble(0.75, 0.90)).toInt()
        val awakeDurationMinutes = totalDurationMinutes - sleepDurationMinutes
        
        // Good sleep distribution - well balanced phases
        val lightSleepMinutes = (sleepDurationMinutes * Random.nextDouble(0.45, 0.55)).toInt()
        val deepSleepMinutes = (sleepDurationMinutes * Random.nextDouble(0.18, 0.25)).toInt()
        val remSleepMinutes = sleepDurationMinutes - lightSleepMinutes - deepSleepMinutes
        
        val sleepEfficiency = ((sleepDurationMinutes.toDouble() / totalDurationMinutes.toDouble()) * 100)
        
        // Good quality physiological indicators
        val avgHeartRate = Random.nextInt(52, 65)
        val minHeartRate = avgHeartRate - Random.nextInt(10, 18)
        val maxHeartRate = avgHeartRate + Random.nextInt(12, 20)
        
        // Horarios de sueño para hoy
        val startCalendar = calendar.clone() as Calendar
        startCalendar.add(Calendar.DAY_OF_YEAR, -1)
        startCalendar.set(Calendar.HOUR_OF_DAY, 22)
        startCalendar.set(Calendar.MINUTE, Random.nextInt(0, 30))
        
        val endCalendar = startCalendar.clone() as Calendar
        endCalendar.add(Calendar.MINUTE, totalDurationMinutes)
        
        val sleepPhaseData = generateQualityBasedSleepPhaseData(
            startCalendar, endCalendar, dateTimeFormat, "GOOD"
        )
        
        return SleepDataUpload(
            uidUser = userId,
            deviceId = "android_mobile_${System.currentTimeMillis()}",
            date = date,
            startTime = dateTimeFormat.format(startCalendar.time),
            endTime = dateTimeFormat.format(endCalendar.time),
            timezone = "America/Mexico_City",
            totalDuration = totalDurationMinutes,
            sleepDuration = sleepDurationMinutes,
            lightSleepMinutes = lightSleepMinutes,
            deepSleepMinutes = deepSleepMinutes,
            remSleepMinutes = remSleepMinutes,
            awakeDuration = awakeDurationMinutes,
            sleepEfficiency = sleepEfficiency,
            awakeningsCount = Random.nextInt(2, 6), // Few awakenings
            quality = "GOOD", // Fixed GOOD quality
            avgHeartRate = avgHeartRate,
            minHeartRate = minHeartRate,
            maxHeartRate = maxHeartRate,
            avgMovement = Random.nextInt(10, 25), // Low movement
            avgRmssd = Random.nextDouble(45.0, 70.0), // Good HRV
            avgSdnn = Random.nextDouble(50.0, 75.0), // Good HRV
            sleepPhaseData = sleepPhaseData,
            createdAt = System.currentTimeMillis(),
            dataVersion = "1.0"
        )
    }

    /**
     * Generate sleep data with EXCELLENT quality (sleepDataUserSchema.quality = "EXCELLENT")
     * Usa la fecha de hoy para consistencia con otros datos
     */
    private fun generateExcellentQualitySleepData(userId: String): SleepDataUpload {
        val calendar = Calendar.getInstance() // Fecha de hoy
        
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        dateTimeFormat.timeZone = TimeZone.getTimeZone("UTC")
        
        val date = dateFormat.format(calendar.time)
        
        // Excellent sleep: optimal duration and efficiency
        val totalDurationMinutes = Random.nextInt(480, 540) // 8-9 hours
        val sleepDurationMinutes = (totalDurationMinutes * Random.nextDouble(0.90, 0.98)).toInt()
        val awakeDurationMinutes = totalDurationMinutes - sleepDurationMinutes
        
        // Excellent sleep distribution - optimal phases
        val lightSleepMinutes = (sleepDurationMinutes * Random.nextDouble(0.40, 0.50)).toInt()
        val deepSleepMinutes = (sleepDurationMinutes * Random.nextDouble(0.20, 0.30)).toInt()
        val remSleepMinutes = sleepDurationMinutes - lightSleepMinutes - deepSleepMinutes
        
        val sleepEfficiency = ((sleepDurationMinutes.toDouble() / totalDurationMinutes.toDouble()) * 100)
        
        // Excellent quality physiological indicators
        val avgHeartRate = Random.nextInt(48, 60)
        val minHeartRate = avgHeartRate - Random.nextInt(12, 20)
        val maxHeartRate = avgHeartRate + Random.nextInt(10, 18)
        
        // Horarios de sueño para hoy
        val startCalendar = calendar.clone() as Calendar
        startCalendar.add(Calendar.DAY_OF_YEAR, -1)
        startCalendar.set(Calendar.HOUR_OF_DAY, 22)
        startCalendar.set(Calendar.MINUTE, Random.nextInt(0, 15))
        
        val endCalendar = startCalendar.clone() as Calendar
        endCalendar.add(Calendar.MINUTE, totalDurationMinutes)
        
        val sleepPhaseData = generateQualityBasedSleepPhaseData(
            startCalendar, endCalendar, dateTimeFormat, "EXCELLENT"
        )
        
        return SleepDataUpload(
            uidUser = userId,
            deviceId = "android_mobile_${System.currentTimeMillis()}",
            date = date,
            startTime = dateTimeFormat.format(startCalendar.time),
            endTime = dateTimeFormat.format(endCalendar.time),
            timezone = "America/Mexico_City",
            totalDuration = totalDurationMinutes,
            sleepDuration = sleepDurationMinutes,
            lightSleepMinutes = lightSleepMinutes,
            deepSleepMinutes = deepSleepMinutes,
            remSleepMinutes = remSleepMinutes,
            awakeDuration = awakeDurationMinutes,
            sleepEfficiency = sleepEfficiency,
            awakeningsCount = Random.nextInt(0, 4), // Minimal awakenings
            quality = "EXCELLENT", // Fixed EXCELLENT quality
            avgHeartRate = avgHeartRate,
            minHeartRate = minHeartRate,
            maxHeartRate = maxHeartRate,
            avgMovement = Random.nextInt(5, 15), // Very low movement
            avgRmssd = Random.nextDouble(60.0, 90.0), // Excellent HRV
            avgSdnn = Random.nextDouble(70.0, 100.0), // Excellent HRV
            sleepPhaseData = sleepPhaseData,
            createdAt = System.currentTimeMillis(),
            dataVersion = "1.0"
        )
    }
    
    /**
     * Genera datos de fases de sueño realistas
     */
    private fun generateSleepPhaseData(
        startCalendar: Calendar,
        endCalendar: Calendar,
        dateTimeFormat: SimpleDateFormat
    ): List<SleepPhaseData> {
        val phaseData = mutableListOf<SleepPhaseData>()
        var id = 1
        val currentTime = startCalendar.clone() as Calendar
        
        // Patrón típico de sueño: AWAKE -> LIGHT -> DEEP -> REM (repetir)
        val phases = arrayOf("AWAKE", "LIGHT", "DEEP", "REM")
        var phaseIndex = 0
        
        while (currentTime.before(endCalendar)) {
            val phase = phases[phaseIndex % phases.size]
            
            // Generar datos fisiológicos según la fase
            val (hrBpm, hrvRmssd, hrvSdnn) = when (phase) {
                "AWAKE" -> Triple(
                    Random.nextInt(70, 78),
                    Random.nextDouble(30.0, 35.0),
                    Random.nextDouble(36.0, 40.0)
                )
                "LIGHT" -> Triple(
                    Random.nextInt(60, 68),
                    Random.nextDouble(38.0, 45.0),
                    Random.nextDouble(44.0, 50.0)
                )
                "DEEP" -> Triple(
                    Random.nextInt(48, 55),
                    Random.nextDouble(55.0, 60.0),
                    Random.nextDouble(63.0, 67.0)
                )
                "REM" -> Triple(
                    Random.nextInt(65, 72),
                    Random.nextDouble(40.0, 45.0),
                    Random.nextDouble(47.0, 52.0)
                )
                else -> Triple(65, 40.0, 50.0)
            }
            
            phaseData.add(
                SleepPhaseData(
                    id = id++,
                    phase = phase,
                    datetime = dateTimeFormat.format(currentTime.time),
                    hrBpm = hrBpm,
                    hrvRmssd = hrvRmssd,
                    hrvSdnn = hrvSdnn
                )
            )
            
            // Avanzar tiempo (15-30 minutos por medición)
            currentTime.add(Calendar.MINUTE, Random.nextInt(15, 30))
            phaseIndex++
        }
        
        return phaseData
    }
    
    /**
     * Genera datos de fases de sueño basados en la calidad especificada
     */
    private fun generateQualityBasedSleepPhaseData(
        startCalendar: Calendar,
        endCalendar: Calendar,
        dateTimeFormat: SimpleDateFormat,
        quality: String
    ): List<SleepPhaseData> {
        val phaseData = mutableListOf<SleepPhaseData>()
        var id = 1
        val currentTime = startCalendar.clone() as Calendar
        
        // Patrones de sueño según la calidad
        val (phases, awakeFreq, phaseVariation) = when (quality) {
            "POOR" -> Triple(
                arrayOf("AWAKE", "LIGHT", "AWAKE", "LIGHT", "DEEP", "AWAKE", "LIGHT", "REM", "AWAKE"),
                0.3, // 30% probabilidad de estar despierto
                10 // Alta variación en duración
            )
            "FAIR" -> Triple(
                arrayOf("AWAKE", "LIGHT", "LIGHT", "DEEP", "LIGHT", "REM", "LIGHT", "DEEP"),
                0.15, // 15% probabilidad de estar despierto
                7 // Variación moderada
            )
            "GOOD" -> Triple(
                arrayOf("LIGHT", "LIGHT", "DEEP", "DEEP", "REM", "LIGHT", "DEEP", "REM"),
                0.08, // 8% probabilidad de estar despierto
                5 // Poca variación
            )
            "EXCELLENT" -> Triple(
                arrayOf("LIGHT", "DEEP", "DEEP", "REM", "DEEP", "REM", "LIGHT", "REM"),
                0.03, // 3% probabilidad de estar despierto
                3 // Muy poca variación
            )
            else -> Triple(
                arrayOf("AWAKE", "LIGHT", "DEEP", "REM"),
                0.1,
                5
            )
        }
        
        var phaseIndex = 0
        
        while (currentTime.before(endCalendar)) {
            // Determinar la fase actual
            val basePhase = phases[phaseIndex % phases.size]
            val actualPhase = if (Random.nextDouble() < awakeFreq) "AWAKE" else basePhase
            
            // Generar datos fisiológicos según la fase y calidad
            val (hrBpm, hrvRmssd, hrvSdnn) = generatePhysiologicalData(actualPhase, quality)
            
            phaseData.add(
                SleepPhaseData(
                    id = id++,
                    phase = actualPhase,
                    datetime = dateTimeFormat.format(currentTime.time),
                    hrBpm = hrBpm,
                    hrvRmssd = hrvRmssd,
                    hrvSdnn = hrvSdnn
                )
            )
            
            // Avanzar tiempo con variación según la calidad
            val baseMinutes = 20
            val variation = Random.nextInt(-phaseVariation, phaseVariation + 1)
            currentTime.add(Calendar.MINUTE, baseMinutes + variation)
            phaseIndex++
        }
        
        return phaseData
    }
    
    /**
     * Genera datos fisiológicos según la fase y calidad del sueño
     */
    private fun generatePhysiologicalData(phase: String, quality: String): Triple<Int, Double, Double> {
        return when (phase) {
            "AWAKE" -> when (quality) {
                "POOR" -> Triple(
                    Random.nextInt(75, 85),
                    Random.nextDouble(20.0, 30.0),
                    Random.nextDouble(25.0, 35.0)
                )
                "FAIR" -> Triple(
                    Random.nextInt(70, 80),
                    Random.nextDouble(25.0, 35.0),
                    Random.nextDouble(30.0, 40.0)
                )
                "GOOD" -> Triple(
                    Random.nextInt(68, 75),
                    Random.nextDouble(30.0, 40.0),
                    Random.nextDouble(35.0, 45.0)
                )
                "EXCELLENT" -> Triple(
                    Random.nextInt(65, 72),
                    Random.nextDouble(35.0, 45.0),
                    Random.nextDouble(40.0, 50.0)
                )
                else -> Triple(72, 32.0, 38.0)
            }
            "LIGHT" -> when (quality) {
                "POOR" -> Triple(
                    Random.nextInt(65, 75),
                    Random.nextDouble(25.0, 35.0),
                    Random.nextDouble(30.0, 40.0)
                )
                "FAIR" -> Triple(
                    Random.nextInt(60, 70),
                    Random.nextDouble(30.0, 40.0),
                    Random.nextDouble(35.0, 45.0)
                )
                "GOOD" -> Triple(
                    Random.nextInt(58, 68),
                    Random.nextDouble(35.0, 45.0),
                    Random.nextDouble(40.0, 50.0)
                )
                "EXCELLENT" -> Triple(
                    Random.nextInt(55, 65),
                    Random.nextDouble(40.0, 50.0),
                    Random.nextDouble(45.0, 55.0)
                )
                else -> Triple(62, 38.0, 44.0)
            }
            "DEEP" -> when (quality) {
                "POOR" -> Triple(
                    Random.nextInt(55, 65),
                    Random.nextDouble(35.0, 45.0),
                    Random.nextDouble(40.0, 50.0)
                )
                "FAIR" -> Triple(
                    Random.nextInt(50, 60),
                    Random.nextDouble(40.0, 50.0),
                    Random.nextDouble(45.0, 55.0)
                )
                "GOOD" -> Triple(
                    Random.nextInt(48, 58),
                    Random.nextDouble(45.0, 55.0),
                    Random.nextDouble(50.0, 60.0)
                )
                "EXCELLENT" -> Triple(
                    Random.nextInt(45, 55),
                    Random.nextDouble(50.0, 65.0),
                    Random.nextDouble(60.0, 75.0)
                )
                else -> Triple(52, 48.0, 55.0)
            }
            "REM" -> when (quality) {
                "POOR" -> Triple(
                    Random.nextInt(68, 78),
                    Random.nextDouble(30.0, 40.0),
                    Random.nextDouble(35.0, 45.0)
                )
                "FAIR" -> Triple(
                    Random.nextInt(65, 75),
                    Random.nextDouble(35.0, 45.0),
                    Random.nextDouble(40.0, 50.0)
                )
                "GOOD" -> Triple(
                    Random.nextInt(62, 72),
                    Random.nextDouble(40.0, 50.0),
                    Random.nextDouble(45.0, 55.0)
                )
                "EXCELLENT" -> Triple(
                    Random.nextInt(60, 70),
                    Random.nextDouble(45.0, 55.0),
                    Random.nextDouble(50.0, 60.0)
                )
                else -> Triple(68, 42.0, 48.0)
            }
            else -> Triple(65, 40.0, 50.0)
        }
    }
}

enum class SleepDataType(val displayName: String) {
    TODAY("Datos de Hoy"),
    YESTERDAY("Datos de Ayer"),
    RANDOM_PAST("Datos Aleatorios"),
    POOR_QUALITY("Calidad POBRE"),
    FAIR_QUALITY("Calidad REGULAR"),
    GOOD_QUALITY("Calidad BUENA"),
    EXCELLENT_QUALITY("Calidad EXCELENTE")
}
