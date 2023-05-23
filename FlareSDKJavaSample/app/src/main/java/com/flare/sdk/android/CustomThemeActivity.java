package com.flare.sdk.android;

import static com.sos.busbysideengine.Constants.BBTheme.CUSTOM;
import static com.sos.busbysideengine.Constants.ENVIRONMENT_PRODUCTION;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.sos.busbysideengine.BBSideEngine;
import com.sos.busbysideengine.Constants;
import com.sos.busbysideengine.rxjavaretrofit.network.model.BBSideEngineListener;
import com.sos.busbysideengine.utils.Common;
import com.sos.busbysideengine.utils.ContactClass;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Random;


public class CustomThemeActivity extends AppCompatActivity implements BBSideEngineListener {
    BBSideEngine bbSideEngine;

    Button btnStart;
    EditText etvUserName, etvCountryCode, etvMobileNumber, etvUserEmail;

    TextView tvConfidence, tvThemeName;
    ImageView ivCloseMain;

    private ProgressBar progressBar;

    boolean checkConfiguration = false;
    String mConfidence;

    private String mode = ENVIRONMENT_PRODUCTION;

    Intent intent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme);
        init();
    }

    public void init() {
        intent = getIntent();
        mode = intent.getStringExtra("mode");

        tvThemeName = findViewById(R.id.tvThemeName);
        btnStart = findViewById(R.id.btnStart);

        etvCountryCode = findViewById(R.id.etvCountryCode);
        etvMobileNumber = findViewById(R.id.etvMobileNumber);
        etvUserName = findViewById(R.id.etvUserName);
        etvUserEmail = findViewById(R.id.etvUserEmail);

        ivCloseMain = findViewById(R.id.ivCloseMain);
        tvConfidence = findViewById(R.id.tvConfidence);

        progressBar = findViewById(R.id.progressBar);
        Log.d("mConfidence1", "" + mConfidence);

        tvThemeName.setText(getString(R.string.custom_theme));

        //Custom Notification
        //        bbSideEngine.setNotificationMainBackgroundColor(R.color.white)
        //        bbSideEngine.setNotificationMainIcon(R.drawable.ic_launcher)
        //        bbSideEngine.setLocationNotificationTitle("Notification Title")
        //        bbSideEngine.setNotificationDescText("Notification Description")

        setupEngine();
        setListener();
    }

    public void setupEngine() {

        //"Your production license key here" or "Your sandbox license key here"
        String lic = intent.getStringExtra("lic");

        BBSideEngine.configure(this,
                lic,
                mode,
                CUSTOM
        );

        bbSideEngine = BBSideEngine.getInstance(this);
        bbSideEngine.showLogs(true);
        bbSideEngine.setBBSideEngineListener(this);
//        bbSideEngine.setEnableFlareAwareNetwork(true); //enableFlareAwareNetwork is a safety for cyclist to send notification for near by fleet users
//        bbSideEngine.setDistanceFilterMeters(20); //You can switch distance filter to publish location in the live tracking url, this should be send location every 20 meters when timer intervals is reached.
//        bbSideEngine.setLowFrequencyIntervalsSeconds(15);//Default is 15 sec, you can update this for your requirements, this will be used only when "high_frequency_mode_enabled" = false
//        bbSideEngine.setHighFrequencyIntervalsSeconds(3); //Default is 3 seconds, you can update this for your requirements, this will be used only when "high_frequency_mode_enabled" = true
//        bbSideEngine.setHighFrequencyModeEnabled(false); //Recommendation to enable high frequency mode when SOS is active, this will help us to batter live tracking experience.

        bbSideEngine.enableActivityTelemetry(true);
//        bbSideEngine.setLocationNotificationTitle("Protection is active")
        bbSideEngine.setStickyEnable(false);
    }

    public void setListener() {
        btnStart.setOnClickListener(v -> {
            bbSideEngine.setUserEmail(etvUserEmail.getText().toString().trim());
            bbSideEngine.setUserName(etvUserName.getText().toString().trim());
            if (bbSideEngine.isEngineStarted()) {
                bbSideEngine.setUserName(etvUserName.getText().toString().trim());
                bbSideEngine.stopSideEngine();
            } else {
                bbSideEngine.startSideEngine(CustomThemeActivity.this);
            }

            tvConfidence.setText("");
            btnStart.setText(bbSideEngine.isEngineStarted() ? getString(R.string.stop) : getString(R.string.start));
        });

        ivCloseMain.setOnClickListener(
                v -> {
                   finish();
                }
        );

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (!checkConfiguration) {
            return;
        }

        if (requestCode == 0) {
            bbSideEngine.startSideEngine(this);
            btnStart.setText(bbSideEngine.isEngineStarted() ? getString(R.string.stop) : getString(R.string.start));
        } else if (requestCode == 1) {
            btnStart.setText(getString(R.string.start));
        }
    }

    public static String getRandomNumberString() {
        // It will generate 6 digit random Number.
        // from 0 to 999999
        Random rnd = new Random();
        int number = rnd.nextInt(999999);
        // this will convert any number sequence into 6 character.
        return String.format("%06d", number);
    }


    private void sendEmail() {
        if (etvUserEmail.getText().toString().equals("")) {
            return;
        }
        bbSideEngine.sendEmail(etvUserEmail.getText().toString());
    }

    private void sendSMS() {
        if (etvCountryCode.getText().toString().equals("") ||
                etvUserName.getText().toString().equals("") ||
                etvMobileNumber.getText().toString().equals("")) {
            return;
        }

        ContactClass contact = new ContactClass();
        contact.setCountryCode(etvCountryCode.getText().toString());
        contact.setPhoneNumber(etvMobileNumber.getText().toString());
        contact.setUserName(etvUserName.getText().toString());
        bbSideEngine.sendSMS(contact);
    }


    @Override
    public void onSideEngineCallback(boolean status, Constants.BBSideOperation type, JSONObject response) {

        Log.w("SIDE_ENGINE_SDK", type.ordinal() + " " + (response != null ? response : ""));
        switch (type) {
            case CONFIGURE:
                // if status = true Now you can ready to start Side engine process
                checkConfiguration = status;
                progressBar.setVisibility(View.GONE);

                break;
            case START:
                //Update your UI here (e.g. update START button color or text here when SIDE engine started)
                break;
            case STOP:
                //Update your UI here (e.g. update STOP button color or text here when SIDE engine started)
                break;
            case SMS:
                //Returns SMS delivery status and response payload
                Log.e("SMS: ", String.valueOf(response));
                break;
            case EMAIL:
                //Returns email delivery status and response payload
                Log.e("Email: ", String.valueOf(response));
                break;
            case INCIDENT_DETECTED:
                //Threshold reached and you will redirect to countdown page
                Log.w("CustomThemeActivity", "INCIDENT_DETECTED");
                setNotification();

                //TODO: Set user id
                bbSideEngine.setUserId(getRandomNumberString());

                //TODO: Set rider name
                bbSideEngine.setRiderName(etvUserName.getText().toString().trim());
                bbSideEngine.setRiderName(etvUserName.getText().toString().trim());

                if (status) {
                    try {
                        boolean mCustomTheme = response.getBoolean("customTheme");
                        //Return incident status and confidence level, you can fetch confidance using the below code:
                        mConfidence = response.getString("confidence");
                        if (!mConfidence.equals("")){
                            tvConfidence.setVisibility(View.VISIBLE);
                            try {
                                tvConfidence.setText("Confidence: "+ mConfidence);
                            }catch (Exception e){
                                Log.e("Exception: ", e.getMessage());
                            }
                        }
                        Log.e("", mCustomTheme + "");
                        Log.e("mConfidence", mConfidence + "");
                        //TODO: If SDK is configured custom UI to open your screen here (MAKE SURE CONFIGURE SDK SELECTED CUSTOM THEME)
                        if (mCustomTheme) {
//                            bbSideEngine.stopSideEngine();
                            if (Common.getInstance().isAppInBackground()) {

                                //TODO: Set user id
                                BBSideEngine.getInstance(null).setUserId(getRandomNumberString());

                                //TODO: Set rider name
                                BBSideEngine.getInstance(null).setRiderName(etvUserName.getText().toString().trim());

                                //TODO: call method for fetching W3W Location data
                                BBSideEngine.getInstance(null).fetchWhat3WordLocation(this);

                                //TODO: Send Email and SMS
                                sendSMS();
                                sendEmail();
//                                BBSideEngine.getInstance(null).sendEmail(viewBinding.etvUserEmail.text.toString().trim()) // Replace your emergency email address

                                //TODO: notify to partner
                                BBSideEngine.getInstance(null).notifyPartner();

                                BBSideEngine.getInstance(null).resumeSensorIfAppInBackground();

                            } else {

                                Intent intent = new Intent(CustomThemeActivity.this, CustomUiActivity.class);
                                intent.putExtra("userName", etvUserName.getText().toString().trim());
                                intent.putExtra("email", etvUserEmail.getText().toString().trim());
                                intent.putExtra("mobileNo", etvMobileNumber.getText().toString().trim());
                                intent.putExtra("countryCode", etvCountryCode.getText().toString().trim());
                                intent.putExtra("btnTestClicked", !ENVIRONMENT_PRODUCTION.equals(mode));
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case INCIDENT_CANCEL:
                //User canceled countdown countdown to get event here, this called only for if you configured standard theme.
                break;
            case TIMER_STARTED:
                //Countdown timer started after breach delay, this called only if you configured standard theme.
                break;
            case TIMER_FINISHED:
                //Countdown timer finished and jump to the incident summary page, this called only if you configured standard theme.
                break;
            case INCIDENT_ALERT_SENT:
                //Return the alert sent (returns alert details (i.e. time, location, recipient, success/failure))
                break;

        }
    }

    private void setNotification(){
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Calendar calendar = Calendar.getInstance();
        long randomNumber = calendar.getTimeInMillis();
        String channelId = "12345";
        Intent intent = new Intent(this, CustomThemeActivity.class);
        Notification.Builder builder;
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel =
                    new NotificationChannel(channelId, "Incident Detected", NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setLightColor(Color.BLUE);
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);
            builder = new Notification
                    .Builder(this, channelId)
                    .setContentTitle(this.getString(R.string.app_name))
                    .setContentText("Incident Detect")
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(),
                            R.mipmap.ic_launcher_round
                    ))
                    .setContentIntent(pendingIntent);

            notificationManager.notify((int) randomNumber, builder.build());
        } else {
            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder builderLower = new NotificationCompat.Builder(this,channelId)
                    .setContentTitle(this.getString(R.string.app_name))
                    .setContentText("Incident Detect")
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri)
                    .setSmallIcon(com.sos.busbysideengine.R.drawable.ic_notification)
                    .setContentIntent(pendingIntent)
                    .setStyle(new NotificationCompat.BigTextStyle().setBigContentTitle(this.getString(R.string.app_name)).bigText("Incident Detect")
                    );
            notificationManager.notify((int) randomNumber, builderLower.build());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bbSideEngine.stopSideEngine();
    }
}