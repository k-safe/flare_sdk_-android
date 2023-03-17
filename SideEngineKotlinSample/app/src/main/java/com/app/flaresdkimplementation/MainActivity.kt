package com.app.flaresdkimplementation

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.AppCompatButton

class MainActivity : AppCompatActivity() {

    private lateinit var btnStandard: AppCompatButton
    private lateinit var btnCustom: AppCompatButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        btnStandard = findViewById(R.id.btnStandard)
        btnCustom = findViewById(R.id.btnCustom)

        setListener()
    }

    private fun setListener() {

        btnStandard.setOnClickListener {
            val intent = Intent(this, StandardThemeActivity::class.java)
            startActivity(intent)
        }

        btnCustom.setOnClickListener {
            val intent = Intent(this, CustomThemeActivity::class.java)
            startActivity(intent)
        }
    }
}
