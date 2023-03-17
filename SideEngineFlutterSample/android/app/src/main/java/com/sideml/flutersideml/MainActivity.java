package com.sideml.flutersideml;

import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sos.busbysideengine.BBSideEngine;
import com.sos.busbysideengine.Constants;
import com.sos.busbysideengine.rxjavaretrofit.network.model.BBSideEngineListener;
import com.sos.busbysideengine.utils.Common;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.MethodChannel;

import static com.sos.busbysideengine.Constants.BBSideOperation.CONFIGURE;
import static com.sos.busbysideengine.Constants.BBSideOperation.INCIDENT_ALERT_SENT;
import static com.sos.busbysideengine.Constants.BBSideOperation.INCIDENT_DETECTED;
import static com.sos.busbysideengine.Constants.ENVIRONMENT_PRODUCTION;

import static com.sos.busbysideengine.Constants.BBTheme.CUSTOM;
import static com.sos.busbysideengine.Constants.BBTheme.STANDARD;
import static com.sos.busbysideengine.Constants.ENVIRONMENT_PRODUCTION;
import static com.sos.busbysideengine.Constants.ENVIRONMENT_SANDBOX;

public class MainActivity extends FlutterActivity implements BBSideEngineListener {
    private static final String CHANNEL = "com.sideml.flutersideml";
    private MethodChannel methodChannel;
    private MethodChannel.Result methodChannelResultConfig;
    private MethodChannel.Result methodChannelResultIncident;
    private MethodChannel.Result methodChannelResultIncidentAlerts;
    private  Map<String,Object> callbackObject = new HashMap<>();
    BBSideEngine bbSideEngine;
    boolean flagToast = false;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bbSideEngine = BBSideEngine.getInstance(MainActivity.this);
        bbSideEngine.showLogs(true);
        bbSideEngine.setBBSideEngineListener(MainActivity.this);
        bbSideEngine.enableActivityTelemetry(true);

        //Custom Notification
//        bbSideEngine.setLocationNotificationTitle("Protection is active");
//        bbSideEngine.setNotificationMainBackgroundColor(R.color.white);
//        bbSideEngine.setNotificationMainIcon(R.drawable.ic_launcher);
//        bbSideEngine.setNotificationDescText("Notification Description");

        flagToast = false;

    }

    @SuppressLint("LongLogTag")
    @Override
    public void onSideEngineCallback(boolean status, Constants.BBSideOperation type, JSONObject response) {
        Log.e("type: ",type+" "+status+"");
        if (type == CONFIGURE) {
            flagToast = !status;
            Map<String,Object> objects = new HashMap<>();
            objects.put("isConfigure",status);
            try {
                methodChannelResultConfig.success(objects);
            }catch (Exception e){
                Log.e("Exception CONFIGURE:", e.getMessage());
            }
        }else if (type == INCIDENT_DETECTED) {
            String mConfidence = null;
            try {
                mConfidence = response.getString("confidence");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                response.put("isAppInBackground", Common.getInstance().isAppInBackground());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Map<String,Object> objects = new HashMap<>();
            objects.put("response",String.valueOf(response));
            objects.put("type",String.valueOf(type));
            try {

                methodChannelResultIncident.success(objects);
            }catch (Exception e){
                Log.e("INCIDENT_DETECTED Error:", ""+e.getMessage());
            }
            Log.e("mConfidence", mConfidence + "");
            Toast.makeText(MainActivity.this, "Incident detected with Confidence: "+mConfidence, Toast.LENGTH_SHORT).show();
        }else if (type == INCIDENT_ALERT_SENT) {
            Log.e("type: ",type+" "+status+"");
            Map<String,Object> objects = new HashMap<>();
            objects.put("response",String.valueOf(response));
            objects.put("type",String.valueOf(type));
            try {
                methodChannelResultIncidentAlerts.success(objects);
            }catch (Exception e){
                Log.e("INCIDENT_ALERT_SENT Error:", e.getMessage());
            }
        }
    }

    @Override
    public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {
        super.configureFlutterEngine(flutterEngine);
        methodChannel = new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), CHANNEL);
        methodChannel.setMethodCallHandler(
                (call, result) -> {
                    // Note: this method is invoked on the main thread.
                    // TODO
                    Log.e("call.method: ",call.method);

                    if (call.method.equals("startSideML")) {
                        if(flagToast){
                            result.success("Please enter valid license key");
                            return;
                        }

                        //Do this
                        Map<String,Object> objects = new HashMap<>();
                        if (bbSideEngine.isEngineStarted()) {
                            bbSideEngine.stopSideEngine();
                            objects.put("isServiceStart",false);
                            result.success(objects);
                        } else {
                            Map<String,Object> param = call.arguments();

                            String userName = null;
                            if (param != null) {
                                userName = Objects.requireNonNull(param.get("userName")).toString();
                            }
                            String email = null;
                            if (param != null) {
                                email = Objects.requireNonNull(param.get("email")).toString();
                            }
                            boolean isTestMode = false;
                            if (param != null) {
                                isTestMode = Boolean.TRUE.equals(param.get("isTestMode"));
                            }
                            if (param != null) {
                                Log.e("param: ",param.toString());
                            }
                            if(email != null){
                                bbSideEngine.setUserEmail(email.trim());
                            }
                            if(userName != null){
                                bbSideEngine.setUserName(userName);
                                bbSideEngine.setRiderName(userName);
                            }
                            String deviceId = Settings.Secure.getString(
                                    getContentResolver(), Settings.Secure.ANDROID_ID);
                            bbSideEngine.setUserId(deviceId);
                            objects.put("isServiceStart",true);
                            bbSideEngine.startSideEngine(MainActivity.this,isTestMode);

                            result.success(objects);
                        }
                    }else if(call.method.equals("incidentDetected")){
                        methodChannelResultIncident = result;
                    }else if(call.method.equals("timerFinish")){
                        methodChannelResultIncidentAlerts = result;
                        Map<String,Object> param = call.arguments();
                        String userName = null;
                        String email = null;

                        if (param != null) {
                            userName = Objects.requireNonNull(param.get("userName")).toString();
                            email = Objects.requireNonNull(param.get("email")).toString();
                        }
                        boolean isTestMode = false;
                        if (param != null) {
                            isTestMode = Boolean.TRUE.equals(param.get("isTestMode"));
                        }
                        String deviceId = Settings.Secure.getString(bbSideEngine.context.getContentResolver(), Settings.Secure.ANDROID_ID);
                        //TODO: Set user id
                        BBSideEngine.getInstance(null).setUserId(deviceId);
                        //TODO: Set rider name
                        BBSideEngine.getInstance(null).setRiderName(userName);
                        //TODO: call method for fetching W3W Location data
                        BBSideEngine.getInstance(null).fetchWhat3WordLocation(this);
                        //TODO: Send Email
                        BBSideEngine.getInstance(null).sendEmail(email, isTestMode);// Replace your emergency email address
                        if(!isTestMode){
                            //TODO: notify to partner
                            BBSideEngine.getInstance(null).notifyPartner();
                        }
                    }else if(call.method.equals("customPartnerNotify")){
//                        methodChannelResultIncidentAlerts = result;
                        Map<String,Object> param = call.arguments();
                        String userName = null;
                        String email = null;

                        if (param != null) {
                            userName = Objects.requireNonNull(param.get("userName")).toString();
                            email = Objects.requireNonNull(param.get("email")).toString();
                        }
                        boolean isTestMode = false;
                        if (param != null) {
                            isTestMode = Boolean.TRUE.equals(param.get("isTestMode"));
                        }
                        String deviceId = Settings.Secure.getString(bbSideEngine.context.getContentResolver(), Settings.Secure.ANDROID_ID);
                        //TODO: Set user id
                        BBSideEngine.getInstance(null).setUserId(deviceId);
                        //TODO: Set rider name
                        BBSideEngine.getInstance(null).setRiderName(userName);
                        //TODO: call method for fetching W3W Location data
                        BBSideEngine.getInstance(null).fetchWhat3WordLocation(this);
                        //TODO: Send Email
                        BBSideEngine.getInstance(null).sendEmail(email, isTestMode);// Replace your emergency email address
                        if(!isTestMode){
                            //TODO: notify to partner
                            BBSideEngine.getInstance(null).notifyPartner();
                        }
                    }else if(call.method.equals("resumeSideEngine")){
                        BBSideEngine.getInstance(null).resumeSideEngine();
                    }else if(call.method.equals("openSurveyUrl")){
                        BBSideEngine.getInstance(null).startSurveyVideoActivity();
                    }else if(call.method.equals("checkSurveyUrl")){
                        Map<String,Object> objects = new HashMap<>();
                        if((BBSideEngine.getInstance(null).surveyVideoURL() == null ||
                                BBSideEngine.getInstance(null).surveyVideoURL() == "")){
                            objects.put("isSurveyUrl",false);
                        }else{
                            objects.put("isSurveyUrl",true);
                        }
                        result.success(objects);
                    }else if(call.method.equals("configure")){
                        methodChannelResultConfig = result;
                        Map<String,Object> param = call.arguments();
                        boolean isCustom = false;
                        if (param != null) {
                            isCustom = Boolean.TRUE.equals(param.get("isCustom"));
                        }
                        Constants.BBTheme theme = STANDARD;
                        if(isCustom){
                            theme = CUSTOM;
                        }
                        Log.e("call.method isCustom: ", String.valueOf(isCustom));
                        BBSideEngine.configure(MainActivity.this,
                                "Your license key here",
                                ENVIRONMENT_PRODUCTION, theme);//CUSTOM
                    } else {
                        result.notImplemented();
                    }
                }
        );
    }
}
