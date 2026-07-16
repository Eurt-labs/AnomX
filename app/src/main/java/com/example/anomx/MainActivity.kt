package com.example.anomx

import android.app.ActivityManager
import android.content.Context
import android.Manifest
import android.content.Intent
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.AudioManager
import android.media.ToneGenerator
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.telephony.SmsManager
import android.text.util.Linkify
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SwitchCompat
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.button.MaterialButtonToggleGroup
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices

class MainActivity : AppCompatActivity() {

    private val phoneList = mutableListOf<String>()
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val input = findViewById<EditText>(R.id.phoneInput)
        val addBtn = findViewById<ImageButton>(R.id.addButton)
        val listView = findViewById<ListView>(R.id.listView)
        val startBtn = findViewById<Button>(R.id.startServiceBtn)
        val stopBtn = findViewById<Button>(R.id.stopServiceBtn)
        val manualSendBtn = findViewById<Button>(R.id.manualSendBtn)
        val modeToggleGroup = findViewById<MaterialButtonToggleGroup>(R.id.modeToggleGroup)
        val contactsCard = findViewById<LinearLayout>(R.id.contactsCard)
        val btnMoreDetails = findViewById<Button>(R.id.btnMoreDetails)

        btnMoreDetails.setOnClickListener {
            startActivity(Intent(this, NationalDirectoryActivity::class.java))
        }



        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, phoneList)
        listView.adapter = adapter

        val prefs = getSharedPreferences("settings", MODE_PRIVATE)

        addBtn.setOnClickListener {
            val useWa = prefs.getBoolean("use_whatsapp", false)
            if (useWa && phoneList.isNotEmpty()) {
                Toast.makeText(this, "WhatsApp mode only allows one contact.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val number = input.text.toString()
            if (number.isNotEmpty()) {
                phoneList.add(number)
                adapter.notifyDataSetChanged()
                saveNumbers()
                input.text.clear()
            }
        }

        listView.setOnItemLongClickListener { _, _, position, _ ->
            phoneList.removeAt(position)
            adapter.notifyDataSetChanged()
            saveNumbers()
            true
        }

        val useWhatsApp = prefs.getBoolean("use_whatsapp", false)
        if (useWhatsApp) {
            modeToggleGroup.check(R.id.btnModeWhatsApp)
            contactsCard.background.setTint(Color.parseColor("#102517"))
        } else {
            modeToggleGroup.check(R.id.btnModeSms)
            contactsCard.background.setTint(Color.parseColor("#211F26"))
        }

        modeToggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                val isWhatsApp = checkedId == R.id.btnModeWhatsApp
                prefs.edit().putBoolean("use_whatsapp", isWhatsApp).apply()

                if (isWhatsApp) {
                    animateToWhatsAppMode(contactsCard)
                    if (phoneList.size > 1) {
                        val first = phoneList.first()
                        phoneList.clear()
                        phoneList.add(first)
                        adapter.notifyDataSetChanged()
                        saveNumbers()
                        Toast.makeText(this, "WhatsApp mode limited to 1 contact. List trimmed.", Toast.LENGTH_LONG).show()
                    }
                } else {
                    animateToSmsMode(contactsCard)
                }
            }
        }
        
        loadNumbers()

        startBtn.setOnClickListener {
            val intent = Intent(this, ShakeService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
            Toast.makeText(this, "Service Started", Toast.LENGTH_SHORT).show()
            updateServiceUI()
        }

        stopBtn.setOnClickListener {
            stopService(Intent(this, ShakeService::class.java))
            Toast.makeText(this, "Service Stopped", Toast.LENGTH_SHORT).show()
            updateServiceUI()
        }

        manualSendBtn.setOnClickListener {
            sendEmergencyAlert()
        }

        requestPermissions()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_about) {
            val dialogView = layoutInflater.inflate(R.layout.dialog_about, null)
            val gitLink = dialogView.findViewById<TextView>(R.id.aboutGitLink)
            val bugLink = dialogView.findViewById<TextView>(R.id.aboutBugReportLink)
            Linkify.addLinks(gitLink, Linkify.WEB_URLS)
            Linkify.addLinks(bugLink, Linkify.WEB_URLS)

            val dialog = AlertDialog.Builder(this)
                .setView(dialogView)
                .create()
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.show()
            return true
        } else if (item.itemId == R.id.action_how_to_use) {
            startActivity(Intent(this, HowToUseActivity::class.java))
            return true
        } else if (item.itemId == R.id.action_emergency_numbers) {
            val dialogView = layoutInflater.inflate(R.layout.dialog_emergency_numbers, null)
            
            fun setDialer(btnId: Int, number: String) {
                dialogView.findViewById<Button>(btnId).setOnClickListener {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number"))
                    startActivity(intent)
                }
            }
            
            setDialer(R.id.btnDial112, "112")
            setDialer(R.id.btnDial100, "100")
            setDialer(R.id.btnDial108, "108")
            setDialer(R.id.btnDial101, "101")
            setDialer(R.id.btnDial1091, "1091")
            setDialer(R.id.btnDial1033, "1033")

            val dialog = AlertDialog.Builder(this)
                .setView(dialogView)
                .create()
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.show()
            return true
        } else if (item.itemId == R.id.action_advanced_features) {
            startActivity(Intent(this, AdvancedDashboardActivity::class.java))
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun saveNumbers() {
        val shared = getSharedPreferences("contacts", MODE_PRIVATE)
        shared.edit().putStringSet("numbers", phoneList.toSet()).apply()
    }

    private fun loadNumbers() {
        val shared = getSharedPreferences("contacts", MODE_PRIVATE)
        val saved = shared.getStringSet("numbers", setOf())
        phoneList.addAll(saved!!)
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.SEND_SMS,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            1
        )
    }

    private fun sendEmergencyAlert() {
        if (phoneList.isEmpty()) {
            Toast.makeText(this, "Please add at least one phone number.", Toast.LENGTH_SHORT).show()
            return
        }

        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        val useWhatsApp = prefs.getBoolean("use_whatsapp", false)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Location permission missing", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(this, "Sending SOS...", Toast.LENGTH_SHORT).show()
        
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
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to get location, sending without it.", Toast.LENGTH_SHORT).show()
            val message = "🚨 EMERGENCY! Location not available"
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
            Toast.makeText(this, "SMS Sent", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to send SMS", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendWhatsApp(phoneList: List<String>, message: String) {
        val phone = phoneList.firstOrNull() ?: return
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = android.net.Uri.parse(
                "https://wa.me/$phone?text=${java.net.URLEncoder.encode(message, "UTF-8")}"
            )
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to open WhatsApp", Toast.LENGTH_SHORT).show()
        }
    }

    private fun vibratePhone() {
        val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        } else {
            vibrator.vibrate(1000)
        }
    }

    override fun onResume() {
        super.onResume()
        updateServiceUI()
        updateMapLocation()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun updateMapLocation() {
        val fused = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fused.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val coordText = findViewById<TextView>(R.id.currentCoordinatesText)
                    coordText?.text = "Lat: ${location.latitude} | Lon: ${location.longitude}"
                    coordText?.setTextColor(android.graphics.Color.parseColor("#4CAF50"))

                    val webView = findViewById<WebView>(R.id.mapWebView)
                    if (webView != null) {
                        webView.settings.javaScriptEnabled = true
                        webView.webViewClient = WebViewClient()
                        val html = "<body style='margin:0;padding:0;'><iframe width=\"100%\" height=\"100%\" frameborder=\"0\" scrolling=\"no\" marginheight=\"0\" marginwidth=\"0\" src=\"https://maps.google.com/maps?q=${location.latitude},${location.longitude}&amp;z=16&amp;output=embed\"></iframe></body>"
                        webView.loadDataWithBaseURL(null, html, "text/html", "utf-8", null)
                    }
                }
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun isServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    private fun updateServiceUI() {
        val startBtn = findViewById<Button>(R.id.startServiceBtn)
        val stopBtn = findViewById<Button>(R.id.stopServiceBtn)
        val statusText = findViewById<TextView>(R.id.serviceStatusText)

        if (isServiceRunning(ShakeService::class.java)) {
            statusText.text = "Status: \uD83D\uDFE2 Active"
            statusText.setTextColor(android.graphics.Color.parseColor("#4CAF50"))
            startBtn.isEnabled = false
            startBtn.alpha = 0.5f
            stopBtn.isEnabled = true
            stopBtn.alpha = 1.0f
        } else {
            statusText.text = "Status: \uD83D\uDD34 Inactive"
            statusText.setTextColor(android.graphics.Color.parseColor("#F44336"))
            startBtn.isEnabled = true
            startBtn.alpha = 1.0f
            stopBtn.isEnabled = false
            stopBtn.alpha = 0.5f
        }
    }

    private fun animateToWhatsAppMode(view: View) {
        val colorFrom = Color.parseColor("#211F26")
        val colorTo = Color.parseColor("#0e2e1a") // Deep green tint for WhatsApp
        val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo)
        colorAnimation.duration = 400 // milliseconds
        colorAnimation.addUpdateListener { animator ->
            view.background.setTint(animator.animatedValue as Int)
        }
        colorAnimation.start()
    }

    private fun animateToSmsMode(view: View) {
        val colorFrom = Color.parseColor("#0e2e1a")
        val colorTo = Color.parseColor("#211F26") // Standard dark material surface
        val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo)
        colorAnimation.duration = 400 // milliseconds
        colorAnimation.addUpdateListener { animator ->
            view.background.setTint(animator.animatedValue as Int)
        }
        colorAnimation.start()
    }
}