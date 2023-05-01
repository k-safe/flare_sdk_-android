package com.app.flaresdkimplementation

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
    private var mode: String? = ENVIRONMENT_PRODUCTION

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        val intent = intent
        mode = intent.getStringExtra("mode")

        var lic = if (ENVIRONMENT_PRODUCTION.equals(mode))
            "Your production license key here" else "Your sandbox license key here"

        lic  = "a6628abe-aa88-47fc-b3a8-6bbb702c44c5";

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
        viewBinding.ivCloseMain.setOnClickListener{
            finish()
        }
        viewBinding.btnStartFlareAware.setOnClickListener {
            if (checkConfiguration) {
                // code here
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
                Log.e("Configured",status.toString())
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
                Toast.makeText(this, "INCIDENT_DETECTED",Toast.LENGTH_LONG).show()
                //Threshold reached and you will redirect to countdown page
            }
            Constants.BBSideOperation.INCIDENT_CANCEL -> {
                //User canceled countdown countdown to get event here, this called only for if you configured standard theme.
            }
            Constants.BBSideOperation.INCIDENT_ALERT_SENT ->{
            //Return the alert sent (returns alert details (i.e. time, location, recipient, success/failure))
            }
            Constants.BBSideOperation.TIMER_STARTED -> {
            //Countdown timer started after breach delay, this called only if you configured standard theme.
            }
            Constants.BBSideOperation.TIMER_FINISHED -> {
            //Countdown timer finished and jump to the incident summary page, this called only if you configured standard theme.
            }
            Constants.BBSideOperation.SOS_ACTIVATE ->{
               // sos activate
            }
            Constants.BBSideOperation.SOS_DEACTIVATE ->{
               // sos deactivate
            }
            else -> {
                Log.e("No Events Find",":")
            }
        }
    }
}
