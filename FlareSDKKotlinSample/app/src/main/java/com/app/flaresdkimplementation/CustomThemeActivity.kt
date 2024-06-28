package com.app.flaresdkimplementation

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
import com.app.flaresdkimplementation.bottomsheets.CustomUIBottomSheet
import com.app.flaresdkimplementation.bottomsheets.SelectActivityBottomSheet
import com.app.flaresdkimplementation.databinding.ActivityThemeBinding
import com.app.flaresdkimplementation.interfaces.OnBottomSheetDismissListener
import com.sos.busbysideengine.BBSideEngine
import com.sos.busbysideengine.Constants.*
import com.sos.busbysideengine.rxjavaretrofit.network.model.BBSideEngineListener
import com.sos.busbysideengine.utils.Common
import com.sos.busbysideengine.utils.ContactClass
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

    companion object {
        fun getRandomNumberString(): String {
            val rnd = Random()
            val number: Int = rnd.nextInt(999999)
            return String.format(Locale.ENGLISH, "%06d", number)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)
        val intent = intent
        mode = intent.getStringExtra("mode")
        viewBinding.tvThemeName.text = getString(R.string.custom_theme)

        bbSideEngine = BBSideEngine.getInstance()
        bbSideEngine.showLogs(true)
        bbSideEngine.setBBSideEngineListener(this)
//      bbSideEngine.setEnableFlareAwareNetwork(true) //enableFlareAwareNetwork is a safety for cyclist to send notification for near by fleet users
//      bbSideEngine.setDistanceFilterMeters(20) //You can switch distance filter to publish location in the live tracking url, this should be send location every 20 meters when timer intervals is reached.
//      bbSideEngine.setLowFrequencyIntervalsSeconds(15) //Default is 15 sec, you can update this for your requirements, this will be used only when "high_frequency_mode_enabled" = false
//      bbSideEngine.setHighFrequencyIntervalsSeconds(3) //Default is 3 seconds, you can update this for your requirements, this will be used only when "high_frequency_mode_enabled" = true
//      bbSideEngine.setHighFrequencyModeEnabled(false) //Recommendation to enable high frequency mode when SOS is active, this will help us to batter live tracking experience.
//      bbSideEngine.setLocationNotificationTitle("Protection is active")

        bbSideEngine.setStickyEnable(false)
        bbSideEngine.activateIncidentTestMode(true) //This is only used in sandbox mode and is TRUE by default. This is why you should test your workflow in sandbox mode. You can change it to FALSE if you want to experience real-life incident detection
//      bbSideEngine.setAppName("Flare SDK Sample")

        //"Your production license key here" or "Your sandbox license key here"
        val lic = intent.getStringExtra("lic")
        val secretKey = intent.getStringExtra("secretKey")

        bbSideEngine.configure(
            this,
            lic,
            secretKey,
            mode,
            BBTheme.CUSTOM,
            "GB"
        )

        //  Custom Notification
        //  bbSideEngine.setNotificationMainBackgroundColor(R.color.white)
        //  bbSideEngine.setNotificationMainIcon(R.drawable.ic_launcher)
        //  bbSideEngine.setLocationNotificationTitle("Notification Title")
        //  bbSideEngine.setNotificationDescText("Notification Description")

        setListener()
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
//            BBSideEngine.getInstance().launchIncidentClassification(this, object :
//                IncidentTypeCallback<String> {
//                override fun onSubmit(incidentType: String) {
//                    Log.d("on Submit>>>", incidentType)
//                }
//
//                override fun onClose() {
//                    Log.d("on onClose>>>", "called")
//                }
//            })
//            return@setOnClickListener
//            val notificationHelper = NotificationHelper(this)
//            val _builder = notificationHelper.setNotification("test", mode)
//            if (_builder != null) {
//                notificationHelper.manager.notify(101, _builder.build())
//            }
//            return@setOnClickListener
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
                //Ignore your personalized countdown page for now and avoid using any features from external engines. The external engine will automatically take care of any required tasks.
            }

            BBSideOperation.INCIDENT_ALERT_SENT -> {
                //This message is intended solely to provide notification regarding the transmission status of alerts. It is unnecessary to invoke any SIDE engine functions in this context.
            }

            BBSideOperation.RESUME_SIDE_ENGINE -> {
                if (isResumeActivity) {
                    isResumeActivity = false
                    viewBinding.btnPauseResume.text = getString(R.string.pause)
                    ForegroundService.startService(this, "Flare SDK Sample")
                }
                //The lateral engine has been restarted, and we are currently monitoring the device's sensors and location in order to analyse another potential incident. There is no requirement to invoke any functions from either party in this context, as the engine on the side will handle the task automatically.
            }

            BBSideOperation.PAUSE_SIDE_ENGINE -> {
                isResumeActivity = true
                viewBinding.btnPauseResume.text = getString(R.string.resume)
                ForegroundService.stopService(this)
                //The lateral engine has been restarted, and we are currently monitoring the device's sensors and location in order to analyse another potential incident. There is no requirement to invoke any functions from either party in this context, as the engine on the side will handle the task automatically.
            }

            BBSideOperation.SMS -> {
                //This message is intended solely to provide notification regarding the transmission status of SMS. It is unnecessary to invoke any SIDE engine functions in this context.
            }

            BBSideOperation.EMAIL -> {
                //This message is intended solely to provide notification regarding the transmission status of Email. It is unnecessary to invoke any SIDE engine functions in this context.
            }

            else -> {
                Log.e("No Events Find", ":")
            }
        }
    }

    @SuppressLint("SuspiciousIndentation")
    private fun setNotification() {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as
                NotificationManager
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
                    .setSmallIcon(com.sos.busbysideengine.R.drawable.ic_notification)
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
