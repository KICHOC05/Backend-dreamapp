package com.example.appmobile.presentation.reciver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log


class DebugBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.i("DebugReceiver", "Recibido broadcast: ${intent?.action}, extras: ${intent?.extras}")
    }
}
