package com.app.flaresdkimplementation

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
import com.app.flaresdkimplementation.databinding.ActivityThemeBinding
import com.sos.busbysideengine.BBSideEngine
import com.sos.busbysideengine.Constants.BBSideOperation
import com.sos.busbysideengine.Constants.BBTheme
import com.sos.busbysideengine.Constants.ENVIRONMENT_PRODUCTION
import com.sos.busbysideengine.rxjavaretrofit.network.model.BBSideEngineListener
import com.sos.busbysideengine.utils.Common
import org.json.JSONException
import org.json.JSONObject
import java.util.Calendar
import java.util.Random

class CustomThemeActivity : AppCompatActivity(), BBSideEngineListener {

    private val viewBinding: ActivityThemeBinding by lazy {
        ActivityThemeBinding.inflate(layoutInflater)
    }

    private lateinit var bbSideEngine: BBSideEngine
    private var mode: String? = ENVIRONMENT_PRODUCTION

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
        viewBinding.tvThemeName.text = getString(R.string.custom_theme)

        bbSideEngine = BBSideEngine.getInstance(this)
        bbSideEngine.showLogs(true)
        bbSideEngine.setBBSideEngineListener(this)
        bbSideEngine.setEnableVRUUpdates(true) //enableVRUUpdates is a safety for cyclist to send notification for near by fleet users
        bbSideEngine.setDistanceFilterMeters(20) //You can switch distance filter to publish location in the live tracking url, this should be send location every 20 meters when timer intervals is reached.
        bbSideEngine.setLowFrequencyIntervalsSeconds(15) //Default is 15 sec, you can update this for your requirements, this will be used only when "high_frequency_mode_enabled" = false
        bbSideEngine.setHighFrequencyIntervalsSeconds(3) //Default is 3 seconds, you can update this for your requirements, this will be used only when "high_frequency_mode_enabled" = true
        bbSideEngine.setHighFrequencyModeEnabled(false) //Recommendation to enable high frequency mode when SOS is active, this will help us to batter live tracking experience.

        bbSideEngine.enableActivityTelemetry(true)
//        bbSideEngine.setLocationNotificationTitle("Protection is active")
        bbSideEngine.setStickyEnable(false)

        val lic = if (ENVIRONMENT_PRODUCTION.equals(mode))
            "Your production license key here" else "Your sandbox license key here"

        BBSideEngine.configure(this, lic, mode,
            BBTheme.CUSTOM
        )

        //Custom Notification
//        bbSideEngine.setNotificationMainBackgroundColor(R.color.white)
//        bbSideEngine.setNotificationMainIcon(R.drawable.ic_launcher)
//        bbSideEngine.setLocationNotificationTitle("Notification Title")
//        bbSideEngine.setNotificationDescText("Notification Description")

        setListener()
    }

    private fun setListener() {
        viewBinding.ivCloseMain.setOnClickListener{
            finish()
        }

        viewBinding.btnStart.setOnClickListener {
            bbSideEngine.setUserEmail(viewBinding.etvUserEmail.text.toString().trim())
            bbSideEngine.setUserName(viewBinding.etvUserName.text.toString().trim())

            if (bbSideEngine.isEngineStarted) {
                bbSideEngine.setUserName(viewBinding.etvUserName.text.toString().trim())
                bbSideEngine.stopSideEngine()
            } else {
                bbSideEngine.startSideEngine(this)
            }
            viewBinding.mConfidence.text =""
            if (bbSideEngine.isEngineStarted){
                viewBinding.btnStart.text = getString (R.string.stop)
            } else {
                viewBinding.btnStart.text =getString(R.string.start)
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
                //Threshold reached and you will redirect to countdown page
                Log.w("CustomThemeActivity", "INCIDENT_DETECTED")
                setNotification()

                //TODO: Set user id
                bbSideEngine.setUserId(getRandomNumberString())
                //TODO: Set rider name
                bbSideEngine.setRiderName(viewBinding.etvUserName.text.toString().trim())
                if (status) {
                    try {
                        val mCustomTheme = response!!.getBoolean("customTheme")
                        mConfidence = response.getString("confidence")
                        if (!mConfidence.equals("")) {
                            viewBinding.mConfidence.visibility = View.VISIBLE
                            try {
                                viewBinding.mConfidence.text = "Confidence: + $mConfidence"
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
                                BBSideEngine.getInstance(null).setRiderName(viewBinding.etvUserName.text.toString().trim())

                                //TODO: call method for fetching W3W Location data
                                BBSideEngine.getInstance(null).fetchWhat3WordLocation(this@CustomThemeActivity)

                                //TODO: Send Email
                                BBSideEngine.getInstance(null).sendEmail(viewBinding.etvUserEmail.text.toString().trim(),false) // Replace your emergency email address

                                //TODO: notify to partner
                                BBSideEngine.getInstance(null).notifyPartner()

                                BBSideEngine.getInstance(null).resumeSensorIfAppInBackground()

                            } else {
                                val intent = Intent(this, CustomUiActivity::class.java)
                                intent.putExtra("userName", viewBinding.etvUserName.text.toString().trim())
                                intent.putExtra("email", viewBinding.etvUserEmail.text.toString().trim())
                                intent.putExtra("btnTestClicked", !ENVIRONMENT_PRODUCTION.equals(mode))
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                startActivity(intent)
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
            BBSideOperation.INCIDENT_AUTO_CANCEL -> {
                //Auto canceled countdown countdown to get event here, this called only for if you configured standard theme.
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
                //Countdown timer finished and jump to the incident summary page, this called only if you configured standard theme.
            }
            else -> {
                Log.e("No Events Find",":")
            }
        }
    }

    private fun setNotification(){
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as
                NotificationManager
        val calendar = Calendar.getInstance()
        val randomNumber = calendar.timeInMillis
        val channelId = "12345"
        val intent = Intent(this, CustomThemeActivity::class.java)
        var builder: Notification.Builder? = null
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel =
                NotificationChannel(channelId, "Incident Detected", NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.lightColor = Color.BLUE
            notificationChannel.enableVibration(true)
            notificationManager.createNotificationChannel(notificationChannel)
            builder = Notification
                .Builder(this, channelId)
                .setContentTitle(this.getString(R.string.app_name))
                .setContentText("Incident Detect")
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setLargeIcon(BitmapFactory.decodeResource(this.resources, R.mipmap.ic_launcher_round))
                .setContentIntent(pendingIntent)

            notificationManager.notify(randomNumber.toInt(), builder.build())
        } else {
            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val builderLower: NotificationCompat.Builder = NotificationCompat.Builder(this,channelId)
                .setContentTitle(this.getString(R.string.app_name))
                .setContentText("Incident Detect")
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setSmallIcon(com.sos.busbysideengine.R.drawable.ic_notification)
                .setContentIntent(pendingIntent)
                .setStyle(
                    NotificationCompat.BigTextStyle().setBigContentTitle(this.getString(R.string.app_name)).bigText("Incident Detect")
                )
            notificationManager.notify(randomNumber.toInt(), builderLower.build())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        bbSideEngine.stopSideEngine()
    }
}
