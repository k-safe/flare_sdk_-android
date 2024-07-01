package com.app.flaresdkimplementation

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.app.flaresdkimplementation.databinding.ActivityMainBinding
import com.sos.busbysideengine.Constants

class MainActivity : AppCompatActivity() {

    // Production Mode
    private var productionLicense = ""
    private var sandboxLicense = ""
    private val secretKey = ""

    private var mode = Constants.ENVIRONMENT_PRODUCTION
    private var postNotificationCode = 1221

    private val viewBinding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        setListener()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    postNotificationCode
                )
            }
        }
    }

    private fun setListener() {

        viewBinding.btnSOS.visibility = View.VISIBLE
        viewBinding.btnEnableFlareAware.visibility = View.VISIBLE
        viewBinding.btnStandard.setOnClickListener {
            mode = if(viewBinding.rgEnvironment.checkedRadioButtonId == R.id.rbSandBox){
                Constants.ENVIRONMENT_SANDBOX
            } else {
                Constants.ENVIRONMENT_PRODUCTION
            }
            val intent = Intent(this, StandardThemeActivity::class.java)
            intent.putExtra("mode", mode)
            intent.putExtra("secretKey", secretKey)
            intent.putExtra("lic",
                if(Constants.ENVIRONMENT_PRODUCTION.equals(mode)) productionLicense else sandboxLicense)
            startActivity(intent)
        }
        viewBinding.btnCustom.setOnClickListener {
            mode = if(viewBinding.rgEnvironment.checkedRadioButtonId == R.id.rbSandBox){
                Constants.ENVIRONMENT_SANDBOX
            } else {
                Constants.ENVIRONMENT_PRODUCTION
            }
            val intent = Intent(this, CustomThemeActivity::class.java)
            intent.putExtra("mode", mode)
            intent.putExtra("secretKey", secretKey)
            intent.putExtra("lic",
                if(Constants.ENVIRONMENT_PRODUCTION.equals(mode)) productionLicense else sandboxLicense)
            startActivity(intent)
        }
        viewBinding.btnSOS.setOnClickListener {
            val intent = Intent(this, EmergencySOSActivity::class.java)
            intent.putExtra("lic", productionLicense)
            intent.putExtra("secretKey", secretKey)
            startActivity(intent)
        }
        viewBinding.rgEnvironment.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.rbProduction) {
                // The switch is checked.
                viewBinding.rbProduction.text = getString(R.string.production_mode)
                viewBinding.btnSOS.visibility = View.VISIBLE
                viewBinding.btnEnableFlareAware.visibility = View.VISIBLE
            } else {
                // The switch isn't checked.
                viewBinding.rbSandBox.text = getString(R.string.sandbox_mode)
                viewBinding.btnSOS.visibility = View.VISIBLE
                viewBinding.btnEnableFlareAware.visibility = View.VISIBLE
            }
        }
        viewBinding.btnEnableFlareAware.setOnClickListener {
            mode = if(viewBinding.rgEnvironment.checkedRadioButtonId == R.id.rbSandBox){
                Constants.ENVIRONMENT_SANDBOX
            } else {
                Constants.ENVIRONMENT_PRODUCTION
            }
            val intent = Intent(this, EnableFlareAwareActivity::class.java)
            intent.putExtra("mode", mode)
            intent.putExtra("secretKey", secretKey)
            intent.putExtra("lic",
                if(Constants.ENVIRONMENT_PRODUCTION.equals(mode)) productionLicense else sandboxLicense)
            startActivity(intent)
        }
    }
}