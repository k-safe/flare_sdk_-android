package com.flare.sdk.android;

import static com.flaresafety.sideengine.Constants.ENVIRONMENT_PRODUCTION;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.flare.sdk.android.databinding.ActivityFlareawareBinding;
import com.flaresafety.sideengine.BBSideEngine;
import com.flaresafety.sideengine.Constants;
import com.flaresafety.sideengine.rxjavaretrofit.network.model.BBSideEngineListener;

import org.json.JSONObject;

public class EnableFlareAwareActivity extends AppCompatActivity implements BBSideEngineListener {

    private BBSideEngine bbSideEngine;
    private boolean checkConfiguration = false;
    private boolean isStartFlareAware = false;

    private String mode  = ENVIRONMENT_PRODUCTION;

    private ActivityFlareawareBinding viewBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewBinding = ActivityFlareawareBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());

        init();
        setListener();
    }

    public void init() {

        Intent intent = getIntent();
        mode = intent.getStringExtra("mode");

        //"Your production license key here"
        String lic = intent.getStringExtra("lic");
        String secretKey = intent.getStringExtra("secretKey");
        String region = intent.getStringExtra("region");

        bbSideEngine = BBSideEngine.getInstance();
        bbSideEngine.showLogs(true);
        bbSideEngine.setBBSideEngineListener(this);
        //    bbSideEngine.enableActivityTelemetry(true);
        bbSideEngine.setHighFrequencyModeEnabled(true); //It is recommended to activate the high frequency mode when the SOS function is engaged in order to enhance the quality of the live tracking experience.
        bbSideEngine.setDistanceFilterMeters(20); //It is possible to activate the distance filter in order to transmit location data in the live tracking URL. This will ensure that location updates are transmitted every 20 meters, once the timer interval has been reached.
        bbSideEngine.setLowFrequencyIntervalsSeconds(15); //The default value is 15 seconds, which can be adjusted to meet specific requirements. This parameter will only be utilized in cases where bbSideEngine.setHighFrequencyModeEnabled(false) is invoked.
        bbSideEngine.setHighFrequencyIntervalsSeconds(3); //The default value is 3 seconds, which can be adjusted to meet specific requirements. This parameter will only be utilized in cases where bbSideEngine.setHighFrequencyModeEnabled(true) is invoked.
        bbSideEngine.setHazardFeatureEnabled(false); //The default hazard feature is enabled ( deafult value is true ), which can be adjusted to meet specific requirements. You can turn off by passing bbSideEngine.setHazardFeatureEnabled(false).

        bbSideEngine.configure(
                this,
                lic,
                secretKey,
                mode,
                Constants.BBTheme.STANDARD,
                region
        );
    }

    String[] per = {Manifest.permission.ACCESS_FINE_LOCATION};

    @SuppressLint("HardwareIds")
    private void setListener() {

        viewBinding.ivCloseMain.setOnClickListener(view -> finish());

        viewBinding.btnStartFlareAware.setOnClickListener(view -> {
            if (checkConfiguration) {
                // code here
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
                        || ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                            this,
                            per,
                            0
                    );
                } else {
                    if (isStartFlareAware) {
                        bbSideEngine.stopFlareAware();
                    } else {
                        bbSideEngine.startFlareAware();
                    }
                }
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (!checkConfiguration) {
            return;
        }
        if (requestCode == 0) {
            new Handler(Looper.getMainLooper()).postDelayed(() -> bbSideEngine.startFlareAware(), 2000);
        }
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
            case START_FLARE_AWARE -> {
                // Start flare aware
                if (response != null) {
                    try {
                        String error = response.getString("Error");
                        Log.e("flareAware error", error);
                    } catch (Exception e) {
                        Log.e("Error: ", e.toString());
                    }
                } else {
                    isStartFlareAware = true;
                    viewBinding.btnStartFlareAware.setText(getString(R.string.stop_flare_aware));
                }
            }
            case STOP_FLARE_AWARE -> {
                // Stop flare aware
                isStartFlareAware = false;
                viewBinding.btnStartFlareAware.setText(getString(R.string.start_flare_aware));
            }
            default -> Log.e("No Events Find", ":");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        bbSideEngine.stopFlareAware();
    }

}
