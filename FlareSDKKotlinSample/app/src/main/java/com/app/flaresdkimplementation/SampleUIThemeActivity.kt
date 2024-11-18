package com.app.flaresdkimplementation

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.app.flaresdkimplementation.bottomsheets.CustomUIBottomSheet
import com.app.flaresdkimplementation.bottomsheets.SelectActivityBottomSheet
import com.app.flaresdkimplementation.databinding.SampleUiActivityThemeBinding
import com.app.flaresdkimplementation.interfaces.OnBottomSheetDismissListener
import com.flaresafety.sideengine.BBSideEngine
import com.flaresafety.sideengine.Constants.*
import com.flaresafety.sideengine.rxjavaretrofit.network.model.BBSideEngineListener
import com.flaresafety.sideengine.utils.Common
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class SampleUIThemeActivity : AppCompatActivity(), BBSideEngineListener,
    OnBottomSheetDismissListener {

    // Production Mode
    private var productionLicense = "4afb485e-a181-4ce7-98f6-38cfe1afc748"
    private var sandboxLicense = "b6dd8509-d50e-48cc-af9e-ce9dcd712132"
    private val secretKey = "0CIyHjdB7HFeW22di09r87Vmb6ibPN82vrsHQTF2"

    private var mode = ENVIRONMENT_PRODUCTION

    private lateinit var bbSideEngine: BBSideEngine
    private var lic = ""
    private var postNotificationCode = 1221

    private val viewBinding: SampleUiActivityThemeBinding by lazy {
        SampleUiActivityThemeBinding.inflate(layoutInflater)
    }

    private var checkConfiguration = false
    private var isResumeActivity = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        bbSideEngine = BBSideEngine.getInstance()
        bbSideEngine.showLogs(true)
        bbSideEngine.setBBSideEngineListener(this)

//      bbSideEngine.setEnableFlareAwareNetwork(true) //enableFlareAwareNetwork is a safety for cyclist to send notification for near by fleet users
//      bbSideEngine.setDistanceFilterMeters(20) //You can switch distance filter to publish location in the live tracking url, this should be send location every 20 meters when timer intervals is reached.
//      bbSideEngine.setLowFrequencyIntervalsSeconds(15) //Default is 15 sec, you can update this for your requirements, this will be used only when "high_frequency_mode_enabled" = false
//      bbSideEngine.setHighFrequencyIntervalsSeconds(3) //Default is 3 seconds, you can update this for your requirements, this will be used only when "high_frequency_mode_enabled" = true
//      bbSideEngine.setHighFrequencyModeEnabled(false) //Recommendation to enable high frequency mode when SOS is active, this will help us to batter live tracking experience.
//      bbSideEngine.setLocationNotificationTitle("Protection is active")

        bbSideEngine.setStickyEnable(true)
        bbSideEngine.activateIncidentTestMode(true) //This is only used in sandbox mode and is TRUE by default. This is why you should test your workflow in sandbox mode. You can change it to FALSE if you want to experience real-life incident detection
//      bbSideEngine.setAppName("Flare SDK Sample")

        configureSideEngine()

        //  Custom Notification
        //  bbSideEngine.setNotificationMainBackgroundColor(R.color.white)
        //  bbSideEngine.setNotificationMainIcon(R.drawable.ic_launcher)
        //  bbSideEngine.setLocationNotificationTitle("Notification Title")
        //  bbSideEngine.setNotificationDescText("Notification Description")

        setListener()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    postNotificationCode
                )
            }
        }

    }

    private fun configureSideEngine() {
        //"Your production license key here" or "Your sandbox license key here"
        lic = if(ENVIRONMENT_PRODUCTION.equals(mode)) productionLicense else sandboxLicense

        viewBinding.progressBar.visibility = View.VISIBLE

        bbSideEngine.configure(
            this,
            lic,
            secretKey,
            mode,
            BBTheme.CUSTOM,
            "GB"
        )
    }

    @SuppressLint("InflateParams")
    private fun setListener() {

        viewBinding.rgEnvironment.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.rbProduction) {
                // The switch is checked.
                viewBinding.rbProduction.text = getString(R.string.production_mode)
                mode = ENVIRONMENT_PRODUCTION


            } else {
                // The switch isn't checked.
                viewBinding.rbSandBox.text = getString(R.string.sandbox_mode)
                mode = ENVIRONMENT_SANDBOX


            }

            if (checkConfiguration) {
                if (bbSideEngine.isEngineStarted) {
                    bbSideEngine.stopSideEngine()
                }
            }

            configureSideEngine()

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

             if (bbSideEngine.isEngineStarted) {
                    bbSideEngine.stopSideEngine()
                } else {
                    showActivityBottomSheet()
                }

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
                //    viewBinding.btnPauseResume.visibility = View.VISIBLE
                //    viewBinding.btnPauseResume.text = getString(R.string.pause)
                } else {
                    viewBinding.btnStart.text = getString(R.string.start)
                    ForegroundService.stopService(this)
                   // viewBinding.btnPauseResume.visibility = View.GONE
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
                   // viewBinding.btnPauseResume.visibility = View.GONE
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
                     /*   mConfidence = response.getString("confidence")
                        if (!mConfidence.equals("")) {
                            viewBinding.mConfidence.visibility = View.VISIBLE
                            try {
                                viewBinding.mConfidence.text = "Confidence: $mConfidence"
                            } catch (ignored: Exception) {
                            }
                        }*/

                        // TODO: If SDK is configured custom UI to open your screen here (MAKE SURE CONFIGURE SDK SELECTED CUSTOM THEME)
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
                  //  viewBinding.btnPauseResume.text = getString(R.string.pause)
                    ForegroundService.startService(this, "Flare SDK Sample")
                }
                //The lateral engine has been restarted, and we are currently monitoring the device's sensors and location in order to analyse another potential incident. There is no requirement to invoke any functions from either party in this context, as the engine on the side will handle the task automatically.
            }

            BBSideOperation.PAUSE_SIDE_ENGINE -> {
                isResumeActivity = true
              //  viewBinding.btnPauseResume.text = getString(R.string.resume)
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
        val intent = Intent(this, SampleUIThemeActivity::class.java)
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
//        bbSideEngine.setRiderName(viewBinding.etvUserName.text.toString().trim())
        bbSideEngine.startSideEngine(this, activityType)
    }
}
