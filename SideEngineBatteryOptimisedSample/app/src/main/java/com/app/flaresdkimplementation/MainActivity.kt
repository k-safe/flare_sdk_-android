package com.app.flaresdkimplementation

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.AppCompatButton
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var btnStandard: AppCompatButton
    private lateinit var btnCustom: AppCompatButton
    private lateinit var btnMap: AppCompatButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        btnStandard = findViewById(R.id.btnStandard)
        btnCustom = findViewById(R.id.btnCustom)
        btnMap = findViewById(R.id.btnMap)

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

        btnMap.setOnClickListener {
            if (!isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ),
                    PERMISSIONS_REQUEST_LOCATION
                )
            } else {
                val intent = Intent(this, MapsActivity::class.java)
                startActivity(intent)
            }
        }
    }

    companion object {
        private const val PERMISSIONS_REQUEST_LOCATION = 0

        fun Context.isPermissionGranted(permission: String): Boolean {
            return ContextCompat.checkSelfPermission(
                this, permission
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
}
