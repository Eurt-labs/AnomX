package com.example.anomx

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class NationalDirectoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_national_directory)

        val toolbar = findViewById<Toolbar>(R.id.toolbarNationalDir)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        setupDialer(R.id.dial_112, "112")
        setupDialer(R.id.dial_100, "100")
        setupDialer(R.id.dial_101, "101")
        setupDialer(R.id.dial_102, "102")
        setupDialer(R.id.dial_108, "108")
        
        setupDialer(R.id.dial_181, "181")
        setupDialer(R.id.dial_1091, "1091")
        setupDialer(R.id.dial_1098, "1098")
        
        setupDialer(R.id.dial_1930, "1930")
        setupDialer(R.id.dial_1906, "1906")
        setupDialer(R.id.dial_1066, "1066")
        setupDialer(R.id.dial_139, "139")
        setupDialer(R.id.dial_14567, "14567")
        setupDialer(R.id.dial_1097, "1097")
        setupDialer(R.id.dial_1551, "1551")
        
        setupDialer(R.id.dial_1078, "1078")
        setupDialer(R.id.dial_1070, "1070")
        setupDialer(R.id.dial_ndrf, "01124363260")
    }

    private fun setupDialer(buttonId: Int, number: String) {
        val btn = findViewById<Button>(buttonId)
        btn?.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number"))
            startActivity(intent)
        }
    }
}
