package com.example.anomx

import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.card.MaterialCardView

class AdvancedDashboardActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_advanced_dashboard)

        val toolbar = findViewById<Toolbar>(R.id.toolbarAdvanced)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        prefs = getSharedPreferences("adv_settings", MODE_PRIVATE)

        val cardBattery = findViewById<MaterialCardView>(R.id.cardBattery)
        val txtBatteryStatus = findViewById<TextView>(R.id.txtBatteryStatus)

        val cardBreadcrumbs = findViewById<MaterialCardView>(R.id.cardBreadcrumbs)
        val txtBreadcrumbsStatus = findViewById<TextView>(R.id.txtBreadcrumbsStatus)

        setupCardToggle(cardBattery, txtBatteryStatus, "opt_battery")
        setupCardToggle(cardBreadcrumbs, txtBreadcrumbsStatus, "opt_breadcrumbs")
    }

    private fun setupCardToggle(card: MaterialCardView, statusLabel: TextView, prefKey: String) {
        val isActive = prefs.getBoolean(prefKey, false)
        updateCardVisuals(card, statusLabel, isActive)

        card.setOnClickListener {
            val currentState = prefs.getBoolean(prefKey, false)
            val newState = !currentState
            prefs.edit().putBoolean(prefKey, newState).apply()
            updateCardVisuals(card, statusLabel, newState)
        }
    }

    private fun updateCardVisuals(card: MaterialCardView, statusLabel: TextView, isActive: Boolean) {
        if (isActive) {
            // Bright, Highlighted layout for ENABLED modules
            card.setCardBackgroundColor(Color.parseColor("#381E72"))
            card.strokeColor = Color.parseColor("#D0BCFF")
            card.alpha = 1.0f

            statusLabel.text = "ENABLED"
            statusLabel.setTextColor(Color.parseColor("#FFFFFF"))
            statusLabel.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#2E7D32")) // Green Background
        } else {
            // Dimmed layout for INACTIVE modules
            card.setCardBackgroundColor(Color.parseColor("#1A181F"))
            card.strokeColor = Color.parseColor("#333333")
            card.alpha = 0.5f

            statusLabel.text = "DISABLED"
            statusLabel.setTextColor(Color.parseColor("#AAAAAA"))
            statusLabel.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#333333"))
        }
    }
}
