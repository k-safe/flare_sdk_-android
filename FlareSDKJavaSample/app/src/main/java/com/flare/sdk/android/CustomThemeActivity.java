package com.flare.sdk.android;

import static com.sos.busbysideengine.Constants.BBTheme.CUSTOM;
import static com.sos.busbysideengine.Constants.ENVIRONMENT_PRODUCTION;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
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

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.sos.busbysideengine.BBSideEngine;
import com.sos.busbysideengine.Constants;
import com.sos.busbysideengine.rxjavaretrofit.network.model.BBSideEngineListener;
import com.sos.busbysideengine.utils.Common;
import com.sos.busbysideengine.utils.ContactClass;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Random;


public class CustomThemeActivity extends AppCompatActivity implements BBSideEngineListener {
    BBSideEngine bbSideEngine;

    Button btnStart, btnPauseResume;
    EditText etvUserName, etvCountryCode, etvMobileNumber, etvUserEmail;

    TextView tvConfidence, tvThemeName;
    ImageView ivCloseMain;

    private ProgressBar progressBar;

    boolean checkConfiguration = false;
    boolean isResumeActivity = false;
    String mConfidence;

    private String mode = ENVIRONMENT_PRODUCTION;

    Intent intent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme);

        init();
        setupEngine();
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
        Log.d("mConfidence1", "" + mConfidence);

        tvThemeName.setText(getString(R.string.custom_theme));

        //Custom Notification
        //        bbSideEngine.setNotificationMainBackgroundColor(R.color.white)
        //        bbSideEngine.setNotificationMainIcon(R.drawable.ic_launcher)
        //        bbSideEngine.setLocationNotificationTitle("Notification Title")
        //        bbSideEngine.setNotificationDescText("Notification Description")

    }

    public void setupEngine() {
        //"Your production license key here" or "Your sandbox license key here"
        String lic = intent.getStringExtra("lic");

        bbSideEngine = BBSideEngine.getInstance();
        bbSideEngine.showLogs(true);
        bbSideEngine.setBBSideEngineListener(this);
//        bbSideEngine.setEnableFlareAwareNetwork(true); //enableFlareAwareNetwork is a safety for cyclist to send notification for near by fleet users
//        bbSideEngine.setDistanceFilterMeters(20); //You can switch distance filter to publish location in the live tracking url, this should be send location every 20 meters when timer intervals is reached.
//        bbSideEngine.setLowFrequencyIntervalsSeconds(15);//Default is 15 sec, you can update this for your requirements, this will be used only when "high_frequency_mode_enabled" = false
//        bbSideEngine.setHighFrequencyIntervalsSeconds(3); //Default is 3 seconds, you can update this for your requirements, this will be used only when "high_frequency_mode_enabled" = true
//        bbSideEngine.setHighFrequencyModeEnabled(false); //Recommendation to enable high frequency mode when SOS is active, this will help us to batter live tracking experience.

//        bbSideEngine.setLocationNotificationTitle("Protection is active")
        bbSideEngine.setStickyEnable(true);
        bbSideEngine.activateIncidentTestMode(true); //This is only used in sandbox mode and is TRUE by default. This is why you should test your workflow in sandbox mode. You can change it to FALSE if you want to experience real-life incident detection

        bbSideEngine.configure(this,
                lic,
                mode,
                CUSTOM
        );
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
                    if (bbSideEngine.isEngineStarted()) {
                        bbSideEngine.setUserName(etvUserName.getText().toString().trim());
                        bbSideEngine.stopSideEngine();
                    } else {

                        BottomSheetDialog dialog = new BottomSheetDialog(CustomThemeActivity.this);
                        View view = getLayoutInflater().inflate(R.layout.dialog_activity, null);
                        LinearLayout llBike = view.findViewById(R.id.llBike);
                        LinearLayout llScooter = view.findViewById(R.id.llScooter);
                        LinearLayout llCycling = view.findViewById(R.id.llCycling);
                        LinearLayout llCancel = view.findViewById(R.id.llCancel);

                        llBike.setOnClickListener(v14 -> {
                            bbSideEngine.setActivityType("Bike");
                            bbSideEngine.startSideEngine(CustomThemeActivity.this);
                            dialog.dismiss();
                        });

                        llScooter.setOnClickListener(v13 -> {
                            bbSideEngine.setActivityType("Scooter");
                            bbSideEngine.startSideEngine(CustomThemeActivity.this);
                            dialog.dismiss();
                        });

                        llCycling.setOnClickListener(v12 -> {
                            bbSideEngine.setActivityType("Cycling");
                            bbSideEngine.startSideEngine(CustomThemeActivity.this);
                            dialog.dismiss();
                        });

                        llCancel.setOnClickListener(v1 -> dialog.dismiss());

                        dialog.dismiss();
                        dialog.setCancelable(true);
                        dialog.setContentView(view);
                        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                        int screenHeight = displayMetrics.heightPixels;
                        dialog.getBehavior().setPeekHeight(screenHeight);
                        dialog.show();

                    }

                    tvConfidence.setText("");
//                    btnStart.setText(bbSideEngine.isEngineStarted() ? getString(R.string.stop) : getString(R.string.start));
                }
        });

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
    public void onSideEngineCallback(
            boolean status,
            Constants.BBSideOperation type,
            JSONObject response
    ) {

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

                if (bbSideEngine.isEngineStarted()) {
                    btnStart.setText(getString(R.string.stop));
                } else {
                    btnStart.setText(getString(R.string.start));
                    btnPauseResume.setVisibility(View.GONE);
                }
                //Please update your user interface accordingly once the lateral engine has been initiated (for instance, modify the colour or text of the START button) to reflect the change in state.
                break;
            //Please update the user interface (UI) in this section to reflect the cessation of the side engine (e.g., amend the colour or text of the STOP button accordingly).
            case SMS:
                //Returns SMS delivery status and response payload
                Log.e("SMS: ", String.valueOf(response));
                break;
            case EMAIL:
                //Returns email delivery status and response payload
                Log.e("Email: ", String.valueOf(response));
                break;
            case INCIDENT_DETECTED:
                //You can initiate your bespoke countdown page from this interface, which must have a minimum timer interval of 30 seconds.

                //Upon completion of your custom countdown, it is imperative to invoke the 'notify partner' method to record the event on the dashboard and dispatch notifications via webhook, Slack, email and SMS.

                Log.w("CustomThemeActivity", "INCIDENT_DETECTED");
                setNotification();

                //TODO: Set user id
                bbSideEngine.setUserId(getRandomNumberString());

                //TODO: Set rider name
                bbSideEngine.setRiderName(etvUserName.getText().toString().trim());

                if (status) {
                    try {
                        boolean mCustomTheme = response.getBoolean("customTheme");
                        //Return incident status and confidence level, you can fetch confidance using the below code:
                        mConfidence = response.getString("confidence");
                        if (!mConfidence.equals("")){
                            tvConfidence.setVisibility(View.VISIBLE);
                            try {
                                tvConfidence.setText("Confidence: "+ mConfidence);
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
                                bbSideEngine.setUserId(getRandomNumberString());

                                //TODO: Set rider name
                                bbSideEngine.setRiderName(etvUserName.getText().toString().trim());

                                //TODO: call method for fetching W3W Location data
                                bbSideEngine.fetchWhat3WordLocation(this);

                                //TODO: Send Email and SMS
                                sendSMS();
                                sendEmail();

                                //TODO: notify to partner
                                bbSideEngine.notifyPartner();

                                bbSideEngine.resumeSensorIfAppInBackground();

                            } else {

                                Intent intent = new Intent(CustomThemeActivity.this, CustomUiActivity.class);
                                intent.putExtra("userName", etvUserName.getText().toString().trim());
                                intent.putExtra("email", etvUserEmail.getText().toString().trim());
                                intent.putExtra("mobileNo", etvMobileNumber.getText().toString().trim());
                                intent.putExtra("countryCode", etvCountryCode.getText().toString().trim());
                                intent.putExtra("btnTestClicked", !ENVIRONMENT_PRODUCTION.equals(mode));
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case INCIDENT_CANCEL:
                //User canceled countdown countdown to get event here, this called only for if you configured standard theme.
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
                //Countdown timer started after breach delay, this called only if you configured standard theme.
                break;
            case TIMER_FINISHED:
                //Countdown timer finished and jump to the incident summary page, this called only if you configured standard theme.
                break;
            case INCIDENT_ALERT_SENT:
                //Return the alert sent (returns alert details (i.e. time, location, recipient, success/failure))
                break;

        }
    }

    private void setNotification(){

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Calendar calendar = Calendar.getInstance();
        long randomNumber = calendar.getTimeInMillis();
        String channelId = "12345";
        Intent intent = new Intent(this, CustomThemeActivity.class);
        Notification.Builder builder;
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE);

        NotificationChannel notificationChannel =
                new NotificationChannel(channelId, "Incident Detected", NotificationManager.IMPORTANCE_HIGH);
        notificationChannel.setLightColor(Color.BLUE);
        notificationChannel.enableVibration(true);
        notificationManager.createNotificationChannel(notificationChannel);
        builder = new Notification
                .Builder(this, channelId)
                .setContentTitle(this.getString(R.string.app_name))
                .setContentText("Incident Detect")
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),
                        R.mipmap.ic_launcher_round
                ))
                .setContentIntent(pendingIntent);

        notificationManager.notify((int) randomNumber, builder.build());
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
    protected void onDestroy() {
        super.onDestroy();
        if (bbSideEngine.isEngineStarted()) {
            bbSideEngine.stopSideEngine();
        }
    }
}