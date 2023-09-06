package com.app.flaresdkimplementation

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.app.flaresdkimplementation.databinding.ActivityMainBinding
import com.sos.busbysideengine.Constants

class MainActivity : AppCompatActivity() {

    private var productionLicense = "8b53824f-ed7a-4829-860b-f6161c568fad"
    private var sandboxLicense = "9518a8f7-a55f-41f4-9eaa-963bdb1fce5f"

    private var mode = Constants.ENVIRONMENT_PRODUCTION
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
        viewBinding.btnSOS.visibility = View.VISIBLE
        viewBinding.btnEnableFlareAware.visibility = View.VISIBLE

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

        viewBinding.btnStandard.setOnClickListener {
            mode = if(viewBinding.rgEnvironment.checkedRadioButtonId == R.id.rbSandBox){
                Constants.ENVIRONMENT_SANDBOX
            } else {
                Constants.ENVIRONMENT_PRODUCTION
            }
            val intent = Intent(this, StandardThemeActivity::class.java)
            intent.putExtra("mode", mode)
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
            intent.putExtra("lic",
                if(Constants.ENVIRONMENT_PRODUCTION.equals(mode)) productionLicense else sandboxLicense)
            startActivity(intent)
        }

        viewBinding.btnSOS.setOnClickListener {
            val intent = Intent(this, EmergencySOSActivity::class.java)
            intent.putExtra("lic", productionLicense)
            startActivity(intent)
        }

        viewBinding.btnEnableFlareAware.setOnClickListener {
            mode = if(viewBinding.rgEnvironment.checkedRadioButtonId == R.id.rbSandBox){
                Constants.ENVIRONMENT_SANDBOX
            } else {
                Constants.ENVIRONMENT_PRODUCTION
            }
            val intent = Intent(this, EnableFlareAwareActivity::class.java)
            intent.putExtra("mode", mode)
            intent.putExtra("lic",
                if(Constants.ENVIRONMENT_PRODUCTION.equals(mode)) productionLicense else sandboxLicense)
            startActivity(intent)
        }
    }
}
