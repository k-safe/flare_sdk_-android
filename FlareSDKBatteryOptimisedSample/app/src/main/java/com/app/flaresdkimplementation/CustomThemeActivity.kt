package com.app.flaresdkimplementation

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.app.ActivityCompat
import com.sos.busbysideengine.BBSideEngine
import com.sos.busbysideengine.Constants
import com.sos.busbysideengine.Constants.BBTheme
import com.sos.busbysideengine.Constants.ENVIRONMENT_PRODUCTION
import com.sos.busbysideengine.rxjavaretrofit.network.model.BBSideEngineListener
import com.sos.busbysideengine.utils.Common
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class CustomThemeActivity : AppCompatActivity(), BBSideEngineListener {

    private lateinit var rlTestIncident: RelativeLayout
    private lateinit var btnStart: AppCompatButton
    private lateinit var btnTestIncident: AppCompatButton
    private lateinit var etvCountryCode: EditText
    private lateinit var etvMobileNumber: EditText
    private lateinit var etvUserName: EditText
    private lateinit var etvUserEmail: EditText
    private lateinit var tvConfidence: TextView
    private lateinit var tvThemeName: TextView
    private lateinit var ivCloseMain: ImageView
    private lateinit var ivCUIClose: ImageView
    private lateinit var bbSideEngine: BBSideEngine
    private lateinit var progressBar: ProgressBar

    private var btnTestClicked = false
    private var checkConfiguration = false
    private var mConfidence : String? = null


    companion object {
        fun getRandomNumberString (): String {
            val rnd = Random()
            val number: Int = rnd.nextInt(999999)
            return String.format("%06d", number)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_theme)

        btnStart = findViewById(R.id.btnStart)
        btnTestIncident = findViewById(R.id.btnTest)
        etvCountryCode = findViewById(R.id.etvCountryCode)
        etvMobileNumber = findViewById(R.id.etvMobileNumber)
        etvUserName = findViewById(R.id.etvUserName)
        etvUserEmail = findViewById(R.id.etvUserEmail)
        tvConfidence = findViewById(R.id.mConfidence)
        tvThemeName = findViewById(R.id.tvThemeName)
        ivCloseMain = findViewById(R.id.ivCloseMain)
        ivCUIClose = findViewById(R.id.ivCUIClose)
        progressBar = findViewById(R.id.progressBar)

        rlTestIncident = findViewById(R.id.rlTestIncident)
        tvThemeName.text = getString(R.string.custom_theme)

        bbSideEngine = BBSideEngine.getInstance(this)
        bbSideEngine.showLogs(true)
        bbSideEngine.setBBSideEngineListener(this)
        bbSideEngine.enableActivityTelemetry(true)
        bbSideEngine.setLocationNotificationTitle("Protection is active")

        //Custom Notification
//        bbSideEngine.setLocationNotificationTitle("Protection is active");
//        bbSideEngine.setNotificationMainBackgroundColor(R.color.white);
//        bbSideEngine.setNotificationMainIcon(R.drawable.ic_launcher);
//        bbSideEngine.setNotificationDescText("Notification Description");

        //Sandbox mode used only for while developing your App (You can use theme STANDARD OR CUSTOM)
        //BBSideEngine.configure(this,
        //"Your license key here",
        //ENVIRONMENT_SANDBOX, STANDARD);

        BBSideEngine.configure(
            this,
            "Your license key here",
            ENVIRONMENT_PRODUCTION,
            BBTheme.CUSTOM
        )

        setListener()
    }

    private fun setListener() {

        ivCloseMain.setOnClickListener{
            finish()
        }
        ivCUIClose.setOnClickListener{
            rlTestIncident.visibility = View.GONE
        }

        btnStart.setOnClickListener {

            bbSideEngine.setUserEmail(etvUserEmail.text.toString().trim())
            bbSideEngine.setUserName(etvUserName.text.toString().trim())

            btnTestClicked = false
            if (bbSideEngine.isEngineStarted) {
                bbSideEngine.setUserName(etvUserName.text.toString().trim())
                bbSideEngine.stopSideEngine()
            } else {
                bbSideEngine.startSideEngine(this, false)
            }
            tvConfidence.text =""
            if (bbSideEngine.isEngineStarted){
                btnStart.text = getString (R.string.stop)
            } else {
                btnStart.text =getString(R.string.start)
            }
        }

        btnTestIncident.setOnClickListener {

            bbSideEngine.setUserEmail(etvUserEmail.text.toString().trim())
            bbSideEngine.setUserName(etvUserName.text.toString().trim())

            if (!bbSideEngine.isEngineStarted) {
                bbSideEngine.stopSideEngine()
                btnStart.text = getString (R.string.start)
                tvConfidence.text =""
            }

            btnTestClicked = true
            bbSideEngine.startSideEngine(this, btnTestClicked)
            if (checkConfiguration && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                rlTestIncident.visibility = View.VISIBLE;
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
            bbSideEngine.startSideEngine(this, btnTestClicked)
            if (!btnTestClicked) {
                if (bbSideEngine.isEngineStarted) {
                    btnStart.text = getString(R.string.stop)
                } else {
                    btnStart.text = getString(R.string.start)
                }
            } else {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    rlTestIncident.visibility = View.VISIBLE;
                }
            }
        } else if (requestCode == 1) {
            btnStart.text = getString(R.string.start)
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
                progressBar.visibility = View.GONE
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
                //Threshold reached and you will redirect to countdown page
                Log.w("CustomThemeActivity", "INCIDENT_DETECTED")
                rlTestIncident.visibility = View.GONE;

                //TODO: Set user id
                bbSideEngine.setUserId(getRandomNumberString())
                //TODO: Set rider name
                bbSideEngine.setRiderName(etvUserName.text.toString().trim())
                if (status) {
                    try {
                        val mCustomTheme = response!!.getBoolean("customTheme")
                        mConfidence = response.getString("confidence")
                        if (!mConfidence.equals("")) {
                            tvConfidence.visibility = View.VISIBLE
                            try {
                                tvConfidence.text = "Confidence: + $mConfidence"
                            } catch (e: Exception) {
                            }
                        }
                        Log.e("", mCustomTheme.toString() + "")
                        Log.e("mConfidence", mConfidence.toString() + "")
                        //TODO: If SDK is configured custom UI to open your screen here (MAKE SURE CONFIGURE SDK SELECTED CUSTOM THEME)
                        if (mCustomTheme) {

                            if (Common.getInstance().isAppInBackground) {

                                //TODO: Set user id
                                BBSideEngine.getInstance(null).setUserId(getRandomNumberString())

                                //TODO: Set rider name
                                BBSideEngine.getInstance(null).setRiderName(etvUserName.text.toString().trim())

                                //TODO: call method for fetching W3W Location data
                                BBSideEngine.getInstance(null).fetchWhat3WordLocation(this@CustomThemeActivity)

                                //TODO: Send Email
                                BBSideEngine.getInstance(null).sendEmail(etvUserEmail.text.toString().trim(), btnTestClicked) // Replace your emergency email address

                                if (!btnTestClicked) {
                                    //TODO: notify to partner
                                    BBSideEngine.getInstance(null).notifyPartner()
                                }

                                BBSideEngine.getInstance(null).resumeSensorIfAppInBackground()

                            } else {
                                val intent = Intent(this, CustomUiActivity::class.java)
                                intent.putExtra("userName", etvUserName.text.toString().trim())
                                intent.putExtra("email", etvUserEmail.text.toString().trim())
                                intent.putExtra("btnTestClicked", btnTestClicked)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                startActivity(intent)
                            }
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
            }
            Constants.BBSideOperation.INCIDENT_CANCEL -> {
                //User canceled countdown countdown to get event here, this called only for if you configured standard theme.
            }
            Constants.BBSideOperation.INCIDENT_AUTO_CANCEL -> {
                //Auto canceled countdown countdown to get event here, this called only for if you configured standard theme.
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
            else -> {
                Log.e("No Events Find",":")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        bbSideEngine.stopSideEngine()
    }
}
