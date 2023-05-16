package com.app.flaresdkimplementation

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.app.flaresdkimplementation.databinding.ActivityFlareawareBinding
import com.sos.busbysideengine.BBSideEngine
import com.sos.busbysideengine.Constants
import com.sos.busbysideengine.Constants.ENVIRONMENT_PRODUCTION
import com.sos.busbysideengine.rxjavaretrofit.network.model.BBSideEngineListener
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

        bbSideEngine = BBSideEngine.getInstance(this)
        bbSideEngine.showLogs(true)
        bbSideEngine.setBBSideEngineListener(this)
        bbSideEngine.enableActivityTelemetry(true)
        bbSideEngine.setEnableFlareAwareNetwork(true)
        bbSideEngine.setHighFrequencyModeEnabled(true) //It is recommended to activate the high frequency mode when the SOS function is engaged in order to enhance the quality of the live tracking experience.
        bbSideEngine.setDistanceFilterMeters(20) //It is possible to activate the distance filter in order to transmit location data in the live tracking URL. This will ensure that location updates are transmitted every 20 meters, once the timer interval has been reached.
        bbSideEngine.setLowFrequencyIntervalsSeconds(15) //The default value is 15 seconds, which can be adjusted to meet specific requirements. This parameter will only be utilized in cases where bbSideEngine.setHighFrequencyModeEnabled(false) is invoked.
        bbSideEngine.setHighFrequencyIntervalsSeconds(3) //The default value is 3 seconds, which can be adjusted to meet specific requirements. This parameter will only be utilized in cases where bbSideEngine.setHighFrequencyModeEnabled(true) is invoked.

        BBSideEngine.configure(this,
            lic,
            ENVIRONMENT_PRODUCTION,
            Constants.BBTheme.STANDARD
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
        } else if (requestCode == 1) {
//            viewBinding.btnStart.text = getString(R.string.start)
        }
    }

    override fun onSideEngineCallback(
        status: Boolean,
        type: Constants.BBSideOperation?,
        response: JSONObject?
    ) {
        when (type) {
            Constants.BBSideOperation.CONFIGURE -> {
                // if status = true Now you can ready to start Side engine process
                checkConfiguration = status
                Log.e("Configured", status.toString())
                viewBinding.progressBar.visibility = View.GONE
            }

            Constants.BBSideOperation.START -> {
                //Update your UI here (e.g. update START button color or text here when SIDE engine started)
            }

            Constants.BBSideOperation.STOP -> {
                //Update your UI here (e.g. update STOP button color or text here when SIDE engine started)
            }

            Constants.BBSideOperation.SMS -> {
                //Returns SMS delivery status and response payload
            }

            Constants.BBSideOperation.EMAIL -> {
                //Returns email delivery status and response payload
            }

            Constants.BBSideOperation.INCIDENT_DETECTED -> {
                Toast.makeText(this, "INCIDENT_DETECTED", Toast.LENGTH_LONG).show()
                //Threshold reached and you will redirect to countdown page
            }

            Constants.BBSideOperation.INCIDENT_CANCEL -> {
                //User canceled countdown countdown to get event here, this called only for if you configured standard theme.
            }

            Constants.BBSideOperation.INCIDENT_ALERT_SENT -> {
                //Return the alert sent (returns alert details (i.e. time, location, recipient, success/failure))
            }

            Constants.BBSideOperation.TIMER_STARTED -> {
                //Countdown timer started after breach delay, this called only if you configured standard theme.
            }

            Constants.BBSideOperation.TIMER_FINISHED -> {
                //Countdown timer finished and jump to the incident summary page, this called only if you configured standard theme.
            }

            Constants.BBSideOperation.SOS_ACTIVATE -> {
                // sos activate
            }

            Constants.BBSideOperation.SOS_DEACTIVATE -> {
                // sos deactivate
            }

            Constants.BBSideOperation.START_FLARE_AWARE -> {
                // Start flare aware
                if (response != null) {
                    try {
                        var error = response.getString("Error")
                    } catch (e: Exception) {
                        Log.e("Error: ", e.stackTraceToString())
                    }
                } else {
                    isStartFlareAware = true
                    viewBinding.btnStartFlareAware.text = getString(R.string.stop_flare_aware)
                }

            }

            Constants.BBSideOperation.STOP_FLARE_AWARE -> {
                // Stop flare aware
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
