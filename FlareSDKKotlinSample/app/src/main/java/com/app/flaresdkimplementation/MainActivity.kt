package com.app.flaresdkimplementation

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.RadioGroup.OnCheckedChangeListener
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.app.flaresdkimplementation.databinding.ActivityMainBinding
import com.flaresafety.sideengine.Constants
import com.flaresafety.sideengine.utils.Common

class MainActivity : AppCompatActivity() {

    // Production Mode
    private var productionLicense = "4afb485e-a181-4ce7-98f6-38cfe1afc748"
    private var sandboxLicense = "b6dd8509-d50e-48cc-af9e-ce9dcd712132"
    private val secretKey = "EN7nPbKOc57COfYaPy66j8bXhlvOkrcX87c7mC76"

    // Sandbox Mode
//    private var productionLicense = "3b08975d-de34-4850-99e7-997381d8682b"
//    private val sandboxLicense = "804ff31c-a9e5-404e-8d8c-5b7a1c2589e9"
//    private var secretKey = "LxbtMfP2My9VXiGWeuvwmaWpkaaWHZ8G415eRvUR"

    private var mode = Constants.ENVIRONMENT_SANDBOX
    private var postNotificationCode = 1221
    var region = ""
    private var isHazardEnabled = true

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

        viewBinding.switchHazard.setOnCheckedChangeListener {_, isChecked ->
            run {
                isHazardEnabled = isChecked
            }
        }

        region = resources.getStringArray(R.array.region_list)[0]


        viewBinding.spinRegion.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {

                region = resources.getStringArray(R.array.region_list)[p2]
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

        }

        viewBinding.btnStandard.setOnClickListener {
            mode = if(viewBinding.rgEnvironment.checkedRadioButtonId == R.id.rbSandBox){
                Constants.ENVIRONMENT_SANDBOX
            } else {
                Constants.ENVIRONMENT_PRODUCTION
            }
            val intent = Intent(this, StandardThemeActivity::class.java)
            intent.putExtra("mode", mode)
            intent.putExtra("secretKey", secretKey)
            intent.putExtra("region", region)
            intent.putExtra("isHazardEnabled", isHazardEnabled)
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
            intent.putExtra("region", region)
            intent.putExtra("isHazardEnabled", isHazardEnabled)
            intent.putExtra("lic",
                if(Constants.ENVIRONMENT_PRODUCTION.equals(mode)) productionLicense else sandboxLicense)
            startActivity(intent)
        }

        viewBinding.btnSOS.setOnClickListener {
            val intent = Intent(this, EmergencySOSActivity::class.java)
            intent.putExtra("lic", productionLicense)
            intent.putExtra("secretKey", secretKey)
            intent.putExtra("region", region)
            intent.putExtra("isHazardEnabled", isHazardEnabled)
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
            intent.putExtra("region", region)
            intent.putExtra("isHazardEnabled", isHazardEnabled)
            intent.putExtra("lic",
                if(Constants.ENVIRONMENT_PRODUCTION.equals(mode)) productionLicense else sandboxLicense)
            startActivity(intent)
        }

        viewBinding.btnHazards.setOnClickListener {
            mode = if(viewBinding.rgEnvironment.checkedRadioButtonId == R.id.rbSandBox){
                Constants.ENVIRONMENT_SANDBOX
            } else {
                Constants.ENVIRONMENT_PRODUCTION
            }
            val intent = Intent(this, HazardsActivity::class.java)
            intent.putExtra("mode", mode)
            intent.putExtra("secretKey", secretKey)
            intent.putExtra("region", region)
            intent.putExtra("isHazardEnabled", isHazardEnabled)
            intent.putExtra("lic",
                if(Constants.ENVIRONMENT_PRODUCTION.equals(mode)) productionLicense else sandboxLicense)
            startActivity(intent)
        }


    }
}