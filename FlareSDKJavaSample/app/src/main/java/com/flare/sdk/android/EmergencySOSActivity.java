package com.flare.sdk.android;


import static android.provider.Settings.Secure.ANDROID_ID;

import static com.flaresafety.sideengine.Constants.ENVIRONMENT_PRODUCTION;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;


import com.flare.sdk.android.databinding.ActivityMainBinding;
import com.flare.sdk.android.databinding.ActivitySosBinding;
import com.flaresafety.sideengine.BBSideEngine;
import com.flaresafety.sideengine.Constants;
import com.flaresafety.sideengine.rxjavaretrofit.network.model.BBSideEngineListener;

import org.json.JSONException;
import org.json.JSONObject;


public class EmergencySOSActivity extends AppCompatActivity implements BBSideEngineListener {

    private BBSideEngine bbSideEngine;
    private boolean checkConfiguration = false;
    private String sosLiveTrackingUrl = "";
    private ActivitySosBinding viewBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewBinding = ActivitySosBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());

        init();
        setListener();
    }

    public void init() {

        Intent intent = getIntent();
        // "Your production license key here"
        String mode = intent.getStringExtra("mode");

        String secretKey = intent.getStringExtra("secretKey");
        String lic = intent.getStringExtra("lic");
        String region = intent.getStringExtra("region");

        bbSideEngine = BBSideEngine.getInstance();
        bbSideEngine.showLogs(true);
        bbSideEngine.setBBSideEngineListener(this);
        bbSideEngine.enableActivityTelemetry(true);
        bbSideEngine.setHazardFeatureEnabled(false); //The default hazard feature is enabled ( deafult value is true ), which can be adjusted to meet specific requirements. You can turn off by passing bbSideEngine.setHazardFeatureEnabled(false).
        bbSideEngine.setStickyEnable(true); //The default hazard feature is enabled ( deafult value is true ), which can be adjusted to meet specific requirements. You can turn off by passing bbSideEngine.setHazardFeatureEnabled(false).

        bbSideEngine.configure(this,
                lic,
                secretKey,
                ENVIRONMENT_PRODUCTION,
                Constants.BBTheme.STANDARD,
                region
        );
    }

    @SuppressLint("HardwareIds")
    private void setListener() {

        viewBinding.ivCloseMain.setOnClickListener(View -> finish());

        viewBinding.btnSos.setOnClickListener(View -> {

            if (checkConfiguration) {
                if (viewBinding.btnSos.getText().toString().equals(getString(R.string.stop_sos))) {
                    bbSideEngine.stopSOS();
                } else {
                    String deviceId = Secure.getString(
                            getContentResolver(),
                            ANDROID_ID
                    );
                    bbSideEngine.setUserId(deviceId);
                    bbSideEngine.setUserEmail(viewBinding.etvUserEmail.getText().toString().trim());
                    bbSideEngine.setUserName(viewBinding.etvUserName.getText().toString().trim());
                    bbSideEngine.startSOS();
                }
            }
        });

        viewBinding.btnSOSLinkShare.setOnClickListener(View -> {
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("text/plain");
            share.putExtra(Intent.EXTRA_TEXT, sosLiveTrackingUrl);
            startActivity(Intent.createChooser(share, "Share link!"));
        });
    }

    @Override
    public void onSideEngineCallback(boolean status, Constants.BBSideOperation type, JSONObject response) {
        switch (type) {
            case CONFIGURE -> {
                // if status = true Now you can ready to start Side engine process
                checkConfiguration = status;
                Log.e("Configured", String.valueOf(status));
                viewBinding.progressBar.setVisibility(View.GONE);
            }
            case START_SOS -> {
                //*The SOS function has been activated. You may now proceed to update your user interface and share a live location tracking link with your social contacts, thereby enabling them to access your real-time location.*//
                if (response.has("sosLiveTrackingUrl")) {
                    try {
                        sosLiveTrackingUrl = response.getString("sosLiveTrackingUrl");
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    viewBinding.btnSOSLinkShare.setVisibility(View.VISIBLE);
                    viewBinding.btnSos.setText(R.string.stop_sos);
                } else if (response.has("Error")) {
                    // log for error
                }
            }
            case STOP_SOS -> {
                viewBinding.btnSOSLinkShare.setVisibility(View.GONE);
                viewBinding.btnSos.setText(R.string.start_sos);
            }
            default -> Log.e("No Events Find", ":");
        }
    }
}


