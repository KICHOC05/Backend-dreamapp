package com.example.dashboardapp.data.remote.helpers

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

class NotifyUpdateHelper(private val notifyUpdateUrl: String) {
    suspend fun notifyUpdate() {
        withContext(Dispatchers.IO) {
            try {
                val url = URL(notifyUpdateUrl)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.connectTimeout = 3000
                conn.readTimeout = 3000
                conn.doOutput = true
                conn.connect()
                conn.inputStream.close()
                conn.disconnect()
            } catch (_: Exception) {
            }
        }
    }
}