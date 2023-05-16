package com.flare.sdk.android;


import static android.provider.Settings.Secure.ANDROID_ID;
import static com.sos.busbysideengine.Constants.ENVIRONMENT_PRODUCTION;

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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;

import com.sos.busbysideengine.BBSideEngine;
import com.sos.busbysideengine.Constants;
import com.sos.busbysideengine.rxjavaretrofit.network.model.BBSideEngineListener;

import org.json.JSONException;
import org.json.JSONObject;


public class EmergencySOSActivity extends AppCompatActivity implements BBSideEngineListener {

    private BBSideEngine bbSideEngine;
    private boolean checkConfiguration = false;
    private String sosLiveTrackingUrl = "";

    ImageView ivCloseMain;
    AppCompatButton btnSos, btnSOSLinkShare;

    private EditText etvUserEmail, etvUserName;
    private ProgressBar progressBar;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sos);

        init();
        setListener();
    }

    public void init() {

        ivCloseMain = findViewById(R.id.ivCloseMain);
        btnSos = findViewById(R.id.btnSos);
        btnSOSLinkShare = findViewById(R.id.btnSOSLinkShare);
        etvUserEmail = findViewById(R.id.etvUserEmail);
        etvUserName = findViewById(R.id.etvUserName);
        progressBar = findViewById(R.id.progressBar);

        //"Your production license key here"
        String lic = getIntent().getStringExtra("lic");

        bbSideEngine = BBSideEngine.getInstance(this);
        bbSideEngine.showLogs(true);
        bbSideEngine.setBBSideEngineListener(this);
        bbSideEngine.enableActivityTelemetry(true);

        BBSideEngine.configure(this,
                lic,
                ENVIRONMENT_PRODUCTION,
                Constants.BBTheme.STANDARD
        );
    }

    String[] per = {Manifest.permission.ACCESS_FINE_LOCATION};

    @SuppressLint("HardwareIds")
    private void setListener() {
        ivCloseMain.setOnClickListener(View -> {
            finish();
        });
        btnSos.setOnClickListener(View -> {
            if (checkConfiguration) {
                if (btnSos.getText().toString().equals("Deactivate SOS")) {
                    bbSideEngine.deActiveSOS();
                } else {
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
                        String deviceId = Secure.getString(
                                getContentResolver(),
                                ANDROID_ID
                        );
                        bbSideEngine.setUserId(deviceId);
                        bbSideEngine.setUserEmail(etvUserEmail.getText().toString().trim());
                        bbSideEngine.setUserName(etvUserName.getText().toString().trim());
                        bbSideEngine.activeSOS();
                    }
                }
            }
        });
        btnSOSLinkShare.setOnClickListener(View -> {
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("text/plain");
            share.putExtra(Intent.EXTRA_TEXT, sosLiveTrackingUrl);
            startActivity(Intent.createChooser(share, "Share link!"));
        });
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (!checkConfiguration) {
            return;
        }
        if (requestCode == 0) {
            String deviceId = Secure.getString(
                    getContentResolver(),
                    ANDROID_ID
            );
            bbSideEngine.setUserId(deviceId);
            bbSideEngine.setUserEmail(etvUserEmail.getText().toString().trim());
            bbSideEngine.setUserName(etvUserName.getText().toString().trim());
            bbSideEngine.activeSOS();
        } else if (requestCode == 1) {
//            viewBinding.btnStart.text = getString(R.string.start)
        }
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
                if (response.has("sosLiveTrackingUrl")) {
                    try {
                        sosLiveTrackingUrl = response.getString("sosLiveTrackingUrl");
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    btnSOSLinkShare.setVisibility(View.VISIBLE);
                    btnSos.setText("Deactivate SOS");
                } else if (response.has("Error")) {
                    //
                }
                break;

            case SOS_DEACTIVATE:
                btnSOSLinkShare.setVisibility(View.GONE);
                btnSos.setText("Activate SOS");
                break;

            default:
                Log.e("No Events Find", ":");
                break;
        }
    }
}


