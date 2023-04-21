package com.sdksideengine.kotlin

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.SwitchCompat
import com.app.flaresdkimplementation.EmergencySOSActivity
import com.app.flaresdkimplementation.databinding.ActivityMainBinding
import com.sos.busbysideengine.Constants

class MainActivity : AppCompatActivity() {
    private val viewBinding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)


        setListener()
    }

    private fun setListener() {

        viewBinding.btnStandard.setOnClickListener {
            var mode = Constants.ENVIRONMENT_PRODUCTION
            if(!viewBinding.swMode.isChecked){
                mode = Constants.ENVIRONMENT_SANDBOX
            }
            val intent = Intent(this, StandardThemeActivity::class.java)
            intent.putExtra("mode",mode)
            startActivity(intent)
        }

        viewBinding.btnCustom.setOnClickListener {
            var mode = Constants.ENVIRONMENT_PRODUCTION
            if(!viewBinding.swMode.isChecked){
                mode = Constants.ENVIRONMENT_SANDBOX
            }
            val intent = Intent(this, CustomThemeActivity::class.java)
            intent.putExtra("mode",mode)
            startActivity(intent)
        }
        viewBinding.btnCustom.setOnClickListener {
            var mode = Constants.ENVIRONMENT_PRODUCTION
            if(!viewBinding.swMode.isChecked){
                mode = Constants.ENVIRONMENT_SANDBOX
            }
            val intent = Intent(this, CustomThemeActivity::class.java)
            intent.putExtra("mode",mode)
            startActivity(intent)
        }
        viewBinding.btnSOS.setOnClickListener {
            var mode = Constants.ENVIRONMENT_PRODUCTION
            if(!viewBinding.swMode.isChecked){
                mode = Constants.ENVIRONMENT_SANDBOX
            }
            val intent = Intent(this, EmergencySOSActivity::class.java)
            intent.putExtra("mode",mode)
            startActivity(intent)
        }

        viewBinding.swMode.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // The switch is checked.
                viewBinding.swMode.text = "Production mode"
                viewBinding.btnSOS.visibility = View.VISIBLE
            } else {
                // The switch isn't checked.
                viewBinding.swMode.text = "Sandbox mode"
                viewBinding.btnSOS.visibility = View.GONE
            }
        }

    }
}
