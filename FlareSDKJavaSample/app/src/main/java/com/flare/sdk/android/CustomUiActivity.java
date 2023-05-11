package com.flare.sdk.android;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
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

    TextView tvCUISeconds, tvCUIFallDetected, tvCUIlatlong, tvCUIWord;

    CountDownTimer countDownTimer;
    RelativeLayout rlCUIMainBg;
    String userName = "", email = "", word = "", mapUri = "", mobileNo= "", countryCode= "";
    boolean btnTestClicked = false;
    boolean isSurvey = false;
    ImageView ivCUIClose;

    RelativeLayout rlCUIAlertView, rlCUIIncidentView;
    double latitude, longitude;

    Vibrator vibrator;

    PreferencesHelper preferencesHelper = null;
    private Common common;

    boolean isIncidentCanceled;

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
        BBSideEngine.getInstance(null).setBBSideEngineListenerInLib(this);

        Intent intent = getIntent();
        userName = intent.getStringExtra("userName");
        email = intent.getStringExtra("email");
        mobileNo = intent.getStringExtra("mobileNo");
        countryCode = intent.getStringExtra("countryCode");
        btnTestClicked = intent.getBooleanExtra("btnTestClicked", false);

        // Ui
        rlCUIAlertView = findViewById(R.id.rlCUIAlertView);
        rlCUIIncidentView = findViewById(R.id.rlCUIIncidentView);
        tvCUIlatlong = findViewById(R.id.tvCUIlatlong);
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
        BBSideEngine.getInstance(null).sendEmail(email);
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
        BBSideEngine.getInstance(null).sendSMS(contact);
    }

    public void setListener() {
        startVibrate();
        long time = common.getTimerInterval();

        countDownTimer = new CountDownTimer((time * 1000), 1000) {

            public void onTick(long millisUntilFinished) {
                tvCUISeconds.setText("" + millisUntilFinished / 1000);
                //here you can have your logic to set text to edittext
            }

            public void onFinish() {
                isIncidentCanceled = false;
                stopVibrate();

                //TODO: Set user id
                BBSideEngine.getInstance(null).setUserId(getRandomNumberString());

                //TODO: Set rider name
                BBSideEngine.getInstance(null).setRiderName(userName);

                //TODO: call method for fetching W3W Location data
                BBSideEngine.getInstance(null).fetchWhat3WordLocation(CustomUiActivity.this);

                //TODO: Send Email and SMS
                sendEmail();
                sendSMS();

                //TODO: notify to partner
                BBSideEngine.getInstance(null).notifyPartner();

                if(common.isAppInBackground()) {
                    BBSideEngine.getInstance(null).resumeSensorIfAppInBackground();
                    finish();
                }

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
                    tvCUIlatlong.setText("Latitude: " + locationData.getLatitude() + ' ' + "Longitude: " + locationData.getLongitude());
                } catch (Exception e) {
                    e.getMessage();
                }
            }
        }.start();

        ivCUIClose.setOnClickListener(v -> {
            if (countDownTimer != null) {
                countDownTimer.cancel();
            }
            stopVibrate();
            if(!isSurvey ||
                    (BBSideEngine.getInstance(null).surveyVideoURL() == null ||
                    BBSideEngine.getInstance(null).surveyVideoURL().equals(""))){
                BBSideEngine.getInstance(null).resumeSideEngine();
                finish();
            }else{
                BBSideEngine.getInstance(null).startSurveyVideoActivity();
            }
            //inactive function
        });
    }

    public void startVibrate() {
        long pattern[] = {0, 100, 200, 300, 400};
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(pattern, 0);
    }

    public void stopVibrate() {
        vibrator.cancel();
    }

    public void setMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.CUIMap);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull GoogleMap googleMap) {
                GoogleMap mMap = googleMap;

                // Add a marker in Sydney and move the camera
                LatLng currentLoc = new LatLng(latitude, longitude);
                mMap.setMinZoomPreference(10.0f);
                mMap.setMaxZoomPreference(20.0f);
                mMap.addMarker(new MarkerOptions()
                        .position(currentLoc)
                        .title("Current Location"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLoc));
                mMap.animateCamera(CameraUpdateFactory.newLatLng(currentLoc));
            }
        });
    }

    @Override
    public void onBackPressed() {

    }
    @Override
    public void onCloseSurveyVideoActivityCallback() {
        BBSideEngine.getInstance(null).resumeSideEngine();
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
        BBSideEngine.getInstance(null).resumeSideEngine();
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
                tvCUIWord.setText("//" + word);
                if (mJSONObjectResult.has("latitude")) {
                    latitude = mJSONObjectResult.getDouble("latitude");
                }
                if (mJSONObjectResult.has("longitude")) {
                    longitude = mJSONObjectResult.getDouble("longitude");
                }
                setMap();
                tvCUIlatlong.setText("Latitude: " + latitude + ' ' + "Longitude: " + longitude);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}