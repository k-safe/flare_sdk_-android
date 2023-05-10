package com.app.flaresdkimplementation

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.app.flaresdkimplementation.databinding.ActivitySosBinding
import com.sos.busbysideengine.BBSideEngine
import com.sos.busbysideengine.Constants
import com.sos.busbysideengine.Constants.ENVIRONMENT_PRODUCTION
import com.sos.busbysideengine.rxjavaretrofit.network.model.BBSideEngineListener
import org.json.JSONObject

class EmergencySOSActivity : AppCompatActivity(), BBSideEngineListener {

    private val viewBinding: ActivitySosBinding by lazy {
        ActivitySosBinding.inflate(layoutInflater)
    }
    private lateinit var bbSideEngine: BBSideEngine
    private var checkConfiguration = false
    private var sosLiveTrackingUrl: String = ""
    private var mode: String? = ENVIRONMENT_PRODUCTION

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)
        mode = intent.getStringExtra("mode")


        val lic = "Your production license key here"

        bbSideEngine = BBSideEngine.getInstance(this)
        bbSideEngine.showLogs(true)
        bbSideEngine.setBBSideEngineListener(this)
        bbSideEngine.enableActivityTelemetry(true)

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
        viewBinding.btnSos.setOnClickListener {
            if (checkConfiguration) {
                if (viewBinding.btnSos.text == "Deactivate SOS") {
                    bbSideEngine.deActiveSOS()
                } else {
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
                        val deviceId = Settings.Secure.getString(
                            this.contentResolver,
                            Settings.Secure.ANDROID_ID
                        )
                        bbSideEngine.setUserId(deviceId)
                        bbSideEngine.setUserEmail(viewBinding.etvUserEmail.text.toString().trim())
                        bbSideEngine.setUserName(viewBinding.etvUserName.text.toString().trim())
                        bbSideEngine.activeSOS()
                    }
                }
            }
        }
        viewBinding.btnSOSLinkShare.setOnClickListener {
            val share = Intent(Intent.ACTION_SEND)
            share.setType("text/plain")
            share.putExtra(Intent.EXTRA_TEXT, sosLiveTrackingUrl)
            startActivity(Intent.createChooser(share, "Share link!"))
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
            val deviceId = Settings.Secure.getString(
                this.contentResolver,
                Settings.Secure.ANDROID_ID
            )
            bbSideEngine.setUserId(deviceId)
            bbSideEngine.setUserEmail(viewBinding.etvUserEmail.text.toString().trim())
            bbSideEngine.setUserName(viewBinding.etvUserName.text.toString().trim())
            bbSideEngine.activeSOS()
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
                if (response!!.has("sosLiveTrackingUrl")) {
                    sosLiveTrackingUrl = response!!.getString("sosLiveTrackingUrl")
                    viewBinding.btnSOSLinkShare.visibility = View.VISIBLE
                    viewBinding.btnSos.text = "Deactivate SOS"
                } else if (response.has("Error")) {
                    //
                }
            }

            Constants.BBSideOperation.SOS_DEACTIVATE -> {
                viewBinding.btnSOSLinkShare.visibility = View.GONE
                viewBinding.btnSos.text = "Activate SOS"
            }

            else -> {
                Log.e("No Events Find", ":")
            }
        }
    }
}


