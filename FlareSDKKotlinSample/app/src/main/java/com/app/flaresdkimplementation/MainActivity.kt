package com.app.flaresdkimplementation

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
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
        viewBinding.btnSOS.visibility = View.VISIBLE
        viewBinding.btnEnableFlareAware.visibility = View.VISIBLE
        viewBinding.btnStandard.setOnClickListener {
            val mode = if(viewBinding.rgEnvironment.checkedRadioButtonId == R.id.rbSandBox){
                Constants.ENVIRONMENT_SANDBOX
            } else {
                Constants.ENVIRONMENT_PRODUCTION
            }
            val intent = Intent(this, StandardThemeActivity::class.java)
            intent.putExtra("mode",mode)
            startActivity(intent)
        }

        viewBinding.btnCustom.setOnClickListener {
            val mode = if(viewBinding.rgEnvironment.checkedRadioButtonId == R.id.rbSandBox){
                Constants.ENVIRONMENT_SANDBOX
            } else {
                Constants.ENVIRONMENT_PRODUCTION
            }
            val intent = Intent(this, CustomThemeActivity::class.java)
            intent.putExtra("mode",mode)
            startActivity(intent)
        }
        viewBinding.btnSOS.setOnClickListener {
            val intent = Intent(this, EmergencySOSActivity::class.java)
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

            val mode =
                if(viewBinding.rgEnvironment.checkedRadioButtonId == R.id.rbSandBox){
                    Constants.ENVIRONMENT_SANDBOX
                } else {
                    Constants.ENVIRONMENT_PRODUCTION
                }
            val intent = Intent(this, EnableFlareAwareActivity::class.java)
            intent.putExtra("mode",mode)
            startActivity(intent)

        }

    }
}
