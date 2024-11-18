package com.app.flaresdkimplementation

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.app.flaresdkimplementation.databinding.ActivityFlareawareBinding
import com.flaresafety.sideengine.BBSideEngine
import com.flaresafety.sideengine.Constants
import com.flaresafety.sideengine.Constants.ENVIRONMENT_PRODUCTION
import com.flaresafety.sideengine.rxjavaretrofit.network.model.BBSideEngineListener
import org.json.JSONObject

class EnableFlareAwareActivity : AppCompatActivity(), BBSideEngineListener {

    private val viewBinding: ActivityFlareawareBinding by lazy {
        ActivityFlareawareBinding.inflate(layoutInflater)
    }
    private lateinit var bbSideEngine: BBSideEngine
    private var checkConfiguration = false
    private var isStartFlareAware = false
    private var mode: String? = ENVIRONMENT_PRODUCTION

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        val intent = intent
        mode = intent.getStringExtra("mode")

        //"Your production license key here"
        val lic = intent.getStringExtra("lic")
        val secretKey = intent.getStringExtra("secretKey")
        val region = intent.getStringExtra("region")

        bbSideEngine = BBSideEngine.getInstance()
        bbSideEngine.showLogs(true)
        bbSideEngine.setBBSideEngineListener(this)
//        bbSideEngine.enableActivityTelemetry(true)
        bbSideEngine.setHighFrequencyModeEnabled(true) //It is recommended to activate the high frequency mode when the SOS function is engaged in order to enhance the quality of the live tracking experience.
        bbSideEngine.setDistanceFilterMeters(20) //It is possible to activate the distance filter in order to transmit location data in the live tracking URL. This will ensure that location updates are transmitted every 20 meters, once the timer interval has been reached.
        bbSideEngine.setLowFrequencyIntervalsSeconds(15) //The default value is 15 seconds, which can be adjusted to meet specific requirements. This parameter will only be utilized in cases where bbSideEngine.setHighFrequencyModeEnabled(false) is invoked.
        bbSideEngine.setHighFrequencyIntervalsSeconds(3) //The default value is 3 seconds, which can be adjusted to meet specific requirements. This parameter will only be utilized in cases where bbSideEngine.setHighFrequencyModeEnabled(true) is invoked.
        bbSideEngine.setHazardFeatureEnabled(false) //The default hazard feature is enabled ( deafult value is true ), which can be adjusted to meet specific requirements. You can turn off by passing bbSideEngine.setHazardFeatureEnabled(false).

        bbSideEngine.configure(
            this,
            lic,
            secretKey,
            mode,
            Constants.BBTheme.STANDARD,
            region
        )
        setListener()
    }

    @SuppressLint("HardwareIds")
    private fun setListener() {
        viewBinding.ivCloseMain.setOnClickListener {
            finish()
        }
        viewBinding.btnStartFlareAware.setOnClickListener {
            if (checkConfiguration) {
                // code here
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        0
                    )
                } else {
                    if (isStartFlareAware) {
                        bbSideEngine.stopFlareAware()
                    } else {
                        bbSideEngine.startFlareAware()
                    }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (!checkConfiguration) {
            return
        }
        if (requestCode == 0) {
            Handler(Looper.getMainLooper()).postDelayed({
                bbSideEngine.startFlareAware()
            },2000)
        }
    }

    override fun onSideEngineCallback(
        status: Boolean,
        type: Constants.BBSideOperation?,
        response: JSONObject?
    ) {
        when (type) {
            Constants.BBSideOperation.CONFIGURE -> {
                //*You now have the capability to call star Flare Aware function at any time. In the event that a user input button is unavailable, you may start the Flare Aware using the function provided below.:*//
                checkConfiguration = status
                Log.e("Configured", status.toString())
                viewBinding.progressBar.visibility = View.GONE
            }
            Constants.BBSideOperation.START_FLARE_AWARE -> {
                //*The Flare Aware function has been activated. You may now proceed to update your user interface.*//
                if (response != null) {
                    try {
                        response.getString("Error")
                    } catch (e: Exception) {
                        Log.e("Error: ", e.stackTraceToString())
                    }
                } else {
                    isStartFlareAware = true
                    viewBinding.btnStartFlareAware.text = getString(R.string.stop_flare_aware)
                }
            }
            Constants.BBSideOperation.STOP_FLARE_AWARE -> {
                //Disabling the Flare Aware function will cease the transmission of location data to the Flare aware network.
                isStartFlareAware = false
                viewBinding.btnStartFlareAware.text = getString(R.string.start_flare_aware)
            }
            else -> {
                Log.e("No Events Find", ":")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        bbSideEngine.stopFlareAware()
    }
}
