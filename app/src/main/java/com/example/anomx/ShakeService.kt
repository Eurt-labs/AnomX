package com.example.anomx

import android.app.*
import android.content.Context
import android.content.Intent
import android.hardware.*
import android.os.*
import android.telephony.SmsManager
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.LocationServices
import kotlin.math.sqrt

class ShakeService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var lastShakeTime = 0L
    private var shakeCount = 0

    override fun onCreate() {
        super.onCreate()

        val channelId = "shake_channel"

        val channel = NotificationChannel(
            channelId,
            "Shake Service",
            NotificationManager.IMPORTANCE_LOW
        )

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("🚨 AnomX Active")
            .setContentText("Double-shake to trigger SOS")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .build()

        startForeground(1, notification)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        val x = event!!.values[0]
        val y = event.values[1]
        val z = event.values[2]

        val acceleration = sqrt(x*x + y*y + z*z)

        if (acceleration > 25) { // Threshold for a very serious shake
            val currentTime = System.currentTimeMillis()
            
            if (currentTime - lastShakeTime > 500) { // Minimum 500ms between discrete shakes
                
                // If it's been more than 3 seconds since the last shake, reset the count
                if (currentTime - lastShakeTime > 3000) {
                    shakeCount = 0
                }
                
                lastShakeTime = currentTime
                shakeCount++

                if (shakeCount >= 2) {
                    shakeCount = 0 // Reset
                    triggerAlert()
                }
            }
        }
    }

    private fun triggerAlert() {

        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        val useWhatsApp = prefs.getBoolean("use_whatsapp", false)

        val shared = getSharedPreferences("contacts", MODE_PRIVATE)
        val phoneList = shared.getStringSet("numbers", setOf())?.toList() ?: return

        val fused = LocationServices.getFusedLocationProviderClient(this)

        fused.getCurrentLocation(
            com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY,
            null
        ).addOnSuccessListener { location ->

            val message = if (location != null) {
                "🚨 EMERGENCY!\nhttps://maps.google.com/?q=${location.latitude},${location.longitude}"
            } else {
                "🚨 EMERGENCY! Location not available"
            }

            if (useWhatsApp) {
                sendWhatsApp(phoneList, message)
            } else {
                sendSMS(phoneList, message)
            }

            vibratePhone()
        }
    }

    private fun sendSMS(phoneList: List<String>, message: String) {
        try {
            val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                getSystemService(SmsManager::class.java)
            } else {
                SmsManager.getDefault()
            }
            
            for (phone in phoneList) {
                smsManager.sendTextMessage(phone, null, message, null, null)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun sendWhatsApp(phoneList: List<String>, message: String) {
        val phone = phoneList.firstOrNull() ?: return

        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = android.net.Uri.parse(
            "https://wa.me/$phone?text=${java.net.URLEncoder.encode(message, "UTF-8")}"
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    private fun vibratePhone() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        } else {
            vibrator.vibrate(1000)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onDestroy() {
        sensorManager.unregisterListener(this)
        super.onDestroy()
    }
}