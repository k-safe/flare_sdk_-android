package com.app.flaresdkimplementation

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.app.flaresdkimplementation.databinding.ActivityThemeBinding
import com.sos.busbysideengine.BBSideEngine
import com.sos.busbysideengine.Constants.BBSideOperation
import com.sos.busbysideengine.Constants.BBTheme
import com.sos.busbysideengine.Constants.ENVIRONMENT_PRODUCTION
import com.sos.busbysideengine.rxjavaretrofit.network.model.BBSideEngineListener
import com.sos.busbysideengine.utils.ContactClass
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class StandardThemeActivity : AppCompatActivity(), BBSideEngineListener {

    private val viewBinding: ActivityThemeBinding by lazy {
        ActivityThemeBinding.inflate(layoutInflater)
    }

    private lateinit var bbSideEngine: BBSideEngine
    private var mode: String? = ENVIRONMENT_PRODUCTION

    private var btnTestClicked = false
    private var checkConfiguration = false
    private var mConfidence : String? = null
    private var sosLiveTrackingUrl: String = ""
    companion object {
        fun getRandomNumberString (): String {
            val rnd = Random()
            val number: Int = rnd.nextInt(999999)
            return String.format("%06d", number)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)
        val intent = intent
        mode = intent.getStringExtra("mode")

        viewBinding.tvThemeName.text = getString(R.string.standard_theme)

        bbSideEngine = BBSideEngine.getInstance(this)
        bbSideEngine.showLogs(true)
        bbSideEngine.setBBSideEngineListener(this)
        bbSideEngine.setEnableFlareAwareNetwork(true) //The "enableFlareAwareNetwork" feature is a safety measure designed for cyclists, which allows them to send notifications to nearby fleet users.
        bbSideEngine.setDistanceFilterMeters(20) //It is possible to activate the distance filter in order to transmit location data in the live tracking URL. This will ensure that location updates are transmitted every 20 meters, once the timer interval has been reached.
        bbSideEngine.setLowFrequencyIntervalsSeconds(15) //The default value is 15 seconds, which can be adjusted to meet specific requirements. This parameter will only be utilized in cases where bbSideEngine.setHighFrequencyModeEnabled(false) is invoked.
        bbSideEngine.setHighFrequencyIntervalsSeconds(3) //The default value is 3 seconds, which can be adjusted to meet specific requirements. This parameter will only be utilized in cases where bbSideEngine.setHighFrequencyModeEnabled(true) is invoked.
        bbSideEngine.setHighFrequencyModeEnabled(false) //It is recommended to activate the high frequency mode when the SOS function is engaged in order to enhance the quality of the live tracking experience.
        bbSideEngine.enableActivityTelemetry(true)
//        bbSideEngine.setLocationNotificationTitle("Protection is active")
        bbSideEngine.setStickyEnable(false)

        val lic = if (ENVIRONMENT_PRODUCTION.equals(mode))
            "Your production license key here" else "Your sandbox license key here"


        BBSideEngine.configure(this, lic, mode,
            BBTheme.STANDARD
        )

        //Custom Notification
//        bbSideEngine.setNotificationMainBackgroundColor(R.color.green_221)
//        bbSideEngine.setNotificationMainIcon(R.drawable.ic_lime)
//        bbSideEngine.setLocationNotificationTitle("Notification Title")
//        bbSideEngine.setNotificationDescText("Notification Description")

        //TODO: Customise the SideEngine theme(Optional).
//        bbSideEngine.setIncidentTimeInterval = 45 = //Default 30 seconds
//        bbSideEngine.setIncidentHeader("header")  //Only for standard theme
//        bbSideEngine.setIncidentInfoMessage("message") //Only for standard theme
//        bbSideEngine.setIncidentPageHeaderColor("#ff0000") //Only for standard theme
//        bbSideEngine.setIncidentPageBackgroundColor("#ff00ff") //Only for standard theme
//        bbSideEngine.setIncidentPageHeaderMessageColor("#ffffff") //Only for standard theme
//        bbSideEngine.setSwipeButtonBgColor(R.color.white) //Default "ffffff" //Only for standard theme
//        bbSideEngine.setSwipeButtonTextSize(18) // Default 16 //Only for standard theme
//        bbSideEngine.setSwipeButtonText("Swipe to Cancel") //Only for standard theme
//        bbSideEngine.setImpactBody("Detected a potential fall or impact involving") //This message show in the SMS, email, webook and slack body with rider name passed in this method (bbSideEngine.setRiderName("App user name here");) parameter

        //enableFlareAwareNetwork is a safety for cyclist to send notifcation for near by fleet users

        setListener()
    }

    private fun setListener() {

        viewBinding.ivCloseMain.setOnClickListener{
            finish()
        }

        viewBinding.btnStart.setOnClickListener {

            bbSideEngine.setUserEmail(viewBinding.etvUserEmail.text.toString().trim())
            bbSideEngine.setUserName(viewBinding.etvUserName.text.toString().trim())
            bbSideEngine.setUserCountryCode(viewBinding.etvCountryCode.text.toString().trim())
            bbSideEngine.setUserMobile(viewBinding.etvMobileNumber.text.toString().trim())

            btnTestClicked = false
            if (bbSideEngine.isEngineStarted) {
                bbSideEngine.setUserName(viewBinding.etvUserName.text.toString().trim())
                bbSideEngine.stopSideEngine()
            } else {
                bbSideEngine.startSideEngine(this)
//                bbSideEngine.setUserId(getRandomNumberString())
            }
            viewBinding.mConfidence.text =""
            if (bbSideEngine.isEngineStarted) {
                viewBinding.btnStart.text = getString (R.string.stop)
            } else {
                viewBinding.btnStart.text =getString(R.string.start)
            }
        }

//        viewBinding.etvUserEmail.text = Editable.Factory.getInstance().newEditable("bhavintnm@gmail.com")
//        viewBinding.etvCountryCode.text = Editable.Factory.getInstance().newEditable("91")
//        viewBinding.etvMobileNumber.text = Editable.Factory.getInstance().newEditable("9725162024")
//        viewBinding.etvUserName.text = Editable.Factory.getInstance().newEditable("Bhavin")

    }

    private fun sendEmail() {
        if (viewBinding.etvUserEmail.text.toString() == "") {
            return
        }
        bbSideEngine.sendEmail(viewBinding.etvUserEmail.text.toString())
    }

    private fun sendSMS() {
        if (viewBinding.etvCountryCode.text.toString() == "" ||
            viewBinding.etvUserName.text.toString() == "" ||
            viewBinding.etvMobileNumber.text.toString() == "") {
            return
        }

        val contact = ContactClass()
        contact.countryCode = viewBinding.etvCountryCode.text.toString()
        contact.phoneNumber = viewBinding.etvMobileNumber.text.toString()
        contact.userName = viewBinding.etvUserName.text.toString()
        bbSideEngine.sendSMS(contact)
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
            bbSideEngine.startSideEngine(this)
            if (bbSideEngine.isEngineStarted) {
                viewBinding.btnStart.text = getString(R.string.stop)
            } else {
                viewBinding.btnStart.text = getString(R.string.start)
            }
        } else if (requestCode == 1) {
            viewBinding.btnStart.text = getString(R.string.start)
        }
    }

    override fun onSideEngineCallback(
        status: Boolean,
        type: BBSideOperation?,
        response: JSONObject?
    ) {
        when (type) {
            BBSideOperation.CONFIGURE -> {
                // if status = true Now you can ready to start Side engine process
                checkConfiguration = status
                Log.e("Configured",status.toString())
                viewBinding.progressBar.visibility = View.GONE
            }
            BBSideOperation.START -> {
                //Update your UI here (e.g. update START button color or text here when SIDE engine started)
            }
            BBSideOperation.STOP -> {
                //Update your UI here (e.g. update STOP button color or text here when SIDE engine started)
            }
            BBSideOperation.SMS -> {
                //Returns SMS delivery status and response payload
            }
            BBSideOperation.EMAIL -> {
                //Returns email delivery status and response payload
            }
            BBSideOperation.INCIDENT_DETECTED -> {
                Toast.makeText(this, "INCIDENT_DETECTED",Toast.LENGTH_LONG).show()
                //Threshold reached and you will redirect to countdown page
                //TODO: Set user id
                bbSideEngine.setUserId(getRandomNumberString())
                //TODO: Set rider name
                bbSideEngine.setRiderName(viewBinding.etvUserName.text.toString().trim())

                if (status) {
                    try {
                        //Return incident status and confidence level, you can fetch confidence using the below code:
                        mConfidence = response!!.getString("confidence")
                        if (!mConfidence.equals("")) {
                            viewBinding.mConfidence.visibility = View.VISIBLE
                            try {
                                viewBinding.mConfidence.text = "Confidence: $mConfidence"
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
            }
            BBSideOperation.INCIDENT_CANCEL -> {
                //User canceled countdown countdown to get event here, this called only for if you configured standard theme.
            }
            BBSideOperation.INCIDENT_ALERT_SENT ->{
                //Return the alert sent (returns alert details (i.e. time, location, recipient, success/failure))
            }
            BBSideOperation.RESUME_SIDE_ENGINE ->{
                //
            }
            BBSideOperation.TIMER_STARTED -> {
                //Countdown timer started after breach delay, this called only if you configured standard theme.
            }
            BBSideOperation.TIMER_FINISHED -> {
                sendSMS()
                sendEmail()
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
