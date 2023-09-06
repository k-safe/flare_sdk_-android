package com.flare.sdk.android;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.sos.busbysideengine.BBSideEngine;
import com.sos.busbysideengine.location.LocationData;
import com.sos.busbysideengine.location.PreferencesHelper;
import com.sos.busbysideengine.rxjavaretrofit.network.model.BBSideEngineUIListener;
import com.sos.busbysideengine.utils.Common;
import com.sos.busbysideengine.utils.ContactClass;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;


public class CustomUiActivity extends AppCompatActivity implements BBSideEngineUIListener {

    TextView tvCUISeconds, tvCUIFallDetected, tvCUILatLong, tvCUIWord;

    CountDownTimer countDownTimer;
    RelativeLayout rlCUIMainBg;
    String userName = "", email = "", word = "", mapUri = "", mobileNo= "", countryCode= "";
    boolean btnTestClicked = false;
    ImageView ivCUIClose;

    boolean isIncidentCanceled = true;
    boolean isSurvey = false;

    RelativeLayout rlCUIAlertView, rlCUIIncidentView;
    double latitude = 0.0, longitude = 0.0;

    Vibrator vibrator;

    PreferencesHelper preferencesHelper = null;
    private Common common;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_ui);

        common = Common.getInstance();
        preferencesHelper = PreferencesHelper.getPreferencesHelper();

        init();
        setListener();
        setClick();
    }

    public void init() {
        BBSideEngine.getInstance().setBBSideEngineListenerInLib(this);

        Intent intent = getIntent();
        userName = intent.getStringExtra("userName");
        email = intent.getStringExtra("email");
        mobileNo = intent.getStringExtra("mobileNo");
        countryCode = intent.getStringExtra("countryCode");
        btnTestClicked = intent.getBooleanExtra("btnTestClicked", false);

        // Ui
        rlCUIAlertView = findViewById(R.id.rlCUIAlertView);
        rlCUIIncidentView = findViewById(R.id.rlCUIIncidentView);
        tvCUILatLong = findViewById(R.id.tvCUIlatlong);
        tvCUIWord = findViewById(R.id.tvCUIWord);
        ivCUIClose = findViewById(R.id.ivCUIClose);

        tvCUIFallDetected = findViewById(R.id.tvCUIFallDetected);
        tvCUISeconds = findViewById(R.id.tvCUISeconds);
        rlCUIMainBg = findViewById(R.id.rlCUIMainBg);
    }

    public void setClick() {
        tvCUIWord.setOnClickListener(v -> {
            if (!mapUri.equals("")) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(mapUri));
                startActivity(i);
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

    private void sendEmail() {
        if (email.equals("")) {
            return;
        }
        BBSideEngine.getInstance().sendEmail(email);
    }

    private void sendSMS() {
        if (countryCode.equals("") ||
                userName.equals("") ||
                mobileNo.equals("")) {
            return;
        }

        ContactClass contact = new ContactClass();
        contact.setCountryCode(countryCode);
        contact.setPhoneNumber(mobileNo);
        contact.setUserName(userName);
        BBSideEngine.getInstance().sendSMS(contact);
    }

    public void setListener() {
        startVibrate();
        long time = common.getTimerInterval();

        countDownTimer = new CountDownTimer((time * 1000), 1000) {

            public void onTick(long millisUntilFinished) {
                tvCUISeconds.setText(String.format("%d", millisUntilFinished / 1000));
                //here you can have your logic to set text to edittext
            }

            public void onFinish() {
                isIncidentCanceled = false;
                stopVibrate();

                //TODO: Set user id
                BBSideEngine.getInstance().setUserId(getRandomNumberString());

                //TODO: Set rider name
                BBSideEngine.getInstance().setRiderName(userName);

                //TODO: call method for fetching W3W Location data
                BBSideEngine.getInstance().fetchWhat3WordLocation(CustomUiActivity.this);

                isSurvey = true;
                rlCUIAlertView.setVisibility(View.VISIBLE);
                rlCUIIncidentView.setVisibility(View.GONE);
                try {
                    LocationData locationData = preferencesHelper.getLocationData(CustomUiActivity.this);
                    if (locationData != null) {
                        latitude = locationData.getLatitude();
                        longitude = locationData.getLongitude();
                    }
                    setMap();
                    tvCUILatLong.setText(String.format("Latitude: %s Longitude: %s", locationData.getLatitude(), locationData.getLongitude()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
              }
        }.start();

        ivCUIClose.setOnClickListener(v -> {
            if (countDownTimer != null) {
                countDownTimer.cancel();
            }
            stopVibrate();
            //inactive function
            feedbackWarning();
        });
    }

    private void feedbackWarning() {
        String appName = "Flare SDK Sample";
        AlertDialog.Builder builder = new AlertDialog.Builder(CustomUiActivity.this);
        builder.setTitle("Help " + appName + " become smarter")
                .setMessage(appName + " incident detection can be improved by learning from your incident. Was this an accurate alert?")
                .setCancelable(false)
                .setNegativeButton(
                        "No",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                BBSideEngine.getInstance().incidentDecline();
                                completeConfirmation();
                            }
                        })
                .setPositiveButton(
                        "Yes",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // TODO: Send Email and SMS
                                sendEmail();
                                sendSMS();

                                // TODO: Notify partner
                                BBSideEngine.getInstance().notifyPartner();

                                if (Common.getInstance().isAppInBackground()) {
                                    BBSideEngine.getInstance().resumeSensorIfAppInBackground();
                                    CustomUiActivity.this.finish();
                                } else {
                                    completeConfirmation();
                                }
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void completeConfirmation(){
        if (!isSurvey ||
                (BBSideEngine.getInstance().surveyVideoURL() == null ||
                        BBSideEngine.getInstance().surveyVideoURL() == "")) {
            BBSideEngine.getInstance().resumeSideEngine();
            finish();
        }else{
            BBSideEngine.getInstance().startSurveyVideoActivity();
        }
    }


    public void startVibrate() {
        long[] pattern = {0, 100, 200, 300, 400};
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(pattern, 0);
    }

    public void stopVibrate() {
        vibrator.cancel();
    }

    public void setMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.CUIMap);

        mapFragment.getMapAsync(googleMap -> {

            // Add a marker in Sydney and move the camera
            LatLng currentLoc = new LatLng(latitude, longitude);
            googleMap.setMinZoomPreference(10.0f);
            googleMap.setMaxZoomPreference(20.0f);
            googleMap.addMarker(new MarkerOptions()
                    .position(currentLoc)
                    .title("Current Location"));
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(currentLoc));
            googleMap.animateCamera(CameraUpdateFactory.newLatLng(currentLoc));
        });
    }

    @Override
    public void onCloseSurveyVideoActivityCallback() {
        BBSideEngine.getInstance().resumeSideEngine();
        finish();
    }
    @Override
    public void onSendSMSCallback(boolean status, JSONObject response) {

    }

    @Override
    public void onSendEmailCallback(boolean status, JSONObject response) {

    }

    @Override
    public void onIncidentCancelCallback(boolean status, JSONObject response) {

    }

    @Override
    public void onIncidentAutoCancelCallback(boolean status, JSONObject response) {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        stopVibrate();
        BBSideEngine.getInstance().resumeSideEngine();
        finish();
    }

    @Override
    public void onIncidentVerifiedByUser(boolean status, JSONObject response) {

    }

    @Override
    public void onIncidentAlertCallback(boolean status, JSONObject response) {
        try {
            if (response!= null) {
                JSONObject mJSONObjectResult = response.getJSONObject("result");
                if (mJSONObjectResult.has("words")) {
                    word = mJSONObjectResult.getString("words");
                }
                if (mJSONObjectResult.has("map")) {
                    mapUri = mJSONObjectResult.getString("map");
                }
                tvCUIWord.setText(String.format("//%s", word));
                if (mJSONObjectResult.has("latitude")) {
                    latitude = mJSONObjectResult.getDouble("latitude");
                }
                if (mJSONObjectResult.has("longitude")) {
                    longitude = mJSONObjectResult.getDouble("longitude");
                }
                setMap();
                tvCUILatLong.setText(String.format("Latitude: %s Longitude: %s", latitude, longitude));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}