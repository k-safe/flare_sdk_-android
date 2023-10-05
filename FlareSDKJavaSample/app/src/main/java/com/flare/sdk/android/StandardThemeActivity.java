package com.flare.sdk.android;

import static com.sos.busbysideengine.Constants.BBTheme.STANDARD;
import static com.sos.busbysideengine.Constants.ENVIRONMENT_PRODUCTION;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.sos.busbysideengine.BBSideEngine;
import com.sos.busbysideengine.Constants;
import com.sos.busbysideengine.rxjavaretrofit.network.model.BBSideEngineListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;


public class StandardThemeActivity extends AppCompatActivity implements BBSideEngineListener {

    private String mode = ENVIRONMENT_PRODUCTION;
    TextView tvConfidence, tvThemeName;
    ImageView ivCloseMain;
    Button btnStart, btnPauseResume;
    EditText etvUserName, etvCountryCode, etvMobileNumber, etvUserEmail;
    ProgressBar progressBar;

    BBSideEngine bbSideEngine;
    boolean btnTestClicked = false;
    boolean checkConfiguration = false;
    boolean isResumeActivity = false;

    String mConfidence = "";
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme);

        init();
        setListener();
    }

    public void init() {

        intent = getIntent();
        mode = intent.getStringExtra("mode");

        tvThemeName = findViewById(R.id.tvThemeName);
        btnStart = findViewById(R.id.btnStart);
        btnPauseResume = findViewById(R.id.btnPauseResume);

        etvCountryCode = findViewById(R.id.etvCountryCode);
        etvMobileNumber = findViewById(R.id.etvMobileNumber);
        etvUserName = findViewById(R.id.etvUserName);
        etvUserEmail = findViewById(R.id.etvUserEmail);

        ivCloseMain = findViewById(R.id.ivCloseMain);
        tvConfidence = findViewById(R.id.tvConfidence);

        progressBar = findViewById(R.id.progressBar);


        tvThemeName.setText(getString(R.string.standard_theme));


        setupEngine();
    }

    public void setupEngine() {

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
        bbSideEngine.activateIncidentTestMode(false); //This is only used in sandbox mode and is TRUE by default. This is why you should test your workflow in sandbox mode. You can change it to FALSE if you want to experience real-life incident detection
        bbSideEngine.setActivityType("Horse Riding");

        //"Your production license key here" or "Your sandbox license key here"
        String lic = intent.getStringExtra("lic");
        bbSideEngine.configure(this,
                lic,
                mode,
                STANDARD
        );

        //Custom Notification
        //        bbSideEngine.setLocationNotificationTitle("Protection is active");
        //        bbSideEngine.setNotificationMainBackgroundColor(R.color.white);
        //        bbSideEngine.setNotificationMainIcon(R.drawable.ic_launcher);
        //        bbSideEngine.setNotificationDescText("Notification Description");


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

        ivCloseMain.setOnClickListener(
                v -> finish()
        );
        btnPauseResume.setOnClickListener(v ->{
            if (bbSideEngine.isEngineStarted()) {
                if (isResumeActivity) {
                    btnPauseResume.setText(getString(R.string.pause));
                    bbSideEngine.resumeSideEngine();
                } else {
                    btnPauseResume.setText(getString(R.string.resume));
                    bbSideEngine.pauseSideEngine();
                }
            }
        });
        btnStart.setOnClickListener(v -> {
            if (checkConfiguration) {
                    bbSideEngine.setUserEmail(etvUserEmail.getText().toString().trim());
                    bbSideEngine.setUserName(etvUserName.getText().toString().trim());
                    bbSideEngine.setUserCountryCode(etvCountryCode.getText().toString().trim());
                    bbSideEngine.setUserMobile(etvMobileNumber.getText().toString().trim());

                    btnTestClicked = false;
                    if (bbSideEngine.isEngineStarted()) {
                        bbSideEngine.setUserName(etvUserName.getText().toString().trim());
                        bbSideEngine.stopSideEngine();
                    } else {
                        BottomSheetDialog dialog = new BottomSheetDialog(this);
                        View view = getLayoutInflater().inflate(R.layout.dialog_activity, null);
                        LinearLayout llBike = view.findViewById(R.id.llBike);
                        LinearLayout llScooter = view.findViewById(R.id.llScooter);
                        LinearLayout llCycling = view.findViewById(R.id.llCycling);
                        LinearLayout llCancel = view.findViewById(R.id.llCancel);

                        llBike.setOnClickListener(v13 -> {
                            bbSideEngine.setActivityType("Bike");
                            bbSideEngine.startSideEngine(StandardThemeActivity.this);
                            dialog.dismiss();
                        });

                        llScooter.setOnClickListener(v12 -> {
                            bbSideEngine.setActivityType("Scooter");
                            bbSideEngine.startSideEngine(StandardThemeActivity.this);
                            dialog.dismiss();
                        });

                        llCycling.setOnClickListener(v1 -> {
                            bbSideEngine.setActivityType("Cycling");
                            bbSideEngine.startSideEngine(StandardThemeActivity.this);
                            dialog.dismiss();
                        });

                        llCancel.setOnClickListener(v14 -> dialog.dismiss());

                        dialog.dismiss();
                        dialog.setCancelable(true);
                        dialog.setContentView(view);
                        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                        int screenHeight = displayMetrics.heightPixels;
                        dialog.getBehavior().setPeekHeight(screenHeight);
                        dialog.show();
                    }
                }
        });
    }

    public static String getRandomNumberString() {
        // It will generate 6 digit random Number.
        // from 0 to 999999
        Random rnd = new Random();
        int number = rnd.nextInt(999999);

        // this will convert any number sequence into 6 character.
        return String.format("%06d", number);
    }
    @Override
    public void onSideEngineCallback(boolean status, Constants.BBSideOperation type, JSONObject response) {

        Log.w("type", ": "+ type);
        Log.w("SIDE_ENGINE_SDK", type.ordinal() + " " + (response != null ? response : ""));
        switch (type) {
            case CONFIGURE:
                // if status = true Now you can ready to start Side engine process
                checkConfiguration = status;
                progressBar.setVisibility(View.GONE);
                break;
            case START:
                //*Please update your user interface accordingly once the lateral engine has been initiated (for instance, modify the colour or text of the START button) to reflect the change in state.*//
                tvConfidence.setText("");
                if (bbSideEngine.isEngineStarted()) {
                    btnStart.setText(getString(R.string.stop));
                    btnPauseResume.setVisibility(View.VISIBLE);
                    btnPauseResume.setText(getString (R.string.pause));
                } else {
                    btnStart.setText(getString(R.string.start));
                    btnPauseResume.setVisibility(View.GONE);
                }
            case STOP:
                //*Please update your user interface accordingly once the lateral engine has been initiated (for instance, modify the colour or text of the START button) to reflect the change in state.*//
                tvConfidence.setText("");
                if (bbSideEngine.isEngineStarted()) {
                    btnStart.setText(getString(R.string.stop));
                } else {
                    btnStart.setText(getString(R.string.start));
                    btnPauseResume.setVisibility(View.GONE);
                }
                break;
            case INCIDENT_DETECTED:
                //Threshold reached and you will redirect to countdown page
                Toast.makeText(this, "INCIDENT_DETECTED",Toast.LENGTH_LONG).show();
                //Threshold reached and you will redirect to countdown page
                //TODO: Set user id
                bbSideEngine.setUserId(getRandomNumberString());
                //TODO: Set rider name
                bbSideEngine.setRiderName(etvUserName.getText().toString().trim());

                if (status) {
                    try {
                        //Return incident status and confidence level, you can fetch confidence using the below code:
                        if (response != null) {
                            mConfidence = response.getString("confidence");
                        }
                        if (!mConfidence.equals("")) {
                            tvConfidence.setVisibility(View.VISIBLE);
                            try {
                                tvConfidence.setText(String.format("Confidence: %s", mConfidence));
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
                    btnPauseResume.setText(getString (R.string.pause));
                }
                //The lateral engine has been restarted, and we are currently monitoring the device's sensors and location in order to analyse another potential incident. There is no requirement to invoke any functions from either party in this context, as the engine on the side will handle the task automatically.
                break;
            case PAUSE_SIDE_ENGINE:
                isResumeActivity = true;
                btnPauseResume.setText(getString (R.string.resume));
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
}