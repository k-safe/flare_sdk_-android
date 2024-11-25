package com.flare.sdk.android;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.flare.sdk.android.bottomsheets.SelectActivityBottomSheet;
import com.flare.sdk.android.databinding.ActivityThemeBinding;
import com.flare.sdk.android.interfaces.OnBottomSheetDismissListener;
import com.flaresafety.sideengine.BBSideEngine;
import com.flaresafety.sideengine.Constants;
import com.flaresafety.sideengine.rxjavaretrofit.network.model.BBSideEngineListener;
import com.flaresafety.sideengine.utils.ContactClass;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;


public class StandardThemeActivity extends AppCompatActivity implements BBSideEngineListener,
        OnBottomSheetDismissListener {

    BBSideEngine bbSideEngine;
    boolean checkConfiguration = false;
    boolean isResumeActivity = false;
    String mConfidence = "";

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

        viewBinding.tvThemeName.setText(getString(R.string.standard_theme));

        setupEngine();
    }

    public void setupEngine() {

        Intent intent = getIntent();
        String mode = intent.getStringExtra("mode");

        //"Your production license key here" or "Your sandbox license key here"
        String lic = intent.getStringExtra("lic");
        String secretKey = intent.getStringExtra("secretKey");
        String region = intent.getStringExtra("region");

        bbSideEngine = BBSideEngine.getInstance();
        bbSideEngine.showLogs(true);
        bbSideEngine.setBBSideEngineListener(this);

//        bbSideEngine.setEnableFlareAwareNetwork(true); //The "enableFlareAwareNetwork" feature is a safety measure designed for cyclists, which allows them to send notifications to nearby fleet users.
//        bbSideEngine.setDistanceFilterMeters(20); //It is possible to activate the distance filter in order to transmit location data in the live tracking URL. This will ensure that location updates are transmitted every 20 meters, once the timer interval has been reached.
//        bbSideEngine.setLowFrequencyIntervalsSeconds(15); //The default value is 15 seconds, which can be adjusted to meet specific requirements. This parameter will only be utilized in cases where bbSideEngine.setHighFrequencyModeEnabled(false) is invoked.
//        bbSideEngine.setHighFrequencyIntervalsSeconds(3); //The default value is 3 seconds, which can be adjusted to meet specific requirements. This parameter will only be utilized in cases where bbSideEngine.setHighFrequencyModeEnabled(true) is invoked.
//        bbSideEngine.setHighFrequencyModeEnabled(false); //It is recommended to activate the high frequency mode when the SOS function is engaged in order to enhance the quality of the live tracking experience.
//        bbSideEngine.setLocationNotificationTitle("Protection is active")

        bbSideEngine.setStickyEnable(true);
        bbSideEngine.activateIncidentTestMode(true); //This is only used in sandbox mode and is TRUE by default. This is why you should test your workflow in sandbox mode. You can change it to FALSE if you want to experience real-life incident detection
        bbSideEngine.setHazardFeatureEnabled(false); //The default hazard feature is enabled ( default value is true ), which can be adjusted to meet specific requirements. You can turn off by passing bbSideEngine.setHazardFeatureEnabled(false).

        bbSideEngine.configure(
                this,
                lic,
                secretKey,
                mode,
                Constants.BBTheme.STANDARD,
                region
        );

        //TODO: Customise the SideEngine theme(Optional).
        //bbSideEngine.setIncidentTimeInterval(45) = Default 30 seconds
        //bbSideEngine.setIncidentHeader("header");  //Only for standard theme
        //bbSideEngine.setIncidentInfoMessage("message"); //Only for standard theme
        //bbSideEngine.setIncidentPageHeaderColor("#ff0000"); //Only for standard theme
        //bbSideEngine.setIncidentPageBackgroundColor("#ff00ff"); //Only for standard theme
        //bbSideEngine.setIncidentPageHeaderMessageColor("#ffffff"); //Only for standard theme
        //bbSideEngine.setSwipeButtonBgColor(R.color.white) = Default "#ffffff" //Only for standard theme
        //bbSideEngine.setSwipeButtonTextSize(18) = Default 16 //Only for standard theme
        //bbSideEngine.setSwipeButtonText("Swipe to Cancel"); //Only for standard theme
        //bbSideEngine.setImpactBody("Detected a potential fall or impact involving"); //This message show in the SMS, email, webhook and slack body with rider name passed in this method (bbSideEngine.setRiderName("App user name here");) parameter

        //enableFlareAwareNetwork is a safety for cyclist to send notification for near by fleet users

    }

    public void setListener() {

        viewBinding.ivCloseMain.setOnClickListener(
                v -> finish()
        );

        viewBinding.btnPauseResume.setOnClickListener(v ->{
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
                    bbSideEngine.setUserCountryCode(viewBinding.etvCountryCode.getText().toString().trim());
                    bbSideEngine.setUserMobile(viewBinding.etvMobileNumber.getText().toString().trim());

                        if (bbSideEngine.isEngineStarted()) {
                            bbSideEngine.setUserName(viewBinding.etvUserName.getText().toString().trim());
                            bbSideEngine.stopSideEngine();
                        } else {
                            showActivityBottomSheet();
                        }

                }
        });
    }

    private void showActivityBottomSheet() {
        SelectActivityBottomSheet selectActivityBottomSheet = new SelectActivityBottomSheet();
        selectActivityBottomSheet.setCancelable(true);
        selectActivityBottomSheet.show(getSupportFragmentManager(), selectActivityBottomSheet.getTag());
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
        if (viewBinding.etvUserEmail.getText().toString().isEmpty()) {
            return;
        }
        bbSideEngine.sendEmail(viewBinding.etvUserEmail.getText().toString());
    }

    private void sendSMS() {
        if (viewBinding.etvCountryCode.getText().toString().isEmpty() ||
                viewBinding.etvUserName.getText().toString().isEmpty() ||
                viewBinding.etvMobileNumber.getText().toString().isEmpty()
        ) {
            return;
        }

        ContactClass contact = new ContactClass();
        contact.setCountryCode(viewBinding.etvCountryCode.getText().toString());
        contact.setPhoneNumber(viewBinding.etvMobileNumber.getText().toString());
        contact.setUserName(viewBinding.etvUserName.getText().toString());
        bbSideEngine.sendSMS(contact);
    }

    @Override
    public void onSideEngineCallback(boolean status, Constants.BBSideOperation type, JSONObject response) {

        Log.w("type", ": "+ type);
        Log.w("SIDE_ENGINE_SDK", type.ordinal() + " " + (response != null ? response : ""));
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
                    viewBinding.btnStart.setText(getString(R.string.stop));
                    viewBinding.btnPauseResume.setVisibility(View.VISIBLE);
                    viewBinding.btnPauseResume.setText(getString (R.string.pause));
                } else {
                    viewBinding.btnStart.setText(getString(R.string.start));
                    viewBinding.btnPauseResume.setVisibility(View.GONE);
                }
            case STOP:
                viewBinding.tvConfidence.setText("");
                if (bbSideEngine.isEngineStarted()) {
                    viewBinding.btnStart.setText(getString(R.string.stop));
                    viewBinding.btnPauseResume.setVisibility(View.VISIBLE);
                    viewBinding.btnPauseResume.setText(getString(R.string.pause));
                } else {
                    isResumeActivity = false;
                    viewBinding.btnStart.setText(getString(R.string.start));
                    viewBinding.btnPauseResume.setVisibility(View.GONE);
                }
                //Please update the user interface (UI) in this section to reflect the cessation of the side engine (e.g., amend the colour or text of the STOP button accordingly).

                break;
            case INCIDENT_DETECTED:
                //The user has identified an incident, and if necessary, it may be appropriate to log the incident in either the analytics system or an external database. Please refrain from invoking any side engine methods at this juncture.
                //Threshold reached and you will redirect to countdown page

                //TODO: Set user id
//                bbSideEngine.setUserId(getRandomNumberString());
                //TODO: Set rider name
                bbSideEngine.setRiderName(viewBinding.etvUserName.getText().toString().trim());

                if (status) {
                    try {
                        //Return incident status and confidence level, you can fetch confidence using the below code:
                        if (response != null) {
                            mConfidence = response.getString("confidence");
                        }
                        if (!mConfidence.equals("")) {
                            viewBinding.tvConfidence.setVisibility(View.VISIBLE);
                            try {
                                viewBinding.tvConfidence.setText(String.format("Confidence: %s", mConfidence));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case INCIDENT_CANCEL:
                //The incident has been canceled because of something the user did, so you can go ahead and register any analytics events if needed.
                break;
            case INCIDENT_ALERT_SENT:
                //This message is intended solely to provide notification regarding the transmission status of alerts. It is unnecessary to invoke any SIDE engine functions in this context.
                break;
            case RESUME_SIDE_ENGINE:
                if(isResumeActivity){
                    isResumeActivity = false;
                    viewBinding.btnPauseResume.setText(getString (R.string.pause));
                }
                //The lateral engine has been restarted, and we are currently monitoring the device's sensors and location in order to analyse another potential incident. There is no requirement to invoke any functions from either party in this context, as the engine on the side will handle the task automatically.
                break;
            case PAUSE_SIDE_ENGINE:
                isResumeActivity = true;
                viewBinding.btnPauseResume.setText(getString (R.string.resume));
                break;
            case TIMER_STARTED:
                //A 30-second countdown timer has started, and the SIDE engine is waiting for a response from the user or an automatic cancellation event. If no events are received within the 30-second intervals of the timer, the SIDE engine will log the incident on the dashboard.
                break;
            case INCIDENT_AUTO_CANCEL:
                //The incident has been automatically cancelled. If necessary, you may log the incident in the analytics system. Please refrain from invoking any side engine methods at this juncture.
                break;
            case TIMER_FINISHED:
                //After the 30-second timer ended, the SIDE engine began the process of registering the incident on the dashboard and sending notifications to emergency contacts.
                break;
            case SMS:
                //This message is intended solely to provide notification regarding the transmission status of SMS. It is unnecessary to invoke any SIDE engine functions in this context.
                break;
            case EMAIL:
                //This message is intended solely to provide notification regarding the transmission status of Email. It is unnecessary to invoke any SIDE engine functions in this context.
                break;
            case INCIDENT_VERIFIED_BY_USER:
                //The user has confirmed that the incident is accurate, therefore you may transmit the corresponding events to analytics, if needed. There is no requirement to invoke any functions from either party in this context, as the engine on the side will handle the task automatically.
                break;

            case POST_INCIDENT_FEEDBACK:

                // When a user gives feedback after receiving a post-incident notification, you will get an event here to identify the type of feedback provided.
                Log.w("StandardThemeActivity", "POST_INCIDENT_FEEDBACK");

                if (status) {
                    // User submitted report an incident
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
                } catch (Exception ignore) {

                }
                break;
            default:
                Log.e("No Events Find",":");
                break;
        }
    }

    @Override
    protected void onDestroy() {
        if (bbSideEngine != null) {
            bbSideEngine.stopSideEngine();
        }
        super.onDestroy();
    }


    @Override
    public void onReportAnIncident() {
        //TODO("Not yet implemented")
    }

    @Override
    public void onActivitySelected(String activityType) {
        bbSideEngine.startSideEngine(this, activityType);
    }
}