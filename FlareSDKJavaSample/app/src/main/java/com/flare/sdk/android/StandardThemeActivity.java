package com.flare.sdk.android;

import static com.sos.busbysideengine.Constants.BBTheme.STANDARD;
import static com.sos.busbysideengine.Constants.ENVIRONMENT_PRODUCTION;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.sos.busbysideengine.BBSideEngine;
import com.sos.busbysideengine.Constants;
import com.sos.busbysideengine.rxjavaretrofit.network.model.BBSideEngineListener;
import com.sos.busbysideengine.utils.ContactClass;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;


public class StandardThemeActivity extends AppCompatActivity implements BBSideEngineListener {

    private String mode = ENVIRONMENT_PRODUCTION;
    TextView tvConfidence, tvThemeName;
    ImageView ivCloseMain;
    Button btnStart;
    EditText etvUserName, etvCountryCode, etvMobileNumber, etvUserEmail;
    ProgressBar progressBar;

    BBSideEngine bbSideEngine;
    boolean btnTestClicked = false;
    boolean checkConfiguration = false;
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

        etvCountryCode = findViewById(R.id.etvCountryCode);
        etvMobileNumber = findViewById(R.id.etvMobileNumber);
        etvUserName = findViewById(R.id.etvUserName);
        etvUserEmail = findViewById(R.id.etvUserEmail);

        ivCloseMain = findViewById(R.id.ivCloseMain);
        tvConfidence = findViewById(R.id.tvConfidence);

        progressBar = findViewById(R.id.progressBar);
        Log.d("mConfidence1", "" + mConfidence);

        setupEngine();
    }

    public void setupEngine() {

        bbSideEngine = BBSideEngine.getInstance(this);
        bbSideEngine.showLogs(true);
        bbSideEngine.setBBSideEngineListener(this);
        bbSideEngine.enableActivityTelemetry(true);

//        bbSideEngine.setEnableFlareAwareNetwork(true); //The "enableFlareAwareNetwork" feature is a safety measure designed for cyclists, which allows them to send notifications to nearby fleet users.
//        bbSideEngine.setDistanceFilterMeters(20); //It is possible to activate the distance filter in order to transmit location data in the live tracking URL. This will ensure that location updates are transmitted every 20 meters, once the timer interval has been reached.
//        bbSideEngine.setLowFrequencyIntervalsSeconds(15); //The default value is 15 seconds, which can be adjusted to meet specific requirements. This parameter will only be utilized in cases where bbSideEngine.setHighFrequencyModeEnabled(false) is invoked.
//        bbSideEngine.setHighFrequencyIntervalsSeconds(3); //The default value is 3 seconds, which can be adjusted to meet specific requirements. This parameter will only be utilized in cases where bbSideEngine.setHighFrequencyModeEnabled(true) is invoked.
//        bbSideEngine.setHighFrequencyModeEnabled(false); //It is recommended to activate the high frequency mode when the SOS function is engaged in order to enhance the quality of the live tracking experience.
        bbSideEngine.enableActivityTelemetry(true);
//        bbSideEngine.setLocationNotificationTitle("Protection is active")
        bbSideEngine.setStickyEnable(false);

        //"Your production license key here" or "Your sandbox license key here"
        String lic = intent.getStringExtra("lic");
        BBSideEngine.configure(this,
                lic,
                mode,
                STANDARD
        );


        //Sandbox mode used only for while developing your App (You can use theme STANDARD OR CUSTOM)
        //BBSideEngine.configure(this,
        //"Your license key here",
        //ENVIRONMENT_SANDBOX, STANDARD);

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
        //bbSideEngine.setSwipeButtonBgColor(R.color.white) = Default "ffffff" //Only for standard theme
        //bbSideEngine.setSwipeButtonTextSize(18) = Default 16 //Only for standard theme
        //bbSideEngine.setSwipeButtonText("Swipe to Cancel"); //Only for standard theme
        //bbSideEngine.setImpactBody("Detected a potential fall or impact involving"); //This message show in the SMS, email, webook and slack body with rider name passed in this method (bbSideEngine.setRiderName("App user name here");) parameter


    }

    public void setListener() {
        btnStart.setOnClickListener(v -> {
            bbSideEngine.setUserEmail(etvUserEmail.getText().toString().trim());
            bbSideEngine.setUserName(etvUserName.getText().toString().trim());
            bbSideEngine.setUserCountryCode(etvCountryCode.getText().toString().trim());
            bbSideEngine.setUserMobile(etvMobileNumber.getText().toString().trim());

            btnTestClicked = false;
            if (bbSideEngine.isEngineStarted()) {
                bbSideEngine.setUserName(etvUserName.getText().toString().trim());
                bbSideEngine.stopSideEngine();
            } else {
                bbSideEngine.startSideEngine(StandardThemeActivity.this);
//                bbSideEngine.setUserId(getRandomNumberString())
            }
            tvConfidence.setText("");
            if (bbSideEngine.isEngineStarted()) {
                btnStart.setText(getString (R.string.stop));
            } else {
                btnStart.setText(getString(R.string.start));
            }
        });

        ivCloseMain.setOnClickListener(
                v -> {
                    finish();
                }
        );

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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (!checkConfiguration) {
            return;
        }
        if (requestCode == 0) {
            bbSideEngine.startSideEngine(this);
            if (bbSideEngine.isEngineStarted()) {
                btnStart.setText(getString(R.string.stop));
            } else {
                btnStart.setText(getString(R.string.start));
            }
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
                //Update your UI here (e.g. update START button color or text here when SIDE engine started)
                break;
            case STOP:
                //Update your UI here (e.g. update STOP button color or text here when SIDE engine started)
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
                                tvConfidence.setText("Confidence: $mConfidence");
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
            case INCIDENT_AUTO_CANCEL:
                tvConfidence.setVisibility(View.GONE);
                //User canceled countdown countdown to get event here, this called only for if you configured standard theme.
                break;
            case TIMER_STARTED:
                //Countdown timer started after breach delay, this called only if you configured standard theme.
                break;
            case TIMER_FINISHED:
                sendSMS();
                sendEmail();
                //Countdown timer finished and jump to the incident summary page, this called only if you configured standard theme.
                break;
            case INCIDENT_ALERT_SENT:
                //Return the alert sent (returns alert details (i.e. time, location, recipient, success/failure))
                break;
            case SMS:
                //Returns SMS delivery status and response payload
                Log.e("SMS: ", String.valueOf(response));
                break;
            case EMAIL:
                //Returns email delivery status and response payload
                Log.e("Email: ", String.valueOf(response));
                break;
            default:
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