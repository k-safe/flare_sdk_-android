package com.sideml.flutersideml;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
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
import static com.sos.busbysideengine.Constants.BBSideOperation.SOS_ACTIVATE;
import static com.sos.busbysideengine.Constants.BBSideOperation.SOS_DEACTIVATE;
import static com.sos.busbysideengine.Constants.BBSideOperation.START_FLARE_AWARE;
import static com.sos.busbysideengine.Constants.BBSideOperation.STOP_FLARE_AWARE;
import static com.sos.busbysideengine.Constants.ENVIRONMENT_PRODUCTION;

import static com.sos.busbysideengine.Constants.BBTheme.CUSTOM;
import static com.sos.busbysideengine.Constants.BBTheme.STANDARD;

public class MainActivity extends FlutterActivity implements BBSideEngineListener {
    private static final String CHANNEL = "com.sideml.flutersideml";
    private MethodChannel methodChannel;
    private MethodChannel.Result methodChannelResultConfig;
    private MethodChannel.Result methodChannelResultIncident;
    private MethodChannel.Result methodChannelResultSOS;
    private MethodChannel.Result methodChannelResultFlareAware;
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
                if(methodChannelResultIncident != null) {
                    methodChannelResultIncident.success(objects);
                }
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
                if(methodChannelResultIncidentAlerts != null){
                    methodChannelResultIncidentAlerts.success(objects);
                }
            }catch (Exception e){
                Log.e("INCIDENT_ALERT_SENT Error:", e.getMessage());
            }
        }else if (type == SOS_ACTIVATE) {
            if (response.has("sosLiveTrackingUrl")) {
                try {
                    Map<String,Object> objects = new HashMap<>();
                    objects.put("response",String.valueOf(response));
                    objects.put("type",String.valueOf(type));
                    objects.put("sosActive",true);
                    try {
                        if(methodChannelResultSOS == null){
                            methodChannelResultConfig.success(objects);
                        }else{
                            methodChannelResultSOS.success(objects);
                        }

                    }catch (Exception e){
                        Log.e("SOS_ACTIVATE Error:", e.getMessage());

                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else if (response.has("Error")) {
                //
            }
        }else if (type == SOS_DEACTIVATE) {
            try {
                Map<String,Object> objects = new HashMap<>();
                objects.put("response",String.valueOf(response));
                objects.put("type",String.valueOf(type));
                objects.put("sosActive",false);
                try {
                    methodChannelResultSOS.success(objects);
                    //methodChannelResultSOS.notImplemented();
                }catch (Exception e){
                    Log.e("SOS_DEACTIVATE Error:", e.getMessage());
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }else if (type == START_FLARE_AWARE) {
            try {
                Map<String,Object> objects = new HashMap<>();
                objects.put("isActive",true);
                methodChannelResultFlareAware.success(objects);
            } catch (Exception e) {
                Log.e("Error: ", e.toString());
            }
        }else if (type == STOP_FLARE_AWARE) {
            try {
                Map<String,Object> objects = new HashMap<>();
                objects.put("isActive",false);
                methodChannelResultFlareAware.success(objects);
            } catch (Exception e) {
                Log.e("Error: ", e.toString());
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
                            if (param != null && param.containsKey("userName")) {
                                userName = Objects.requireNonNull(param.get("userName")).toString();
                            }
                            String email = null;
                            if (param != null && param.containsKey("email")) {
                                email = Objects.requireNonNull(param.get("email")).toString();
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
                            bbSideEngine.startSideEngine(MainActivity.this);

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
                        String deviceId = Settings.Secure.getString(bbSideEngine.context.getContentResolver(), Settings.Secure.ANDROID_ID);
                        //TODO: Set user id
                        BBSideEngine.getInstance(null).setUserId(deviceId);
                        //TODO: Set rider name
                        BBSideEngine.getInstance(null).setRiderName(userName);
                        //TODO: call method for fetching W3W Location data
                        BBSideEngine.getInstance(null).fetchWhat3WordLocation(this);
                        //TODO: Send Email
                        BBSideEngine.getInstance(null).sendEmail(email);// Replace your emergency email address
                        //TODO: notify to partner
                        BBSideEngine.getInstance(null).notifyPartner();

                    }else if(call.method.equals("customPartnerNotify")){
//                        methodChannelResultIncidentAlerts = result;
                        Map<String,Object> param = call.arguments();
                        String userName = null;
                        String email = null;

                        if (param != null) {
                            userName = Objects.requireNonNull(param.get("userName")).toString();
                            email = Objects.requireNonNull(param.get("email")).toString();
                        }
                        String deviceId = Settings.Secure.getString(bbSideEngine.context.getContentResolver(), Settings.Secure.ANDROID_ID);
                        //TODO: Set user id
                        BBSideEngine.getInstance(null).setUserId(deviceId);
                        //TODO: Set rider name
                        BBSideEngine.getInstance(null).setRiderName(userName);
                        //TODO: call method for fetching W3W Location data
                        BBSideEngine.getInstance(null).fetchWhat3WordLocation(this);
                        //TODO: Send Email
                        BBSideEngine.getInstance(null).sendEmail(email);// Replace your emergency email address
                        //TODO: notify to partner
                        BBSideEngine.getInstance(null).notifyPartner();

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
                        String mode = "";
                        String lic = "";
                        if (param != null) {
                            isCustom = Boolean.TRUE.equals(param.get("isCustom"));
                            mode = String.valueOf(param.get("mode"));
                            lic = String.valueOf(param.get("lic"));
                        }
                        Constants.BBTheme theme = STANDARD;
                        if(isCustom){
                            theme = CUSTOM;
                        }
                        BBSideEngine.configure(MainActivity.this,
                                lic, mode, theme);
                    }else if(call.method.equals("startSOSML")){
                        methodChannelResultSOS = result;
                        Map<String,Object> param = call.arguments();
                        String deviceId = Settings.Secure.getString(bbSideEngine.context.getContentResolver(), Settings.Secure.ANDROID_ID);

                        String userName = null;
                        if (param != null && param.containsKey("userName")) {
                            userName = Objects.requireNonNull(param.get("userName")).toString();
                        }
                        String email = null;
                        if (param != null && param.containsKey("email")) {
                            email = Objects.requireNonNull(param.get("email")).toString();
                        }
                        boolean isActive = false;
                        if (param != null && param.containsKey("isActive")) {
                            isActive = Boolean.TRUE.equals(param.get("isActive"));
                        }
                        bbSideEngine.setUserId(deviceId);
                        if(email != null){
                            bbSideEngine.setUserEmail(email);
                        }
                        if(userName != null){
                            bbSideEngine.setUserName(userName);
                        }
                        if(isActive){
                            bbSideEngine.activeSOS();
                        }else{
                            bbSideEngine.deActiveSOS();
                        }
                    }else if(call.method.equals("startFlareAwareML")){
                        methodChannelResultFlareAware = result;
                        Map<String,Object> param = call.arguments();

                        boolean isActive = false;
                        if (param != null && param.containsKey("isActive")) {
                            isActive = Boolean.TRUE.equals(param.get("isActive"));
                        }
                        if(isActive) {
                            bbSideEngine.startFlareAware();
                        }else{
                            bbSideEngine.stopFlareAware();
                        }
                    }else {
                        result.notImplemented();
                    }
                }
        );
    }
}
