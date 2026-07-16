package com.example.anomx

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import android.telephony.SmsManager
import android.os.Build
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices

class BatteryReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BATTERY_LOW) {
            val prefs = context.getSharedPreferences("adv_settings", Context.MODE_PRIVATE)
            val isBatteryPingEnabled = prefs.getBoolean("opt_battery", false)
            
            if (isBatteryPingEnabled) {
                sendLastGaspPing(context)
            }
        }
    }

    private fun sendLastGaspPing(context: Context) {
        val shared = context.getSharedPreferences("contacts", Context.MODE_PRIVATE)
        val phoneList = shared.getStringSet("numbers", setOf())?.toList() ?: return
        
        if (phoneList.isEmpty()) return

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        val fused = LocationServices.getFusedLocationProviderClient(context)
        fused.getCurrentLocation(
            com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
            null
        ).addOnSuccessListener { location ->
            val message = if (location != null) {
                "🚨 BATTERY CRITICAL! Phone is about to die. Last known location:\nhttps://maps.google.com/?q=${location.latitude},${location.longitude}"
            } else {
                "🚨 BATTERY CRITICAL! Phone is about to die. Location unavailable."
            }

            try {
                val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    context.getSystemService(SmsManager::class.java)
                } else {
                    SmsManager.getDefault()
                }
                
                for (phone in phoneList) {
                    smsManager.sendTextMessage(phone, null, message, null, null)
                }
                Toast.makeText(context, "Last-Gasp Ping Sent", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
