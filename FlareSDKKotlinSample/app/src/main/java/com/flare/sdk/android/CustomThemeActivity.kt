package com.flare.sdk.android

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.flare.sdk.android.bottomsheets.CustomUIBottomSheet
import com.flare.sdk.android.bottomsheets.SelectActivityBottomSheet
import com.flare.sdk.android.databinding.ActivityThemeBinding
import com.flare.sdk.android.interfaces.OnBottomSheetDismissListener
import com.flaresafety.sideengine.BBSideEngine
import com.flaresafety.sideengine.Constants
import com.flaresafety.sideengine.Constants.*
import com.flaresafety.sideengine.SurveyTypeCallback
import com.flaresafety.sideengine.rxjavaretrofit.network.model.BBSideEngineListener
import com.flaresafety.sideengine.utils.Common
import com.flaresafety.sideengine.utils.ContactClass
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class CustomThemeActivity : AppCompatActivity(), BBSideEngineListener,
    OnBottomSheetDismissListener {

    private val viewBinding: ActivityThemeBinding by lazy {
        ActivityThemeBinding.inflate(layoutInflater)
    }
    private lateinit var bbSideEngine: BBSideEngine
    private var mode: String? = ENVIRONMENT_PRODUCTION

    private var checkConfiguration = false
    private var mConfidence: String? = null
    private var isResumeActivity = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        init()
        setListener()
    }

    private fun init() {

        viewBinding.tvThemeName.text = getString(R.string.custom_theme)
        setupEngine();
    }

    private fun setupEngine() {

        val intent = intent
        mode = intent.getStringExtra("mode")

        //"Your production license key here" or "Your sandbox license key here"
        val lic = intent.getStringExtra("lic")
        val secretKey = intent.getStringExtra("secretKey")
        val region = intent.getStringExtra("region")

        bbSideEngine = BBSideEngine.getInstance()
        bbSideEngine.showLogs(true)
        bbSideEngine.setBBSideEngineListener(this)
//      bbSideEngine.setEnableFlareAwareNetwork(true) //enableFlareAwareNetwork is a safety for cyclist to send notification for near by fleet users
//      bbSideEngine.setDistanceFilterMeters(20) //You can switch distance filter to publish location in the live tracking url, this should be send location every 20 meters when timer intervals is reached.
//      bbSideEngine.setLowFrequencyIntervalsSeconds(15) //Default is 15 sec, you can update this for your requirements, this will be used only when "high_frequency_mode_enabled" = false
//      bbSideEngine.setHighFrequencyIntervalsSeconds(3) //Default is 3 seconds, you can update this for your requirements, this will be used only when "high_frequency_mode_enabled" = true
//      bbSideEngine.setHighFrequencyModeEnabled(false) //Recommendation to enable high frequency mode when SOS is active, this will help us to batter live tracking experience.
//      bbSideEngine.setLocationNotificationTitle("Protection is active")
        bbSideEngine.setHazardFeatureEnabled(false) //The default hazard feature is enabled ( default value is true ), which can be adjusted to meet specific requirements. You can turn off by passing bbSideEngine.setHazardFeatureEnabled(false).

        bbSideEngine.setStickyEnable(false)
        bbSideEngine.activateIncidentTestMode(true) //This is only used in sandbox mode and is TRUE by default. This is why you should test your workflow in sandbox mode. You can change it to FALSE if you want to experience real-life incident detection


        bbSideEngine.configure(
            this,
            lic,
            secretKey,
            mode,
            BBTheme.CUSTOM,
            region
        )

        //  Custom Notification
        //  bbSideEngine.setNotificationMainBackgroundColor(R.color.white)
        //  bbSideEngine.setNotificationMainIcon(R.drawable.ic_launcher)
        //  bbSideEngine.setLocationNotificationTitle("Notification Title")
        //  bbSideEngine.setNotificationDescText("Notification Description")

    }

    @SuppressLint("InflateParams")
    private fun setListener() {

        viewBinding.ivCloseMain.setOnClickListener {
            finish()
        }

        viewBinding.btnPauseResume.setOnClickListener {
            if (bbSideEngine.isEngineStarted) {
                if (isResumeActivity) {
                    viewBinding.btnPauseResume.text = getString(R.string.pause)
                    bbSideEngine.resumeSideEngine()
                } else {
                    viewBinding.btnPauseResume.text = getString(R.string.resume)
                    bbSideEngine.pauseSideEngine()
                }
            }
        }

        viewBinding.btnStart.setOnClickListener {

            if (checkConfiguration) {
                bbSideEngine.setUserEmail(viewBinding.etvUserEmail.text.toString().trim())
                bbSideEngine.setUserName(viewBinding.etvUserName.text.toString().trim())

                if (bbSideEngine.isEngineStarted) {
                    bbSideEngine.setUserName(viewBinding.etvUserName.text.toString().trim())
                    bbSideEngine.stopSideEngine()
                } else {
                    showActivityBottomSheet()
                }
                viewBinding.mConfidence.text = ""
                if (bbSideEngine.isEngineStarted) {
                    viewBinding.btnStart.text = getString(R.string.stop)
                } else {
                    viewBinding.btnStart.text = getString(R.string.start)
                }
            }
        }


    }

    private fun showActivityBottomSheet() {
        val selectActivityBottomSheet = SelectActivityBottomSheet()
        selectActivityBottomSheet.isCancelable = true
        selectActivityBottomSheet.show(supportFragmentManager, selectActivityBottomSheet.tag)
    }

    private fun showIncidentBottomSheet() {
        val customUIBottomSheet = CustomUIBottomSheet()
        customUIBottomSheet.isCancelable = false
        customUIBottomSheet.show(supportFragmentManager, customUIBottomSheet.tag)
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
            viewBinding.etvMobileNumber.text.toString() == ""
        ) {
            return
        }

        val contact = ContactClass()
        contact.countryCode = viewBinding.etvCountryCode.text.toString()
        contact.phoneNumber = viewBinding.etvMobileNumber.text.toString()
        contact.userName = viewBinding.etvUserName.text.toString()
        bbSideEngine.sendSMS(contact)
    }


    @SuppressLint("SetTextI18n")
    override fun onSideEngineCallback(
        status: Boolean,
        type: BBSideOperation?,
        response: JSONObject?
    ) {
        when (type) {
            BBSideOperation.CONFIGURE -> {
                //You are now able to initiate the SIDE engine process at any time. In the event that there is no user input button available to commence the activity, you may commence the SIDE engine by executing the following command:
                checkConfiguration = status
                viewBinding.progressBar.visibility = View.GONE
            }

            BBSideOperation.START -> {
                if (bbSideEngine.isEngineStarted) {
                    ForegroundService.startService(this, "Flare SDK Sample")
                    viewBinding.btnStart.text = getString(R.string.stop)
                    viewBinding.btnPauseResume.visibility = View.VISIBLE
                    viewBinding.btnPauseResume.text = getString(R.string.pause)
                } else {
                    viewBinding.btnStart.text = getString(R.string.start)
                    ForegroundService.stopService(this)
                    viewBinding.btnPauseResume.visibility = View.GONE
                }
                //Please update your user interface accordingly once the lateral engine has been initiated (for instance, modify the colour or text of the START button) to reflect the change in state.
            }

            BBSideOperation.STOP -> {
                if (bbSideEngine.isEngineStarted) {
                    viewBinding.btnStart.text = getString(R.string.stop)
                } else {
                    ForegroundService.stopService(this)
                    viewBinding.btnStart.text = getString(R.string.start)
                    isResumeActivity = false
                    viewBinding.btnPauseResume.visibility = View.GONE
                }
                //Please update the user interface (UI) in this section to reflect the cessation of the side engine (e.g., amend the colour or text of the STOP button accordingly).
            }

            BBSideOperation.INCIDENT_DETECTED -> {

                Log.w("CustomThemeActivity", "INCIDENT_DETECTED")
                setNotification()

                //TODO: Set user id
//                bbSideEngine.setUserId(getRandomNumberString())
                //TODO: Set rider name
//                bbSideEngine.setRiderName(viewBinding.etvUserName.text.toString().trim())
                if (status) {
                    try {
                        val mCustomTheme = response!!.getBoolean("customTheme")
                        mConfidence = response.getString("confidence")
                        if (!mConfidence.equals("")) {
                            viewBinding.mConfidence.visibility = View.VISIBLE
                            try {
                                viewBinding.mConfidence.text = "Confidence: $mConfidence"
                            } catch (ignored: Exception) {
                            }
                        }

                        //TODO: If SDK is configured custom UI to open your screen here (MAKE SURE CONFIGURE SDK SELECTED CUSTOM THEME)
                        if (mCustomTheme) {

                            if (Common.getInstance().isAppInBackground) {
                                BBSideEngine.getInstance().resumeSideEngine()
                            } else {
                                showIncidentBottomSheet()
                            }
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
            }

            BBSideOperation.INCIDENT_AUTO_CANCEL -> {
                //User canceled countdown countdown to get event here, this called only for if you configured standard theme.
            }

            BBSideOperation.INCIDENT_ALERT_SENT -> {
                //Return the alert sent (returns alert details (i.e. time, location, recipient, success/failure))
            }

            BBSideOperation.RESUME_SIDE_ENGINE -> {
                if (isResumeActivity) {
                    isResumeActivity = false
                    viewBinding.btnPauseResume.text = getString(R.string.pause)
                    ForegroundService.startService(this, "Flare SDK Sample")
                }
                // The side engine has been restarted, and we are currently monitoring the device's sensors and location in order to analyse another potential incident. There is no requirement to invoke any functions from either party in this context, as the engine on the side will handle the task automatically.
            }

            BBSideOperation.PAUSE_SIDE_ENGINE -> {
                isResumeActivity = true
                viewBinding.btnPauseResume.text = getString(R.string.resume)
                ForegroundService.stopService(this)

                // The side engine has been paused, and we are stop monitoring the device's sensors and location.
            }

            BBSideOperation.SMS -> {
                //This message is intended solely to provide notification regarding the transmission status of SMS. It is unnecessary to invoke any SIDE engine functions in this context.
            }

            BBSideOperation.EMAIL -> {
                //This message is intended solely to provide notification regarding the transmission status of Email. It is unnecessary to invoke any SIDE engine functions in this context.
            }

            BBSideOperation.POST_INCIDENT_FEEDBACK -> {

                // When a user gives feedback after receiving a post-incident notification, you will get an event here to identify the type of feedback provided.
                Log.w("CustomThemeActivity", "POST_INCIDENT_FEEDBACK")

                if (status) {
                    // User submitted report an incident
                    callSurveyVideoPage()
                } else {
                    // User is alright
                }

                if (response != null && response.has("message")) {
                    val message = response.getString("message")
                    if (message.isNotEmpty()) {
                        Log.w("POST_INCIDENT_FEEDBACK", message)
                    }
                }
            }
            BBSideOperation.TIMER_STARTED -> {
                //Countdown timer started after breach delay, this called only if you configured standard theme.

            }

            BBSideOperation.TIMER_FINISHED -> {
                //Countdown timer finished and jump to the incident summary page, this called only if you configured standard theme.

            }

            else -> {
                Log.e("No Events Find", ":")
            }
        }
    }

    private fun callSurveyVideoPage() {

        BBSideEngine.getInstance().postIncidentSurvey(BBSurveyType.VIDEO, object :

            SurveyTypeCallback<String> {

            override fun onEnd(surveyType: String?) {
                BBSideEngine.getInstance().resumeSideEngine()
                finish();
                Common.getInstance().showToast("End called")
            }

            override fun onCancel() {
                BBSideEngine.getInstance().resumeSideEngine()
                finish();
                Common.getInstance().showToast("Cancel called")
            }
        })
    }

    @SuppressLint("SuspiciousIndentation")
    private fun setNotification() {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val calendar = Calendar.getInstance()
        val randomNumber = calendar.timeInMillis
        val channelId = "12345"
        val intent = Intent(this, CustomThemeActivity::class.java)
        val builder: Notification.Builder?
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel =
                NotificationChannel(
                    channelId,
                    "Incident Detected",
                    NotificationManager.IMPORTANCE_HIGH
                )
            notificationChannel.lightColor = Color.BLUE
            notificationChannel.enableVibration(true)
            notificationManager.createNotificationChannel(notificationChannel)
            builder = Notification
                .Builder(this, channelId)
                .setContentTitle(this.getString(R.string.app_name))
                .setContentText("Incident Detected")
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setLargeIcon(
                    BitmapFactory.decodeResource(
                        this.resources,
                        R.mipmap.ic_launcher_round
                    )
                )
                .setContentIntent(pendingIntent)

            notificationManager.notify(randomNumber.toInt(), builder.build())
        } else {
            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val builderLower: NotificationCompat.Builder =
                NotificationCompat.Builder(this, channelId)
                    .setContentTitle(this.getString(R.string.app_name))
                    .setContentText("Incident Detected")
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                    .setSmallIcon(com.flaresafety.sideengine.R.drawable.ic_notification)
                    .setContentIntent(pendingIntent)
                    .setStyle(
                        NotificationCompat.BigTextStyle()
                            .setBigContentTitle(this.getString(R.string.app_name))
                            .bigText("Incident Detected")
                    )
            notificationManager.notify(randomNumber.toInt(), builderLower.build())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (bbSideEngine.isEngineStarted) {
            bbSideEngine.stopSideEngine()
        }
        ForegroundService.stopService(this)
    }

    private fun navigateToMap() {
        startActivity(Intent(this, MapActivity::class.java))
    }

    override fun onReportAnIncident() {
        navigateToMap()
    }

    override fun onActivitySelected(activityType: String) {
        bbSideEngine.setRiderName(viewBinding.etvUserName.text.toString().trim())
        bbSideEngine.startSideEngine(this, activityType)
    }
}
