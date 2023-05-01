package com.app.flaresdkimplementation

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class TestIncidentActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_incident)

        val ivCUIClose = findViewById<View>(R.id.ivCUIClose)
        ivCUIClose.setOnClickListener {
            finish()
        }
    }

    override fun onStop() {
        super.onStop()
        finish()
    }

    override fun onNavigateUp(): Boolean {
        finish()
        return true
    }
}