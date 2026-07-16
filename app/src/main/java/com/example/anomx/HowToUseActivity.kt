package com.example.anomx

import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.card.MaterialCardView

class HowToUseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_how_to_use)

        val toolbar = findViewById<Toolbar>(R.id.toolbarHowToUse)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        val textIntro = findViewById<TextView>(R.id.textIntro)
        val card1 = findViewById<MaterialCardView>(R.id.cardStep1)
        val card2 = findViewById<MaterialCardView>(R.id.cardStep2)
        val card3 = findViewById<MaterialCardView>(R.id.cardStep3)

        // Load flushed animations
        val anim1 = AnimationUtils.loadAnimation(this, R.anim.fade_slide_up)
        anim1.startOffset = 100
        val anim2 = AnimationUtils.loadAnimation(this, R.anim.fade_slide_up)
        anim2.startOffset = 300
        val anim3 = AnimationUtils.loadAnimation(this, R.anim.fade_slide_up)
        anim3.startOffset = 500
        val anim4 = AnimationUtils.loadAnimation(this, R.anim.fade_slide_up)
        anim4.startOffset = 700

        textIntro.startAnimation(anim1)
        card1.startAnimation(anim2)
        card2.startAnimation(anim3)
        card3.startAnimation(anim4)
    }
}
