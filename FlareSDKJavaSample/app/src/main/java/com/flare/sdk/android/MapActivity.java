package com.flare.sdk.android;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.flare.sdk.android.databinding.ActivityCustomUiBinding;
import com.flaresafety.sideengine.BBSideEngine;
import com.flaresafety.sideengine.Constants;
import com.flaresafety.sideengine.IncidentTypeCallback;
import com.flaresafety.sideengine.SurveyTypeCallback;
import com.flaresafety.sideengine.rxjavaretrofit.network.model.BBSideEngineUIListener;
import com.flaresafety.sideengine.utils.Common;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;


public class MapActivity extends AppCompatActivity implements BBSideEngineUIListener {

    private double latitude = 0.0;
    private double longitude = 0.0;
    private String word = "";
    private String mapUri = "";

    String userName = "", email = "", mobileNo = "", countryCode = "";
    boolean btnTestClicked = false;

    private ActivityCustomUiBinding viewBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewBinding = ActivityCustomUiBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());

        init();
        setListener();
    }

    public void init() {

        BBSideEngine.getInstance().setBBSideEngineListenerInLib(this);
        BBSideEngine.getInstance().fetchWhat3WordLocation(this);

        Intent intent = getIntent();
        userName = intent.getStringExtra("userName");
        email = intent.getStringExtra("email");
        mobileNo = intent.getStringExtra("mobileNo");
        countryCode = intent.getStringExtra("countryCode");
        btnTestClicked = intent.getBooleanExtra("btnTestClicked", false);
    }

    public void setListener() {

        viewBinding.tvCUIWord.setOnClickListener(v -> {
            if (!mapUri.equals("")) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(mapUri));
                startActivity(i);
            }
        });

        viewBinding.llCUIAlertTop.setOnClickListener(view -> {
            BBSideEngine.getInstance().launchIncidentClassification(this, new IncidentTypeCallback<>() {
                @Override
                public void onSubmit(String incidentType) {
                    Log.d("onSubmit:", incidentType);
                    callSurveyVideoPage();
                }

                @Override
                public void onClose() {
                    Log.d("onClose:", "");
                    callSurveyVideoPage();
                }
            });
        });
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
    public void onIncidentAlertCallback(boolean status, JSONObject response) {
        try {
            if (response != null) {
                JSONObject mJSONObjectResult = response.getJSONObject("result");
                if (mJSONObjectResult.has("words")) {
                    word = mJSONObjectResult.getString("words");
                }
                if (mJSONObjectResult.has("map")) {
                    mapUri = mJSONObjectResult.getString("map");
                }

                viewBinding.tvCUIWord.setText(String.format("//%s", word));
                if (mJSONObjectResult.has("latitude")) {
                    latitude = mJSONObjectResult.getDouble("latitude");
                }
                if (mJSONObjectResult.has("longitude")) {
                    longitude = mJSONObjectResult.getDouble("longitude");
                }
                setMap();
                viewBinding.tvCUILatLon.setText(String.format("Latitude: %s Longitude: %s", latitude, longitude));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCloseSurveyVideoActivityCallback() {
//        BBSideEngine.getInstance().resumeSideEngine();
//        finish();
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
        /*     if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        stopVibrate();
        BBSideEngine.getInstance().resumeSideEngine();
        finish();*/
    }

    @Override
    public void onIncidentVerifiedByUser(boolean status, JSONObject response) {

    }
}