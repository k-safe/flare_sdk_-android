package com.flare.sdk.android

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.flare.sdk.android.databinding.HazardsFlareawareBinding
import com.flare.sdk.android.model.Hazard
import com.flare.sdk.android.utils.MarkerUtils
import com.flaresafety.sideengine.BBSideEngine
import com.flaresafety.sideengine.Constants
import com.flaresafety.sideengine.Constants.ENVIRONMENT_PRODUCTION
import com.flaresafety.sideengine.rxjavaretrofit.network.model.BBSideEngineListener
import com.flaresafety.sideengine.utils.Common
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONObject

class HazardsActivity : AppCompatActivity(), BBSideEngineListener, OnMapReadyCallback {

    private val viewBinding: HazardsFlareawareBinding by lazy {
        HazardsFlareawareBinding.inflate(layoutInflater)
    }
    private lateinit var bbSideEngine: BBSideEngine
    private var checkConfiguration = false
    private var mode: String? = ENVIRONMENT_PRODUCTION

    private lateinit var googleMap: GoogleMap
    private var hazardsList: List<Hazard>? = null
    private var isReportHazards = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        val intent = intent

        //"Your production license key here"
        val lic = intent.getStringExtra("lic")
        val secretKey = intent.getStringExtra("secretKey")
        val region = intent.getStringExtra("region")
        val isHazardEnabled = intent.getBooleanExtra("isHazardEnabled", true)

        bbSideEngine = BBSideEngine.getInstance()
        bbSideEngine.showLogs(true)
        bbSideEngine.setBBSideEngineListener(this)
//      bbSideEngine.enableActivityTelemetry(true)
        bbSideEngine.setHighFrequencyModeEnabled(true) //It is recommended to activate the high frequency mode when the SOS function is engaged in order to enhance the quality of the live tracking experience.
        bbSideEngine.setDistanceFilterMeters(20) //It is possible to activate the distance filter in order to transmit location data in the live tracking URL. This will ensure that location updates are transmitted every 20 meters, once the timer interval has been reached.
        bbSideEngine.setLowFrequencyIntervalsSeconds(15) //The default value is 15 seconds, which can be adjusted to meet specific requirements. This parameter will only be utilized in cases where bbSideEngine.setHighFrequencyModeEnabled(false) is invoked.
        bbSideEngine.setHighFrequencyIntervalsSeconds(3) //The default value is 3 seconds, which can be adjusted to meet specific requirements. This parameter will only be utilized in cases where bbSideEngine.setHighFrequencyModeEnabled(true) is invoked.
        bbSideEngine.setHazardFeatureEnabled(isHazardEnabled) //The default hazard feature is enabled, which can be adjusted to meet specific requirements. You can turn off by passing bbSideEngine.setHazardFeatureEnabled(false).
        bbSideEngine.setHazardVoiceAlertEnabled(true) //The default hazardVoiceAlertEnabled is enabled, which can be adjusted to meet specific requirements. You can turn off by passing bbSideEngine.setHazardVoiceAlertEnabled(false).

        bbSideEngine.configure(
            this,
            lic,
            secretKey,
            mode,
            Constants.BBTheme.STANDARD,
            region
        )

        val mapFragment =
            (supportFragmentManager.findFragmentById(R.id.fragmentMapView) as SupportMapFragment?)!!
        mapFragment.getMapAsync(this)
        setListener()

    }

    @SuppressLint("HardwareIds")
    private fun setListener() {
        viewBinding.ivCloseMain.setOnClickListener {
            finish()
        }

        viewBinding.btnReportHazards.setOnClickListener {
            if (checkConfiguration) {
                hazardsButtonClicked(true)
            }
        }

        viewBinding.btnManageHazards.setOnClickListener {
            if (checkConfiguration) {
                hazardsButtonClicked(false)
            }
        }
    }

    private fun hazardsButtonClicked(isItemClicked: Boolean) {
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
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                11211
            )
        } else {

            isReportHazards = isItemClicked

            val bbSideEngine = BBSideEngine.getInstance()
            if (isItemClicked) {
                bbSideEngine.reportHazards(this)
            } else {
                bbSideEngine.manageHazards(this)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (!checkConfiguration) {
            return
        }
        if (requestCode == 11211) {
            hazardsButtonClicked(isReportHazards)
        }
    }

    override fun onSideEngineCallback(
        status: Boolean,
        type: Constants.BBSideOperation?,
        response: JSONObject?
    ) {
        when (type) {
            Constants.BBSideOperation.CONFIGURE -> {
                /*You now have the capability to call star Flare Aware function at any time. In the event that a user input button is unavailable,
                 you may start the Flare Aware using the function provided below.:
                 */
                checkConfiguration = status
                Log.e("Configured", status.toString())
                val hazards = bbSideEngine.fetchHazards()
                viewBinding.progressBar.visibility = View.GONE
            }

            Constants.BBSideOperation.FETCH_HAZARDS -> {
                Log.w("HazardsActivity", "FETCH_HAZARDS")
                if (status) {

                    if (hazardsList != null && hazardsList!!.isNotEmpty()) {
                        googleMap.clear()
                    }

                    val hazards = response!!.getJSONArray("hazards")
                    if (hazards.length() > 0) {
                        hazardsList = parseHazardsResponse(hazards.toString())
                        refreshHazardsMap()
                    }
                } else {
                    val message = response!!.getString("message")
                    if (message.isNotEmpty()) {
                        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                    }
                }
            }

            Constants.BBSideOperation.REPORT_HAZARD -> {
                Log.w("HazardsActivity", "REPORT_HAZARD")
                if (status) {
                    Log.w("Response", response.toString())
                }
                if (response!!.has("message")) {

                    val message = response.getString("message")
                    if (message.isNotEmpty()) {
                        Log.w("REPORT_HAZARD", message)
                    }
                }
            }

            Constants.BBSideOperation.DELETE_HAZARD -> {
                Log.w("HazardsActivity", "DELETE_HAZARD")
                if (status) {
                    Log.w("DELETE_HAZARD", "" + true)
                }

                if (response != null && response.has("message")) {

                    val message = response.getString("message")
                    if (message.isNotEmpty()) {
                        Log.w("DELETE_HAZARD", message)
                    }
                }
            }

            Constants.BBSideOperation.MANAGE_HAZARD -> {
                Log.w("HazardsActivity", "MANAGE_HAZARD")

                if (status) {
                    Log.w("MANAGE_HAZARD", "true")
                }

                if (response != null && response.has("message")) {

                    val message = response.getString("message")
                    if (message.isNotEmpty()) {
                        Log.w("MANAGE_HAZARD", message)
                    }
                }
            }

            Constants.BBSideOperation.ALERTED_HAZARD -> {
                Log.w("HazardsActivity", "ALERTED_HAZARD")
                if (status) {
                    Log.w("ALERTED_HAZARD", response.toString())
                }

                if (response != null && response.has("message")) {

                    val message = response.getString("message")
                    if (message.isNotEmpty()) {
                        Log.w("ALERTED_HAZARD", message)
                    }
                }
            }

            Constants.BBSideOperation.FEEDBACK_HAZARD -> {
                Log.w("HazardsActivity", "FEEDBACK_HAZARD")
                if (status) {
                    Log.w("FEEDBACK_HAZARD", response.toString())
                }

                if (response != null && response.has("message")) {
                    val message = response.getString("message")
                    if (message.isNotEmpty()) {
                        Log.w("FEEDBACK_HAZARD", message)
                    }
                }
            }

            Constants.BBSideOperation.UPDATE_LOCATION -> {
                Log.w("HazardsActivity", "UPDATE_LOCATION")

                if (status) {
                    Log.w("UPDATE_LOCATION", response.toString())
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
                    return
                }
                googleMap.isMyLocationEnabled = true
                googleMap.uiSettings.isMyLocationButtonEnabled = false
                googleMap.uiSettings.isCompassEnabled = false
                googleMap.uiSettings.isRotateGesturesEnabled = true
                googleMap.uiSettings.isScrollGesturesEnabled = true
                googleMap.uiSettings.isZoomGesturesEnabled = true

                if (response != null && response.has("latitude") && response.has("latitude")) {
                    val latitude = response.getDouble("latitude")
                    val longitude = response.getDouble("longitude")
                    recenterMap(latitude, longitude);
                }
            }

            else -> {
                Log.e("No Events Find", ":")
            }
        }
    }

    private fun recenterMap(lat: Double, lon: Double) {
        val initialLatLng: LatLng = LatLng(
            lat,
            lon
        )
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLatLng, 15f))
    }

    private fun parseHazardsResponse(jsonString: String): List<Hazard> {
        val gson = Gson()
        val hazardListType = object : TypeToken<List<Hazard>>() {}.type
        return gson.fromJson(jsonString, hazardListType)
    }


    override fun onMapReady(map: GoogleMap) {
        googleMap = map
    }

    private fun refreshHazardsMap() {

        val locationData = Common.getInstance().latestLocation
        val location: LatLng
        if (locationData != null) {
            // Set a marker at a specific location (for example, New York City)
            location = LatLng(
                locationData.latitude,
                locationData.longitude
            ) // Latitude and Longitude for NYC
            // Move the camera to the marker
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))

        } else {

            if (hazardsList!!.isNotEmpty()) {
                location = LatLng(
                    hazardsList!![0].lat,
                    hazardsList!![0].lon
                )
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

    override fun onDestroy() {
        super.onDestroy()
        stopEngine()
    }

    private fun stopEngine() {
        bbSideEngine.stopSideEngine()
    }

    override fun onBackPressed() {
        stopEngine()
        super.onBackPressed()
    }

}