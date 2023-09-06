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

        init();
        setListener()
    }

    private fun init() {

        //"Your production license key here"
        val lic = intent.getStringExtra("lic")

        bbSideEngine = BBSideEngine.getInstance()
        bbSideEngine.showLogs(true)
        bbSideEngine.setBBSideEngineListener(this)
        bbSideEngine.enableActivityTelemetry(true)

        bbSideEngine.configure(this,
            lic,
            ENVIRONMENT_PRODUCTION,
            Constants.BBTheme.STANDARD
        )
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
                //*You now have the capability to activate an SOS signal at any time. In the event that a user input button is unavailable, you may activate the SOS signal using the function provided below.:*//
                checkConfiguration = status
                Log.e("Configured", status.toString())
                viewBinding.progressBar.visibility = View.GONE
            }
            Constants.BBSideOperation.SOS_ACTIVATE -> {
                //*The SOS function has been activated. You may now proceed to update your user interface and share a live location tracking link with your social contacts, thereby enabling them to access your real-time location.*//
                if (response!!.has("sosLiveTrackingUrl")) {
                    sosLiveTrackingUrl = response!!.getString("sosLiveTrackingUrl")
                    viewBinding.btnSOSLinkShare.visibility = View.VISIBLE
                    viewBinding.btnSos.text = "Deactivate SOS"
                } else if (response.has("Error")) {
                    //
                }
            }
            Constants.BBSideOperation.SOS_DEACTIVATE -> {
                //Disabling the SOS function will cease the transmission of location data to the live tracking dashboard and free up system memory resources, thereby conserving battery and data consumption.
                viewBinding.btnSOSLinkShare.visibility = View.GONE
                viewBinding.btnSos.text = "Activate SOS"
            }
            else -> {
                Log.e("No Events Find", ":")
            }
        }
    }
}


