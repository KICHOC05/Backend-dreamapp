package com.example.appmobile.domain.model

import com.google.gson.annotations.SerializedName

data class SleepDataUpload(
    @SerializedName("uidUser") val uidUser: String,
    @SerializedName("deviceId") val deviceId: String,
    @SerializedName("date") val date: String,
    @SerializedName("startTime") val startTime: String,
    @SerializedName("endTime") val endTime: String,
    @SerializedName("timezone") val timezone: String,
    @SerializedName("totalDuration") val totalDuration: Int,
    @SerializedName("sleepDuration") val sleepDuration: Int,
    @SerializedName("lightSleepMinutes") val lightSleepMinutes: Int,
    @SerializedName("deepSleepMinutes") val deepSleepMinutes: Int,
    @SerializedName("remSleepMinutes") val remSleepMinutes: Int,
    @SerializedName("awakeDuration") val awakeDuration: Int,
    @SerializedName("sleepEfficiency") val sleepEfficiency: Double,
    @SerializedName("awakeningsCount") val awakeningsCount: Int,
    @SerializedName("quality") val quality: String,
    @SerializedName("avgHeartRate") val avgHeartRate: Int,
    @SerializedName("minHeartRate") val minHeartRate: Int,
    @SerializedName("maxHeartRate") val maxHeartRate: Int,
    @SerializedName("avgMovement") val avgMovement: Int,
    @SerializedName("avgRmssd") val avgRmssd: Double,
    @SerializedName("avgSdnn") val avgSdnn: Double,
    @SerializedName("sleepPhaseData") val sleepPhaseData: List<SleepPhaseData>,
    @SerializedName("createdAt") val createdAt: Long,
    @SerializedName("dataVersion") val dataVersion: String
)

data class SleepPhaseData(
    @SerializedName("id") val id: Int,
    @SerializedName("phase") val phase: String,
    @SerializedName("datetime") val datetime: String,
    @SerializedName("hr_bpm") val hrBpm: Int,
    @SerializedName("hrv_rmssd") val hrvRmssd: Double,
    @SerializedName("hrv_sdnn") val hrvSdnn: Double
)

data class SleepUploadResponse(
    val success: Boolean,
    val message: String,
    val data: SleepUploadData?
)

data class SleepUploadData(
    val documentId: String,
    val date: String,
    val startTime: String,
    val userId: String,
    val totalMeasurements: Int,
    val sleepDuration: Int,
    val sleepEfficiency: Double,
    val quality: String
)

data class SleepUploadError(
    val error: String,
    val message: String,
    val date: String? = null,
    val startTime: String? = null,
    val details: List<ValidationError>? = null
)

data class ValidationError(
    val field: String,
    val message: String,
    val code: String
)
