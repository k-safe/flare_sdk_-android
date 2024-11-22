package com.flare.sdk.android;

import static com.flaresafety.sideengine.Constants.ENVIRONMENT_PRODUCTION;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.flare.sdk.android.databinding.ActivityMainBinding;
import com.flare.sdk.android.model.Hazard;
import com.flaresafety.sideengine.BBSideEngine;
import com.flaresafety.sideengine.Constants;
import com.flaresafety.sideengine.rxjavaretrofit.network.model.BBSideEngineListener;
import com.flaresafety.sideengine.utils.Common;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class HazardsActivity extends AppCompatActivity implements BBSideEngineListener, OnMapReadyCallback {

    private ActivityMainBinding viewBinding;

    private BBSideEngine bbSideEngine;
    private boolean checkConfiguration = false;
    private String mode = ENVIRONMENT_PRODUCTION;

    private GoogleMap googleMap;
    private List<Hazard> hazardsList = new ArrayList<>();
    private boolean isReportHazards = true;

    private ActivityMainBinding viewBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());

        Intent intent = getIntent();
        mode = intent.getStringExtra("mode");

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

        val mapFragment =
                (supportFragmentManager.findFragmentById(R.id.fragmentMapView) as SupportMapFragment?)!!
                mapFragment.getMapAsync(this)
        setListener();

        val hazards = bbSideEngine.fetchHazards();
    }

    private void setListener() {
        viewBinding.ivCloseMain.setOnClickListener {
            finish();
        }

        viewBinding.btnReportHazards.setOnClickListener {
            if (checkConfiguration) {
                hazardsButtonClicked(true);
            }
        }

        viewBinding.btnManageHazards.setOnClickListener {
            if (checkConfiguration) {
                hazardsButtonClicked(false);
            }
        }
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


    private void recenterMap(Double lat,Double lon) {
        LatLng initialLatLng = new  LatLng(
                lat,
                lon
        );
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLatLng, 15f));
    }

    private List<Hazard> parseHazardsResponse(String jsonString)  {
        Gson gson = new Gson();
        val hazardListType = new TypeToken<List<Hazard>>() {}.type
        return gson.fromJson(jsonString, hazardListType);
    }


    override fun onMapReady(map: GoogleMap) {
        googleMap = map
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
                        hazardsList[0].lat,
                        hazardsList[0].lon
                );
                // Move the camera to the marker
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
            }
        }

        val baseMarkerSize = 100f  // Base size of the marker
        val markerSize = MarkerUtils.getMarkerSize(this, baseMarkerSize)

        for (hazard in hazardsList!!) {

            val markerLocation = LatLng(hazard.lat, hazard.lon) // Latitude and Longitude for NYC
            val iconResId =
                    resources.getIdentifier(hazard.iconDrawableName, "drawable", packageName)

            // Resize the marker icon to a smaller size (e.g., 64x64)
            val bitmap = BitmapFactory.decodeResource(resources, iconResId)
            val smallMarker = Bitmap.createScaledBitmap(bitmap, markerSize, markerSize, false)

            val icon = BitmapDescriptorFactory.fromBitmap(smallMarker)
            googleMap.addMarker(
                    MarkerOptions().position(markerLocation).title(hazard.name).icon(icon)
            )
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

    @Override
    public void onSideEngineCallback(boolean status, Constants.BBSideOperation type, JSONObject response) {

        switch (type) {
            case CONFIGURE -> {
                /*You now have the capability to call star Flare Aware function at any time. In the event that a user input button is unavailable,
                 you may start the Flare Aware using the function provided below.:
                 */
                checkConfiguration = status;
                Log.e("Configured", status.toString())
                viewBinding.progressBar.visibility = View.GONE
            } case FETCH_HAZARDS -> {
                Log.w("HazardsActivity", "FETCH_HAZARDS") if (status) {

                    if (hazardsList != null && hazardsList !!.isNotEmpty()){
                        googleMap.clear()
                    }

                    val hazards = response !!.getJSONArray("hazards")
                    if (hazards.length() > 0) {
                        hazardsList = parseHazardsResponse(hazards.toString())
                        refreshHazardsMap()
                    }
                } else {
                    val message = response !!.getString("message")
                    if (message.isNotEmpty()) {
                        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                    }
                }
            } case REPORT_HAZARD -> {
                Log.w("HazardsActivity", "REPORT_HAZARD") if (status) {
                    Log.w("Response", response.toString())
                } if (response !!has("message") {

                    val message = response.getString("message")
                    if (message.isNotEmpty()) {
                        Log.w("REPORT_HAZARD", message)
                    }
                }
            } case DELETE_HAZARD -> {
                Log.w("HazardsActivity", "DELETE_HAZARD") if (status) {
                    Log.w("DELETE_HAZARD", "" + true)
                } if (response != null && response.has("message")) {

                    String message = response.getString("message");
                    if (!message.isEmpty()) {
                        Log.w("DELETE_HAZARD", message);
                    }
                }
            } case MANAGE_HAZARD -> {
                Log.w("HazardsActivity", "MANAGE_HAZARD") if (status) {
                    Log.w("MANAGE_HAZARD", "true")
                } if (response != null && response.has("message")) {

                    String message = response.getString("message");
                    if (!message.isEmpty()) {
                        Log.w("MANAGE_HAZARD", message);
                    }
                }
            } case ALERTED_HAZARD -> {
                Log.w("HazardsActivity", "ALERTED_HAZARD");
                if (status) {
                    Log.w("ALERTED_HAZARD", response.toString());
                }
                if (response != null && response.has("message")) {

                    String message = response.getString("message");
                    if (!message.isEmpty()) {
                        Log.w("ALERTED_HAZARD", message);
                    }
                }
            }
            case FEEDBACK_HAZARD -> {
                Log.w("HazardsActivity", "FEEDBACK_HAZARD");
                if (status) {
                    Log.w("FEEDBACK_HAZARD", response.toString());
                }
                if (response != null && response.has("message")) {
                    String message = response.getString("message");
                    if (!message.isEmpty()) {
                        Log.w("FEEDBACK_HAZARD", message);
                    }
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

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

    }
}