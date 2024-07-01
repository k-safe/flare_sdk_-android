package com.app.flaresdkimplementation

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
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

        //"Your production license key here"
        val lic = intent.getStringExtra("lic")
        val secretKey = intent.getStringExtra("secretKey")

        bbSideEngine = BBSideEngine.getInstance()
        bbSideEngine.showLogs(true)
        bbSideEngine.setBBSideEngineListener(this)
        bbSideEngine.enableActivityTelemetry(true)

        bbSideEngine.configure(
            this,
            lic,
            secretKey,
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
                    bbSideEngine.stopSOS()
                } else {

                        val deviceId = Settings.Secure.getString(
                            this.contentResolver,
                            Settings.Secure.ANDROID_ID
                        )
                        bbSideEngine.setUserId(deviceId)
                        bbSideEngine.setUserEmail(viewBinding.etvUserEmail.text.toString().trim())
                        bbSideEngine.setUserName(viewBinding.etvUserName.text.toString().trim())
                        bbSideEngine.startSOS()
                }
            }
        }
        viewBinding.btnSOSLinkShare.setOnClickListener {
            val share = Intent(Intent.ACTION_SEND)
            share.type = "text/plain"
            share.putExtra(Intent.EXTRA_TEXT, sosLiveTrackingUrl)
            startActivity(Intent.createChooser(share, "Share link!"))
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
            Constants.BBSideOperation.START_SOS -> {
                //*The SOS function has been activated. You may now proceed to update your user interface and share a live location tracking link with your social contacts, thereby enabling them to access your real-time location.*//
                if (response!!.has("sosLiveTrackingUrl")) {
                    sosLiveTrackingUrl = response.getString("sosLiveTrackingUrl")
                    viewBinding.btnSOSLinkShare.visibility = View.VISIBLE
                    viewBinding.btnSos.text = getString(R.string.deactivate_sos)
                } else if (response.has("Error")) {
                    // handle error here
                }
            }
            Constants.BBSideOperation.STOP_SOS -> {
                //Disabling the SOS function will cease the transmission of location data to the live tracking dashboard and free up system memory resources, thereby conserving battery and data consumption.
                viewBinding.btnSOSLinkShare.visibility = View.GONE
                viewBinding.btnSos.text = getString(R.string.activate_sos)
            }
            else -> {
                Log.e("No Events Find", ":")
            }
        }
    }
}


