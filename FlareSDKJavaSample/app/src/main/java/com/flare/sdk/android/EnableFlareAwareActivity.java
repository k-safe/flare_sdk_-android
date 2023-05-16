package com.flare.sdk.android;

import static com.sos.busbysideengine.Constants.ENVIRONMENT_PRODUCTION;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;

import com.sos.busbysideengine.BBSideEngine;
import com.sos.busbysideengine.Constants;
import com.sos.busbysideengine.rxjavaretrofit.network.model.BBSideEngineListener;

import org.json.JSONObject;

public class EnableFlareAwareActivity extends AppCompatActivity implements BBSideEngineListener {

    private BBSideEngine bbSideEngine;
    private boolean checkConfiguration = false;
    private boolean isStartFlareAware = false;

    private AppCompatButton btnStartFlareAware;
    private ImageView ivCloseMain;

    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flareaware);

        init();
        setListener();
    }

    public void init() {
        ivCloseMain = findViewById(R.id.ivCloseMain);
        btnStartFlareAware = findViewById(R.id.btnStartFlareAware);
        progressBar = findViewById(R.id.progressBar);

        Intent intent = getIntent();
        //"Your production license key here"
        String lic = intent.getStringExtra("lic");

        bbSideEngine = BBSideEngine.getInstance(this);
        bbSideEngine.showLogs(true);
        bbSideEngine.setBBSideEngineListener(this);
        bbSideEngine.enableActivityTelemetry(true);
        bbSideEngine.setEnableFlareAwareNetwork(true);
        bbSideEngine.setHighFrequencyModeEnabled(true); //It is recommended to activate the high frequency mode when the SOS function is engaged in order to enhance the quality of the live tracking experience.
        bbSideEngine.setDistanceFilterMeters(20); //It is possible to activate the distance filter in order to transmit location data in the live tracking URL. This will ensure that location updates are transmitted every 20 meters, once the timer interval has been reached.
        bbSideEngine.setLowFrequencyIntervalsSeconds(15); //The default value is 15 seconds, which can be adjusted to meet specific requirements. This parameter will only be utilized in cases where bbSideEngine.setHighFrequencyModeEnabled(false) is invoked.
        bbSideEngine.setHighFrequencyIntervalsSeconds(3); //The default value is 3 seconds, which can be adjusted to meet specific requirements. This parameter will only be utilized in cases where bbSideEngine.setHighFrequencyModeEnabled(true) is invoked.

        BBSideEngine.configure(this,
                lic,
                ENVIRONMENT_PRODUCTION,
                Constants.BBTheme.STANDARD
        );
    }

    String[] per = {Manifest.permission.ACCESS_FINE_LOCATION};

    @SuppressLint("HardwareIds")
    private void setListener() {
        ivCloseMain.setOnClickListener(view -> finish());

        btnStartFlareAware.setOnClickListener(this::onClick);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (!checkConfiguration) {
            return;
        }
        if (requestCode == 0) {

            bbSideEngine.startFlareAware();

        } else if (requestCode == 1) {

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        bbSideEngine.stopFlareAware();
    }

    @Override
    public void onSideEngineCallback(boolean status, Constants.BBSideOperation type, JSONObject response) {
        switch (type) {
            case CONFIGURE:
                // if status = true Now you can ready to start Side engine process
                checkConfiguration = status;
                Log.e("Configured", String.valueOf(status));
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
                break;

            case EMAIL:
                //Returns email delivery status and response payload
                break;

            case INCIDENT_DETECTED:
                Toast.makeText(this, "INCIDENT_DETECTED", Toast.LENGTH_LONG).show();
                //Threshold reached and you will redirect to countdown page
                break;

            case INCIDENT_CANCEL:
                //User canceled countdown countdown to get event here, this called only for if you configured standard theme.
                break;

            case INCIDENT_ALERT_SENT:
                //Return the alert sent (returns alert details (i.e. time, location, recipient, success/failure))
                break;

            case TIMER_STARTED:
                //Countdown timer started after breach delay, this called only if you configured standard theme.
                break;

            case TIMER_FINISHED:
                //Countdown timer finished and jump to the incident summary page, this called only if you configured standard theme.
                break;

            case SOS_ACTIVATE:
                // sos activate
                break;

            case SOS_DEACTIVATE:
                // sos deactivate
                break;

            case START_FLARE_AWARE:
                // Start flare aware
                if (response != null) {
                    try {
                        String error = response.getString("Error");
                    } catch (Exception e) {
                        Log.e("Error: ", e.toString());
                    }
                } else {
                    isStartFlareAware = true;
                    btnStartFlareAware.setText(getString(R.string.stop_flare_aware));
                }

                break;

            case STOP_FLARE_AWARE:
                // Stop flare aware
                isStartFlareAware = false;
                btnStartFlareAware.setText(getString(R.string.start_flare_aware));
                break;

            default:
                Log.e("No Events Find", ":");
                break;
        }
    }

    private void onClick(View view) {
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
    }
}
