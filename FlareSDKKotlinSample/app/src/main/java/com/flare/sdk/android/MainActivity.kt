package com.flare.sdk.android

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.flare.sdk.android.databinding.ActivityMainBinding
import com.flaresafety.sideengine.Constants

class MainActivity : AppCompatActivity() {

    // Production Mode
    private var productionLicense = "your production key"
    private var sandboxLicense = "your sandbox key"
    private val secretKey = "secret key"

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

        process()
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

    private fun process() {
        viewBinding.btnSOS.visibility = View.VISIBLE
        viewBinding.btnEnableFlareAware.visibility = View.VISIBLE

        region = resources.getStringArray(R.array.region_list)[0]

        mode = if (viewBinding.rgEnvironment.checkedRadioButtonId == R.id.rbSandBox) {
            Constants.ENVIRONMENT_SANDBOX
        } else {
            Constants.ENVIRONMENT_PRODUCTION
        }
    }

    private fun setListener() {

        viewBinding.switchHazard.setOnCheckedChangeListener { _, isChecked ->
            run {
                isHazardEnabled = isChecked
            }
        }

        viewBinding.spinRegion.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, position: Int, p3: Long) {
                    region = resources.getStringArray(R.array.region_list)[position]
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {

                }
            }

        viewBinding.rgEnvironment.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.rbProduction) {
                // The switch is checked.
                viewBinding.rbProduction.text = getString(R.string.production_mode)
                mode = Constants.ENVIRONMENT_PRODUCTION
            } else {
                // The switch isn't checked.
                viewBinding.rbSandBox.text = getString(R.string.sandbox_mode)
                mode = Constants.ENVIRONMENT_SANDBOX
            }

            viewBinding.btnSOS.visibility = View.VISIBLE
            viewBinding.btnEnableFlareAware.visibility = View.VISIBLE

        }

        viewBinding.btnStandard.setOnClickListener {

            val intent = Intent(this, StandardThemeActivity::class.java)
            intent.putExtra("mode", mode)
            intent.putExtra("secretKey", secretKey)
            intent.putExtra("region", region)
            intent.putExtra("isHazardEnabled", isHazardEnabled)
            intent.putExtra(
                "lic",
                if (Constants.ENVIRONMENT_PRODUCTION.equals(mode)) productionLicense else sandboxLicense
            )
            startActivity(intent)
        }

        viewBinding.btnCustom.setOnClickListener {

            val intent = Intent(this, CustomThemeActivity::class.java)
            intent.putExtra("mode", mode)
            intent.putExtra("secretKey", secretKey)
            intent.putExtra("region", region)
            intent.putExtra("isHazardEnabled", isHazardEnabled)
            intent.putExtra(
                "lic",
                if (Constants.ENVIRONMENT_PRODUCTION.equals(mode)) productionLicense else sandboxLicense
            )
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


        viewBinding.btnEnableFlareAware.setOnClickListener {

            val intent = Intent(this, EnableFlareAwareActivity::class.java)
            intent.putExtra("mode", mode)
            intent.putExtra("secretKey", secretKey)
            intent.putExtra("region", region)
            intent.putExtra("isHazardEnabled", isHazardEnabled)
            intent.putExtra(
                "lic",
                if (Constants.ENVIRONMENT_PRODUCTION.equals(mode)) productionLicense else sandboxLicense
            )
            startActivity(intent)
        }

        viewBinding.btnHazards.setOnClickListener {

            val intent = Intent(this, HazardsActivity::class.java)
            intent.putExtra("secretKey", secretKey)
            intent.putExtra("region", region)
            intent.putExtra("isHazardEnabled", isHazardEnabled)
            intent.putExtra( "lic", productionLicense )
            startActivity(intent)
        }


    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == postNotificationCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted. You can now send notifications.
            } else {
                // Permission denied. Handle accordingly (e.g., show a message or disable notification functionality).
            }
        }
    }
}