package com.example.myapplication;

import static com.sos.busbysideengine.Constants.BBTheme.CUSTOM;
import static com.sos.busbysideengine.Constants.BBTheme.STANDARD;
import static com.sos.busbysideengine.Constants.ENVIRONMENT_PRODUCTION;
import static com.sos.busbysideengine.Constants.ENVIRONMENT_SANDBOX;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.sos.busbysideengine.BBSideEngine;
import com.sos.busbysideengine.Constants;
import com.sos.busbysideengine.rxjavaretrofit.network.model.BBSideEngineListener;
import com.sos.busbysideengine.utils.Common;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;


public class CustomThemeActivity extends AppCompatActivity implements BBSideEngineListener {

    Button btnStart, btnTestIncident;
    EditText etUserName, etCountryCode, etMobileNumber, etUserEmail;
    BBSideEngine bbSideEngine;
    boolean btnTestClicked = false;
    boolean checkConfiguration = false;
    TextView textView, tvThemeName;
    String mConfidence;
    ImageView ivClose;
    RelativeLayout rlTestIncident;
    LinearLayout ivCloseMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme);

        bbSideEngine = BBSideEngine.getInstance(this);
        bbSideEngine.showLogs(true);
        bbSideEngine.setBBSideEngineListener(this);
        bbSideEngine.enableActivityTelemetry(true);
        bbSideEngine.setLocationNotificationTitle("Protection is active");
        //Sandbox mode used only for while developing your App (You can use theme STANDARD OR CUSTOM)
        //BBSideEngine.configure(this,
        //"Your license key here",
        //ENVIRONMENT_SANDBOX, CUSTOM);

        //Custom Notification
//        bbSideEngine.setLocationNotificationTitle("Protection is active");
//        bbSideEngine.setNotificationMainBackgroundColor(R.color.white);
//        bbSideEngine.setNotificationMainIcon(R.drawable.ic_launcher);
//        bbSideEngine.setNotificationDescText("Notification Description");

        //Production mode used when you release app to the app store (You can use theme STANDARD OR CUSTOM)
        BBSideEngine.configure(this,
                "Your license key here",
        ENVIRONMENT_PRODUCTION, CUSTOM);

        init();
        setListener();
    }

    public void init() {
        btnStart = findViewById(R.id.btnStart);
        etCountryCode = findViewById(R.id.etCountryCode);
        etMobileNumber = findViewById(R.id.etMobileNumber);
        etUserName = findViewById(R.id.etUserName);
        etUserEmail = findViewById(R.id.etUserEmail);
        btnTestIncident = findViewById(R.id.btnTestIncident);
        textView=findViewById(R.id.mConfidence);
        tvThemeName=findViewById(R.id.tvThemeName);
        Log.d("mConfidence1",""+mConfidence);
        tvThemeName.setText("Custom theme");
        ivClose = findViewById(R.id.ivClose);
        rlTestIncident =findViewById(R.id.rlTestIncident);
        ivCloseMain=findViewById(R.id.ivCloseMain);
    }

    public void setListener() {
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bbSideEngine.setUserEmail(etUserEmail.getText().toString().trim());
                bbSideEngine.setUserName(etUserName.getText().toString().trim());
                btnTestClicked = false;
                if (bbSideEngine.isEngineStarted()) {
                    bbSideEngine.setUserName(etUserName.getText().toString().trim());
                    bbSideEngine.stopSideEngine();
                    textView.setText("");
                    ((Button) v).setText(bbSideEngine.isEngineStarted() ? getString(R.string.stop) : getString(R.string.start));
                } else {
                    bbSideEngine.startSideEngine(CustomThemeActivity.this, btnTestClicked);
                    ((Button) v).setText(bbSideEngine.isEngineStarted() ? getString(R.string.stop) : getString(R.string.start));
                }
            }
        });
        ivCloseMain.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        rlTestIncident.setVisibility(View.GONE);
                        bbSideEngine.stopSideEngine();
                    }
                }

        );

        btnTestIncident.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bbSideEngine.setUserEmail(etUserEmail.getText().toString().trim());
                bbSideEngine.setUserName(etUserName.getText().toString().trim());
                if (bbSideEngine.isEngineStarted()) {
                    bbSideEngine.stopSideEngine();
                    textView.setText("");
                    btnStart.setText( getString(R.string.start));
                }
                btnTestClicked = true;
                bbSideEngine.startSideEngine(CustomThemeActivity.this, btnTestClicked);
                if (checkConfiguration && ActivityCompat.checkSelfPermission(CustomThemeActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    rlTestIncident.setVisibility(View.VISIBLE);
                }
            }
        });
        ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (!checkConfiguration) {
            return;
        }
        if (requestCode == 0) {
            bbSideEngine.startSideEngine(CustomThemeActivity.this, btnTestClicked);
            if (!btnTestClicked) {
                btnStart.setText(bbSideEngine.isEngineStarted() ? getString(R.string.stop) : getString(R.string.start));
            } else {
                if (ActivityCompat.checkSelfPermission(CustomThemeActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    rlTestIncident.setVisibility(View.VISIBLE);
                }
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

        Log.w("SIDE_ENGINE_SDK", type.ordinal() + " " + (response != null ? response : ""));
        switch (type) {
            case CONFIGURE:
                // if status = true Now you can ready to start Side engine process
                checkConfiguration = status;
                break;
            case START:
                //Update your UI here (e.g. update START button color or text here when SIDE engine started)
                break;
            case STOP:
                //Update your UI here (e.g. update STOP button color or text here when SIDE engine started)
                break;
            case INCIDENT_DETECTED:
                //Threshold reached and you will redirect to countdown page
                rlTestIncident.setVisibility(View.GONE);
                Log.d("status", "" + status);
                //TODO: Set user id
                bbSideEngine.setUserId(getRandomNumberString());
                //TODO: Set rider name
                bbSideEngine.setRiderName(etUserName.getText().toString().trim());
                if (status) {
                    try {
                        boolean mCustomTheme = response.getBoolean("customTheme");
                        //Return incident status and confidence level, you can fetch confidance using the below code:
                        mConfidence = response.getString("confidence");
                        if (!mConfidence.equals("")){
                            textView.setVisibility(View.VISIBLE);
                            try {
                                textView.setText("Confidence: "+ mConfidence);
                            }catch (Exception e){
                                Log.e("Exception: ", e.getMessage());
                            }
                        }
                        Log.e("", mCustomTheme + "");
                        Log.e("mConfidence", mConfidence + "");
                        //TODO: If SDK is configured custom UI to open your screen here (MAKE SURE CONFIGURE SDK SELECTED CUSTOM THEME)
                        if (mCustomTheme) {
//                            bbSideEngine.stopSideEngine();
                            if (Common.getInstance().isAppInBackground()) {

                                //TODO: Set user id
                                BBSideEngine.getInstance(null).setUserId(getRandomNumberString());

                                //TODO: Set rider name
                                BBSideEngine.getInstance(null).setRiderName(etUserName.getText().toString().trim());

                                //TODO: call method for fetching W3W Location data
                                BBSideEngine.getInstance(null).fetchWhat3WordLocation(this);

                                //TODO: Send Email
                                BBSideEngine.getInstance(null).sendEmail(etUserEmail.getText().toString().trim(), btnTestClicked); // Replace your emergency email address

                                if (!btnTestClicked) {
                                    //TODO: notify to partner
                                    BBSideEngine.getInstance(null).notifyPartner();
                                }

                                BBSideEngine.getInstance(null).resumeSensorIfAppInBackground();

                            } else {
                                Intent intent = new Intent(CustomThemeActivity.this, CustomUiActivity.class);
                                intent.putExtra("userName", etUserName.getText().toString().trim());
                                intent.putExtra("email", etUserEmail.getText().toString().trim());
                                intent.putExtra("btnTestClicked", btnTestClicked);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                btnTestClicked = false;
                break;
            case INCIDENT_CANCEL:
                //User canceled countdown countdown to get event here, this called only for if you configured standard theme.
                break;
            case TIMER_STARTED:
                //Countdown timer started after breach delay, this called only if you configured standard theme.
                break;
            case TIMER_FINISHED:
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
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bbSideEngine.stopSideEngine();
    }
}