package com.flare.sdk.android;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.flare.sdk.android.bottomsheets.CustomUIBottomSheet;
import com.flare.sdk.android.bottomsheets.SelectActivityBottomSheet;
import com.flare.sdk.android.databinding.ActivityThemeBinding;
import com.flare.sdk.android.interfaces.OnBottomSheetDismissListener;
import com.flaresafety.sideengine.BBSideEngine;
import com.flaresafety.sideengine.Constants;
import com.flaresafety.sideengine.SurveyTypeCallback;
import com.flaresafety.sideengine.rxjavaretrofit.network.model.BBSideEngineListener;
import com.flaresafety.sideengine.utils.Common;
import com.flaresafety.sideengine.utils.ContactClass;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;


public class CustomThemeActivity extends AppCompatActivity implements BBSideEngineListener,
        OnBottomSheetDismissListener {
    BBSideEngine bbSideEngine;
    boolean checkConfiguration = false;
    String mConfidence = null;
    boolean isResumeActivity = false;

    Intent intent;
    private ActivityThemeBinding viewBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewBinding = ActivityThemeBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());

        init();
        setListener();
    }

    public void init() {

        viewBinding.tvThemeName.setText(getString(R.string.custom_theme));
        setupEngine();
    }

    public void setupEngine() {

        intent = getIntent();
        String mode = intent.getStringExtra("mode");

        // "Your production license key here" or "Your sandbox license key here"
        String lic = intent.getStringExtra("lic");
        String region = intent.getStringExtra("region");
        String secretKey = intent.getStringExtra("secretKey");

        bbSideEngine = BBSideEngine.getInstance();
        bbSideEngine.showLogs(true);
        bbSideEngine.setBBSideEngineListener(this);
//      bbSideEngine.setEnableFlareAwareNetwork(true); //enableFlareAwareNetwork is a safety for cyclist to send notification for near by fleet users
//      bbSideEngine.setDistanceFilterMeters(20); //You can switch distance filter to publish location in the live tracking url, this should be send location every 20 meters when timer intervals is reached.
//      bbSideEngine.setLowFrequencyIntervalsSeconds(15);//Default is 15 sec, you can update this for your requirements, this will be used only when "high_frequency_mode_enabled" = false
//      bbSideEngine.setHighFrequencyIntervalsSeconds(3); //Default is 3 seconds, you can update this for your requirements, this will be used only when "high_frequency_mode_enabled" = true
//      bbSideEngine.setHighFrequencyModeEnabled(false); //Recommendation to enable high frequency mode when SOS is active, this will help us to batter live tracking experience.
//      bbSideEngine.setLocationNotificationTitle("Protection is active")
        bbSideEngine.setHazardFeatureEnabled(false); //The default hazard feature is enabled ( default value is true ), which can be adjusted to meet specific requirements. You can turn off by passing bbSideEngine.setHazardFeatureEnabled(false).

        bbSideEngine.setStickyEnable(true);
        bbSideEngine.activateIncidentTestMode(true); //This is only used in sandbox mode and is TRUE by default. This is why you should test your workflow in sandbox mode. You can change it to FALSE if you want to experience real-life incident detection

        bbSideEngine.configure(
                this,
                lic,
                secretKey,
                mode,
                Constants.BBTheme.STANDARD,
                region
        );

        //Custom Notification
        //        bbSideEngine.setNotificationMainBackgroundColor(R.color.white)
        //        bbSideEngine.setNotificationMainIcon(R.drawable.ic_launcher)
        //        bbSideEngine.setLocationNotificationTitle("Notification Title")
        //        bbSideEngine.setNotificationDescText("Notification Description")

    }

    public void setListener() {

        viewBinding.ivCloseMain.setOnClickListener(
                v -> finish()
        );

        viewBinding.btnPauseResume.setOnClickListener(v -> {
            if (bbSideEngine.isEngineStarted()) {
                if (isResumeActivity) {
                    viewBinding.btnPauseResume.setText(getString(R.string.pause));
                    bbSideEngine.resumeSideEngine();
                } else {
                    viewBinding.btnPauseResume.setText(getString(R.string.resume));
                    bbSideEngine.pauseSideEngine();
                }
            }
        });

        viewBinding.btnStart.setOnClickListener(v -> {

            if (checkConfiguration) {
                bbSideEngine.setUserEmail(viewBinding.etvUserEmail.getText().toString().trim());
                bbSideEngine.setUserName(viewBinding.etvUserName.getText().toString().trim());

                if (bbSideEngine.isEngineStarted()) {
                    bbSideEngine.setUserName(viewBinding.etvUserName.getText().toString().trim());
                    bbSideEngine.stopSideEngine();
                } else {
                    showActivityBottomSheet();
                }

                viewBinding.tvConfidence.setText("");
                if (bbSideEngine.isEngineStarted()) {
                    viewBinding.btnStart.setText(getString(R.string.stop));
                } else {
                    viewBinding.btnStart.setText(getString(R.string.start));
                }
            }
        });

    }

    private void showActivityBottomSheet() {
        SelectActivityBottomSheet selectActivityBottomSheet = new SelectActivityBottomSheet();
        selectActivityBottomSheet.setCancelable(true);
        selectActivityBottomSheet.show(getSupportFragmentManager(), selectActivityBottomSheet.getTag());
    }

    private void showIncidentBottomSheet() {
        CustomUIBottomSheet customUIBottomSheet = new CustomUIBottomSheet();
        customUIBottomSheet.setCancelable(true);
        customUIBottomSheet.show(getSupportFragmentManager(), customUIBottomSheet.getTag());
    }

    private void sendEmail() {
        if (viewBinding.etvUserEmail.getText().toString().equals("")) {
            return;
        }
        bbSideEngine.sendEmail(viewBinding.etvUserEmail.getText().toString());
    }

    private void sendSMS() {
        if (viewBinding.etvCountryCode.getText().toString().equals("") ||
                viewBinding.etvUserName.getText().toString().equals("") ||
                viewBinding.etvMobileNumber.getText().toString().equals("")) {
            return;
        }

        ContactClass contact = new ContactClass();
        contact.setCountryCode(viewBinding.etvCountryCode.getText().toString());
        contact.setPhoneNumber(viewBinding.etvMobileNumber.getText().toString());
        contact.setUserName(viewBinding.etvUserName.getText().toString());
        bbSideEngine.sendSMS(contact);
    }

    @Override
    public void onSideEngineCallback(
            boolean status,
            Constants.BBSideOperation type,
            JSONObject response
    ) {

        switch (type) {
            case CONFIGURE:
                // if status = true Now you can ready to start Side engine process
                checkConfiguration = status;
                viewBinding.progressBar.setVisibility(View.GONE);

                break;
            case START:
                //*Please update your user interface accordingly once the lateral engine has been initiated (for instance, modify the colour or text of the START button) to reflect the change in state.*//
                viewBinding.tvConfidence.setText("");
                if (bbSideEngine.isEngineStarted()) {
                    ForegroundService.startService(this, "Flare SDK Sample");
                    viewBinding.btnStart.setText(getString(R.string.stop));
                    viewBinding.btnPauseResume.setVisibility(View.VISIBLE);
                    viewBinding.btnPauseResume.setText(getString(R.string.pause));
                } else {
                    viewBinding.btnStart.setText(getString(R.string.start));
                    ForegroundService.stopService(this);
                    viewBinding.btnPauseResume.setVisibility(View.GONE);
                }
            case STOP:

                if (bbSideEngine.isEngineStarted()) {
                    viewBinding.btnStart.setText(getString(R.string.stop));
                } else {
                    ForegroundService.stopService(this);
                    isResumeActivity = false;
                    viewBinding.btnStart.setText(getString(R.string.start));
                    viewBinding.btnPauseResume.setVisibility(View.GONE);
                }
                //Please update your user interface accordingly once the lateral engine has been initiated (for instance, modify the colour or text of the START button) to reflect the change in state.
                break;

            case INCIDENT_DETECTED:

                Log.w("CustomThemeActivity", "INCIDENT_DETECTED");
                setNotification();

                //TODO: Set user id
//                bbSideEngine.setUserId(getRandomNumberString());

                //TODO: Set rider name
//                bbSideEngine.setRiderName(viewBinding.etvUserName.getText().toString().trim());

                if (status) {
                    try {
                        boolean mCustomTheme = response.getBoolean("customTheme");
                        //Return incident status and confidence level, you can fetch confidence using the below code:
                        mConfidence = response.getString("confidence");
                        if (!mConfidence.equals("")) {
                            viewBinding.tvConfidence.setVisibility(View.VISIBLE);
                            try {
                                viewBinding.tvConfidence.setText("Confidence: " + mConfidence);
                            } catch (Exception e) {
                                Log.e("Exception: ", e.getMessage());
                            }
                        }

                        //TODO: If SDK is configured custom UI to open your screen here (MAKE SURE CONFIGURE SDK SELECTED CUSTOM THEME)
                        if (mCustomTheme) {
                            if (Common.getInstance().isAppInBackground()) {
                                BBSideEngine.getInstance().resumeSideEngine();
                            } else {
                                showIncidentBottomSheet();
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

            case INCIDENT_ALERT_SENT:
                //Return the alert sent (returns alert details (i.e. time, location, recipient, success/failure))
                break;

            case RESUME_SIDE_ENGINE:
                if (isResumeActivity) {
                    isResumeActivity = false;
                    viewBinding.btnPauseResume.setText(getString(R.string.pause));
                    ForegroundService.startService(this, "Flare SDK Sample");
                }
                // The side engine has been restarted, and we are currently monitoring the device's sensors and location in order to analyse another potential incident. There is no requirement to invoke any functions from either party in this context, as the engine on the side will handle the task automatically.
                break;
            case PAUSE_SIDE_ENGINE:
                isResumeActivity = true;
                viewBinding.btnPauseResume.setText(getString(R.string.resume));
                ForegroundService.stopService(this);
                // The side engine has been paused, and we are stop monitoring the device's sensors and location.

                break;

            case SMS:
                //This message is intended solely to provide notification regarding the transmission status of SMS. It is unnecessary to invoke any SIDE engine functions in this context.
                break;

            case EMAIL:
                //This message is intended solely to provide notification regarding the transmission status of Email. It is unnecessary to invoke any SIDE engine functions in this context.
                break;

            case POST_INCIDENT_FEEDBACK:
                // When a user gives feedback after receiving a post-incident notification, you will get an event here to identify the type of feedback provided.
                Log.w("CustomThemeActivity", "POST_INCIDENT_FEEDBACK");

                if (status) {
                    // User submitted report an incident
                    callSurveyVideoPage();
                } else {
                    // User is alright
                }

                try {
                    if (response != null && response.has("message")) {
                        String message = response.getString("message");
                        if (!message.isEmpty()) {
                            Log.w("POST_INCIDENT_FEEDBACK", message);
                        }
                    }
                } catch (Exception ignored) {

                }
                break;

            case TIMER_STARTED:
                //Countdown timer started after breach delay, this called only if you configured standard theme.
                break;
            case TIMER_FINISHED:
                //Countdown timer finished and jump to the incident summary page, this called only if you configured standard theme.
                break;

        }
    }

    private void callSurveyVideoPage() {
        BBSideEngine.getInstance().postIncidentSurvey(Constants.BBSurveyType.VIDEO, new SurveyTypeCallback<>() {
            @Override
            public void onEnd(String s) {
                BBSideEngine.getInstance().resumeSideEngine();
                finish();
                Common.getInstance().showToast("End called");
            }

            @Override
            public void onCancel() {
                BBSideEngine.getInstance().resumeSideEngine();
                finish();
                Common.getInstance().showToast("Cancel called");
            }
        });
    }

    private void setNotification() {

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Calendar calendar = Calendar.getInstance();
        long randomNumber = calendar.getTimeInMillis();
        String channelId = "12345";
        Intent intent = new Intent(this, CustomThemeActivity.class);
        Notification.Builder builder;
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE);

        NotificationChannel notificationChannel = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            notificationChannel = new NotificationChannel(channelId, "Incident Detected", NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setLightColor(Color.BLUE);
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);

            builder = new Notification.Builder(this, channelId)
                    .setContentTitle(this.getString(R.string.app_name))
                    .setContentText("Incident Detect")
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(),
                            R.mipmap.ic_launcher_round
                    ))
                    .setContentIntent(pendingIntent);

            notificationManager.notify((int) randomNumber, builder.build());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bbSideEngine.isEngineStarted()) {
            bbSideEngine.stopSideEngine();
        }
        ForegroundService.stopService(this);
    }

    @Override
    public void onReportAnIncident() {
        navigateToMap();
    }

    private void navigateToMap() {
        startActivity(new Intent(this, MapActivity.class));
    }

    @Override
    public void onActivitySelected(String activityType) {
        bbSideEngine.setRiderName(viewBinding.etvUserName.getText().toString().trim());
        bbSideEngine.startSideEngine(this, activityType);
    }
}