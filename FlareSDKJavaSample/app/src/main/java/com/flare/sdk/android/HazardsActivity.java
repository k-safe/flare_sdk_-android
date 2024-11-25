package com.flare.sdk.android;

import static com.flaresafety.sideengine.Constants.ENVIRONMENT_PRODUCTION;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.flare.sdk.android.databinding.ActivityHazardsBinding;
import com.flare.sdk.android.model.Hazard;
import com.flare.sdk.android.utils.MarkerUtils;
import com.flaresafety.sideengine.BBSideEngine;
import com.flaresafety.sideengine.Constants;
import com.flaresafety.sideengine.rxjavaretrofit.network.model.BBSideEngineListener;
import com.flaresafety.sideengine.utils.Common;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class HazardsActivity extends AppCompatActivity implements BBSideEngineListener, OnMapReadyCallback {

    private BBSideEngine bbSideEngine;
    private boolean checkConfiguration = false;
    private String mode = ENVIRONMENT_PRODUCTION;

    private GoogleMap googleMap;
    private List<Hazard> hazardsList = new ArrayList<>();
    private boolean isReportHazards = true;

    private ActivityHazardsBinding viewBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewBinding = ActivityHazardsBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());

        init();
        setListener();
    }

    public void init() {

        Intent intent = getIntent();

        //"Your production license key here"
        String lic = intent.getStringExtra("lic");
        String secretKey = intent.getStringExtra("secretKey");
        String region = intent.getStringExtra("region");
        boolean isHazardEnabled = intent.getBooleanExtra("isHazardEnabled", true);

        bbSideEngine = BBSideEngine.getInstance();
        bbSideEngine.showLogs(true);
        bbSideEngine.setBBSideEngineListener(this);
//      bbSideEngine.enableActivityTelemetry(true)
        bbSideEngine.setHighFrequencyModeEnabled(true); // It is recommended to activate the high frequency mode when the SOS function is engaged in order to enhance the quality of the live tracking experience.
        bbSideEngine.setDistanceFilterMeters(20); // It is possible to activate the distance filter in order to transmit location data in the live tracking URL. This will ensure that location updates are transmitted every 20 meters, once the timer interval has been reached.
        bbSideEngine.setLowFrequencyIntervalsSeconds(15); // The default value is 15 seconds, which can be adjusted to meet specific requirements. This parameter will only be utilized in cases where bbSideEngine.setHighFrequencyModeEnabled(false) is invoked.
        bbSideEngine.setHighFrequencyIntervalsSeconds(3); // The default value is 3 seconds, which can be adjusted to meet specific requirements. This parameter will only be utilized in cases where bbSideEngine.setHighFrequencyModeEnabled(true) is invoked.
        bbSideEngine.setHazardFeatureEnabled(isHazardEnabled); // The default hazard feature is enabled, which can be adjusted to meet specific requirements. You can turn off by passing bbSideEngine.setHazardFeatureEnabled(false).
        bbSideEngine.setHazardVoiceAlertEnabled(true); // The default hazardVoiceAlertEnabled is enabled, which can be adjusted to meet specific requirements. You can turn off by passing bbSideEngine.setHazardVoiceAlertEnabled(false).

        bbSideEngine.configure(
                this,
                lic,
                secretKey,
                mode,
                Constants.BBTheme.STANDARD,
                region
        );

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragmentMapView);
        mapFragment.getMapAsync(this);

    }

    private void setListener() {
        viewBinding.ivCloseMain.setOnClickListener(view -> {
            finish();
        });

        viewBinding.btnReportHazards.setOnClickListener(view -> {
            if (checkConfiguration) {
                hazardsButtonClicked(true);
            }
        });

        viewBinding.btnManageHazards.setOnClickListener(view -> {
            if (checkConfiguration) {
                hazardsButtonClicked(false);
            }
        });
    }

    private void hazardsButtonClicked(Boolean isItemClicked) {
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
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    11211
            );
        } else {

            isReportHazards = isItemClicked;

            BBSideEngine bbSideEngine = BBSideEngine.getInstance();
            if (isItemClicked) {
                bbSideEngine.reportHazards(this);
            } else {
                bbSideEngine.manageHazards(this);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (!checkConfiguration) {
            return;
        }
        if (requestCode == 11211) {
            hazardsButtonClicked(isReportHazards);
        }
    }

    @Override
    public void onSideEngineCallback(boolean status, Constants.BBSideOperation type, JSONObject response) {

        switch (type) {
            case CONFIGURE -> {
                /*You now have the capability to call star Flare Aware function at any time. In the event that a user input button is unavailable,
                 you may start the Flare Aware using the function provided below.:
                 */
                checkConfiguration = status;
                Log.e("Configured", String.valueOf(status));
                viewBinding.progressBar.setVisibility(View.GONE);

                JSONObject hazardsListResponse = bbSideEngine.fetchHazards();
                // parse above json and get the list of active hazards as per specify radius settings.


            }
            case FETCH_HAZARDS -> {
                Log.w("HazardsActivity", "FETCH_HAZARDS");

                try {
                    if (status) {

                        if (hazardsList != null && !hazardsList.isEmpty()) {
                            googleMap.clear();
                        }

                        JSONArray hazards = response.getJSONArray("hazards");
                        if (hazards.length() > 0) {
                            hazardsList = parseHazardsResponse(hazards.toString());
                            refreshHazardsMap();
                        }
                    } else {
                        String message = response.getString("message");
                        if (!message.isEmpty()) {
                            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                        }
                    }
                } catch (Exception ignored) {

                }
            } case REPORT_HAZARD -> {
                Log.w("HazardsActivity", "REPORT_HAZARD");
                if (status) {
                    Log.w("Response", response.toString());
                }

                try {
                    if (response != null && response.has("message")) {

                        String message = response.getString("message");

                        if (!message.isEmpty()) {
                            Log.w("REPORT_HAZARD", message);
                        }
                    }
                } catch (Exception ignored) {

                }
            }
            case DELETE_HAZARD -> {

                Log.w("HazardsActivity", "DELETE_HAZARD");
                if (status) {
                    Log.w("Response", response.toString());
                }
                try {
                    if (response != null && response.has("message")) {
                        String message = response.getString("message");
                        if (!message.isEmpty()) {
                            Log.w("DELETE_HAZARD", message);
                        }
                    }
                } catch (Exception ignored) {

                }

            }
            case MANAGE_HAZARD -> {

                Log.w("HazardsActivity", "MANAGE_HAZARD");

                if (status) {
                    Log.w("Response", response.toString());
                }
                try {
                    if (response != null && response.has("message")) {
                        String message = response.getString("message");
                        if (!message.isEmpty()) {
                            Log.w("MANAGE_HAZARD", message);
                        }
                    }
                } catch (Exception ignored) {

                }

            } case ALERTED_HAZARD -> {
                Log.w("HazardsActivity", "ALERTED_HAZARD");

                if (status) {
                    Log.w("ALERTED_HAZARD", response.toString());
                }

                try {
                    if (response != null && response.has("message")) {

                        String message = response.getString("message");
                        if (!message.isEmpty()) {
                            Log.w("ALERTED_HAZARD", message);
                        }
                    }
                } catch (Exception ignored) {

                }
            }
            case FEEDBACK_HAZARD -> {

                Log.w("HazardsActivity", "FEEDBACK_HAZARD");
                if (status) {
                    Log.w("FEEDBACK_HAZARD", response.toString());
                }

                try {
                    if (response != null && response.has("message")) {
                        String message = response.getString("message");
                        if (!message.isEmpty()) {
                            Log.w("FEEDBACK_HAZARD", message);
                        }
                    }
                } catch (Exception ignored) {

                }
            }
            case UPDATE_LOCATION -> {
                Log.w("HazardsActivity", "UPDATE_LOCATION");

                if (status) {
                    Log.w("UPDATE_LOCATION", response.toString());
                }

                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                googleMap.setMyLocationEnabled(true);
                googleMap.getUiSettings().setMyLocationButtonEnabled(false);
                googleMap.getUiSettings().setCompassEnabled(false);
                googleMap.getUiSettings().setRotateGesturesEnabled(true);
                googleMap.getUiSettings().setScrollGesturesEnabled(true);
                googleMap.getUiSettings().setZoomGesturesEnabled(true);

                if (response != null && response.has("latitude") && response.has("latitude")) {
                    try {
                        Double latitude = response.getDouble("latitude");
                        Double longitude = response.getDouble("longitude");
                        recenterMap(latitude, longitude);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }

                }
            }
            default -> Log.e("No Events Find", ":");
        }
    }

    private void recenterMap(Double lat, Double lon) {
        LatLng initialLatLng = new LatLng(
                lat,
                lon
        );
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLatLng, 15f));
    }

    private List<Hazard> parseHazardsResponse(String jsonString) {
        Gson gson = new Gson();
        Type hazardListType = new TypeToken<List<Hazard>>() {
        }.getType();
        return gson.fromJson(jsonString, hazardListType);

    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
    }

    private void refreshHazardsMap() {

        Location locationData = Common.getInstance().getLatestLocation();
        LatLng location;
        if (locationData != null) {
            // Set a marker at a specific location (for example, New York City)
            location = new LatLng(
                    locationData.getLatitude(),
                    locationData.getLongitude()
            ); // Latitude and Longitude for NYC
            // Move the camera to the marker
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f));

        } else {

            if (hazardsList.size() > 0) {
                location = new LatLng(
                        hazardsList.get(0).getLat(),
                        hazardsList.get(0).getLon()
                );
                // Move the camera to the marker
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f));
            }
        }

        float baseMarkerSize = 100f;  // Base size of the marker
        int markerSize = MarkerUtils.getMarkerSize(this, baseMarkerSize);

        for (Hazard hazard : hazardsList) {

            LatLng markerLocation = new LatLng(hazard.getLat(), hazard.getLon()); // Latitude and Longitude for NYC
            int iconResId = getResources().getIdentifier(hazard.getIconDrawableName(), "drawable", getPackageName());

            // Resize the marker icon to a smaller size (e.g., 64x64)
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), iconResId);
            Bitmap smallMarker = Bitmap.createScaledBitmap(bitmap, markerSize, markerSize, false);

            BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(smallMarker);
            googleMap.addMarker(new MarkerOptions().position(markerLocation).title(hazard.getName()).icon(icon));
        }

    }

    public void onDestroy() {
        super.onDestroy();
        stopEngine();
    }

    private void stopEngine() {
        bbSideEngine.stopSideEngine();
    }

    public void onBackPressed() {
        stopEngine();
        super.onBackPressed();
    }
}