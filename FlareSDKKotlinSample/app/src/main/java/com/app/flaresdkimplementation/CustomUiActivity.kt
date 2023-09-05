package com.app.flaresdkimplementation

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Vibrator
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.sos.busbysideengine.BBSideEngine
import com.sos.busbysideengine.location.PreferencesHelper
import com.sos.busbysideengine.rxjavaretrofit.network.model.BBSideEngineUIListener
import com.sos.busbysideengine.utils.Common
import com.sos.busbysideengine.utils.ContactClass
import org.json.JSONObject
import java.util.*

class CustomUiActivity : AppCompatActivity(), BBSideEngineUIListener {

    var tvCUISeconds: TextView? = null
    private var tvCUIFallDetected: TextView? = null
    var tvCUILatLong: TextView? = null
    private var tvCUIWord: TextView? = null
    private var countDownTimer: CountDownTimer? = null
    private var rlCUIMainBg: RelativeLayout? = null
    private var userName: String? = ""
    private var email: String? = ""
    private var mobileNo: String? = ""
    private var countryCode: String? = ""
    private var word: String? = ""
    private var mapUri: String? = ""
    var btnTestClicked = false
    private var ivCUIClose: ImageView? = null

    private var isIncidentCanceled = true
    private var isSurvey = false

    var rlCUIAlertView: RelativeLayout? = null
    var rlCUIIncidentView:RelativeLayout? = null
    var latitude = 0.0
    var longitude:Double = 0.0
    private var vibrator: Vibrator? = null
    var preferencesHelper: PreferencesHelper? = null

    var common: Common? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.custom_ui)
        common = Common.getInstance()
        preferencesHelper = PreferencesHelper.getPreferencesHelper()
        init()
        setListener()
        setClick()
    }

    private fun init() {
        BBSideEngine.getInstance().setBBSideEngineListenerInLib(this)
        val intent = intent
        userName = intent.getStringExtra("userName")
        email = intent.getStringExtra("email")
        mobileNo = intent.getStringExtra("mobileNo")
        countryCode = intent.getStringExtra("countryCode")
        btnTestClicked = intent.getBooleanExtra("btnTestClicked", false)
        rlCUIAlertView = findViewById(R.id.rlCUIAlertView)
        rlCUIIncidentView = findViewById(R.id.rlCUIIncidentView)
        tvCUILatLong = findViewById(R.id.tvCUIlatlong)
        tvCUIWord = findViewById(R.id.tvCUIWord)
        ivCUIClose = findViewById(R.id.ivCUIClose)
        tvCUIFallDetected = findViewById(R.id.tvCUIFallDetected)
        tvCUISeconds = findViewById(R.id.tvCUISeconds)
        rlCUIMainBg = findViewById(R.id.rlCUIMainBg)
    }

    private fun setClick() {
        tvCUIWord?.setOnClickListener {
            if (mapUri != "") {
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse(mapUri)
                startActivity(i)
            }
        }
    }

    fun getRandomNumberString(): String {
        val rnd = Random()
        val number: Int = rnd.nextInt(999999)
        return String.format("%06d", number)
    }

    private fun sendEmail() {
        if (email == "") {
            return
        }
        BBSideEngine.getInstance().sendEmail(email)
    }

    private fun sendSMS() {
        if (countryCode == "" ||
            userName == "" ||
            mobileNo == "") {
            return
        }

        val contact = ContactClass()
        contact.countryCode = countryCode
        contact.phoneNumber = mobileNo
        contact.userName = userName
        BBSideEngine.getInstance().sendSMS(contact)
    }


    private fun setListener() {
        startVibrate()
        val time = common?.timerInterval
        countDownTimer = object : CountDownTimer((time?.times(1000))!!, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                tvCUISeconds!!.text = (millisUntilFinished / 1000).toString()
                //here you can have your logic to set text to edittext
            }

            override fun onFinish() {

                isIncidentCanceled = false
                stopVibrate()

                //TODO: Set user id
                BBSideEngine.getInstance().setUserId(getRandomNumberString())

                //TODO: Set rider name
                BBSideEngine.getInstance().setRiderName(userName)

                //TODO: call method for fetching W3W Location data
                BBSideEngine.getInstance().fetchWhat3WordLocation(this@CustomUiActivity)

                isSurvey = true
                rlCUIAlertView!!.visibility = View.VISIBLE
                rlCUIIncidentView?.visibility = View.GONE
                try {
                    val locationData = preferencesHelper?.getLocationData(this@CustomUiActivity)
                    if (locationData != null) {
                        latitude = locationData.latitude
                        longitude = locationData.longitude
                    }
                    setMap()
                    tvCUILatLong?.text = "Latitude: " + locationData!!.latitude + ' ' + "Longitude: " + locationData.longitude
                } catch (e: Exception) {
                    e.message
                }
            }
        }.start()

        ivCUIClose?.setOnClickListener { v ->
            if (countDownTimer != null) {
                countDownTimer!!.cancel()
            }
            stopVibrate()
            //inactive function
            feedbackWarning()
        }
    }

    private fun feedbackWarning() {
        val appName = "Flare SDK Sample"
        val builder = AlertDialog.Builder(this)
            .setTitle("Help $appName become smarter")
            .setMessage("$appName incident detection can be improved by learning from your incident. Was this an accurate alert?")
            .setCancelable(false)
            .setNegativeButton(
                "No"
            ) { dialog: DialogInterface, _: Int ->
                dialog.dismiss()
                BBSideEngine.getInstance().incidentDecline()
                completeConfirmation()
            }
            .setPositiveButton(
                "Yes"
            ) { _: DialogInterface?, _: Int ->

                //TODO: Send Email and SMS
                sendEmail()
                sendSMS()

                //TODO: notify to partner
                BBSideEngine.getInstance().notifyPartner()

                if(Common.getInstance().isAppInBackground) {
                    BBSideEngine.getInstance().resumeSensorIfAppInBackground();
                    finish();
                }else{
                    completeConfirmation()
                }

            }
        val alert = builder.create()
        alert.show()
    }
    private fun completeConfirmation(){
        if (!isSurvey ||
            (BBSideEngine.getInstance().surveyVideoURL() == null ||
                    BBSideEngine.getInstance().surveyVideoURL() == "")) {
            BBSideEngine.getInstance().resumeSideEngine();
            finish()
        }else{
            BBSideEngine.getInstance().startSurveyVideoActivity()
        }
    }
    private fun startVibrate() {
        val pattern = longArrayOf(0, 100, 1000, 1500, 2000, 2500, 3000)
        vibrator = this.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator?
        vibrator!!.vibrate(pattern, 0)
    }

    fun stopVibrate() {
        vibrator!!.cancel()
    }

    fun setMap() {
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.CUIMap) as SupportMapFragment
        mapFragment.getMapAsync { googleMap ->

            // Add a marker in Sydney and move the camera
            val currentLoc = LatLng(latitude, longitude)
            googleMap.setMinZoomPreference(10.0f)
            googleMap.setMaxZoomPreference(20.0f)
            googleMap.addMarker(
                MarkerOptions()
                    .position(currentLoc)
                    .title("Current Location")
            )
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(currentLoc))
            googleMap.animateCamera(CameraUpdateFactory.newLatLng(currentLoc))
        }
    }

    override fun onCloseSurveyVideoActivityCallback() {
        BBSideEngine.getInstance().resumeSideEngine()
        finish()
    }

    override fun onSendSMSCallback(status: Boolean, response: JSONObject?) {

    }

    override fun onSendEmailCallback(status: Boolean, response: JSONObject?) {

    }

    override fun onIncidentCancelCallback(status: Boolean, response: JSONObject?) {

    }

    override fun onIncidentAutoCancelCallback(status: Boolean, response: JSONObject?) {
        if (countDownTimer != null) {
            countDownTimer!!.cancel()
        }
        stopVibrate()
        BBSideEngine.getInstance().resumeSideEngine()
        finish()
    }

    override fun onIncidentVerifiedByUser(status: Boolean, response: JSONObject?) {

    }

    override fun onIncidentAlertCallback(status: Boolean, response: JSONObject?) {
        if (response!= null){
            val mJSONObjectResult = response.getJSONObject("result")
            if (mJSONObjectResult.has("words")) {
                word = mJSONObjectResult.getString("words")
            }
            if (mJSONObjectResult.has("map")) {
                mapUri = mJSONObjectResult.getString("map")
            }
            tvCUIWord?.text = "//$word"
            if (mJSONObjectResult.has("latitude")) {
                latitude = mJSONObjectResult.getDouble("latitude")
            }
            if (mJSONObjectResult.has("longitude")) {
                longitude = mJSONObjectResult.getDouble("longitude")
            }
            setMap()
            tvCUILatLong?.text = "Latitude: $latitude Longitude: $longitude"
        }
    }

}
