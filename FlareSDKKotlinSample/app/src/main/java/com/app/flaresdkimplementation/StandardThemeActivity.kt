package com.app.flaresdkimplementation

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
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

        bbSideEngine = BBSideEngine.getInstance()
        bbSideEngine.showLogs(true)
        bbSideEngine.setBBSideEngineListener(this)
//        bbSideEngine.setEnableFlareAwareNetwork(true) //The "enableFlareAwareNetwork" feature is a safety measure designed for cyclists, which allows them to send notifications to nearby fleet users.
//        bbSideEngine.setDistanceFilterMeters(20) //It is possible to activate the distance filter in order to transmit location data in the live tracking URL. This will ensure that location updates are transmitted every 20 meters, once the timer interval has been reached.
//        bbSideEngine.setLowFrequencyIntervalsSeconds(15) //The default value is 15 seconds, which can be adjusted to meet specific requirements. This parameter will only be utilized in cases where bbSideEngine.setHighFrequencyModeEnabled(false) is invoked.
//        bbSideEngine.setHighFrequencyIntervalsSeconds(3) //The default value is 3 seconds, which can be adjusted to meet specific requirements. This parameter will only be utilized in cases where bbSideEngine.setHighFrequencyModeEnabled(true) is invoked.
//        bbSideEngine.setHighFrequencyModeEnabled(false) //It is recommended to activate the high frequency mode when the SOS function is engaged in order to enhance the quality of the live tracking experience.
        bbSideEngine.enableActivityTelemetry(true)
//        bbSideEngine.setLocationNotificationTitle("Protection is active")
        bbSideEngine.setStickyEnable(true)

        //"Your production license key here" or "Your sandbox license key here"
        val lic = intent.getStringExtra("lic")


        bbSideEngine.configure(this, lic, mode,
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
            if (checkConfiguration) {
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
            }
        }
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

    @SuppressLint("SetTextI18n")
    override fun onSideEngineCallback(
        status: Boolean,
        type: BBSideOperation?,
        response: JSONObject?
    ) {
        when (type) {
            BBSideOperation.CONFIGURE -> {
                //*You are now able to initiate the SIDE engine process at any time. In the event that there is no user input button available to commence the activity, you may commence the SIDE engine by executing the following command:*//
                checkConfiguration = status
                Log.e("Configured",status.toString())
                viewBinding.progressBar.visibility = View.GONE
            }
            BBSideOperation.START -> {
                //*Update your user interface accordingly once the lateral engine has been initiated (for instance, modify the colour or text of the START button) to reflect the change in state.*//
            }
            BBSideOperation.STOP -> {
                //Update the user interface (UI) in this section to reflect the cessation of the side engine (e.g., amend the colour or text of the STOP button accordingly).
            }
            BBSideOperation.INCIDENT_DETECTED -> {
                //The user has identified an incident, and if necessary, it may be appropriate to log the incident in either the analytics system or an external database. Refrain from invoking any side engine methods at this juncture.
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
                //The incident has been canceled because of something the user did, so you can go ahead and register any analytics events if needed.
            }
            BBSideOperation.INCIDENT_ALERT_SENT ->{
                //This message is intended solely to provide notification regarding the transmission status of alerts. It is unnecessary to invoke any SIDE engine functions in this context.
            }
            BBSideOperation.RESUME_SIDE_ENGINE ->{
                //The lateral engine has been restarted, and we are currently monitoring the device's sensors and location in order to analyse another potential incident. There is no requirement to invoke any functions from either party in this context, as the engine on the side will handle the task automatically.
            }
            BBSideOperation.TIMER_STARTED -> {
                //A 30-second countdown timer has started, and the SIDE engine is waiting for a response from the user or an automatic cancellation event. If no events are received within the 30-second intervals of the timer, the SIDE engine will log the incident on the dashboard.
            }
            BBSideOperation.INCIDENT_AUTO_CANCEL -> {
                //The incident has been automatically cancelled. If necessary, you may log the incident in the analytics system. Refrain from invoking any side engine methods at this juncture.
            }
            BBSideOperation.TIMER_FINISHED -> {
                //After the 30-second timer ended, the SIDE engine began the process of registering the incident on the dashboard and sending notifications to emergency contacts.
                sendSMS()
                sendEmail()
            }
            BBSideOperation.SMS -> {
                //This message is intended solely to provide notification regarding the transmission status of SMS. It is unnecessary to invoke any SIDE engine functions in this context.
            }
            BBSideOperation.EMAIL -> {
                //This message is intended solely to provide notification regarding the transmission status of Email. It is unnecessary to invoke any SIDE engine functions in this context.
            }
            BBSideOperation.INCIDENT_VERIFIED_BY_USER -> {
                //The user has confirmed that the incident is accurate, therefore you may transmit the corresponding events to analytics, if needed. There is no requirement to invoke any functions from either party in this context, as the engine on the side will handle the task automatically.
            }
            else -> {
                Log.e("No Events Find",":")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (bbSideEngine.isEngineStarted) {
            bbSideEngine.stopSideEngine()
        }
    }
}
