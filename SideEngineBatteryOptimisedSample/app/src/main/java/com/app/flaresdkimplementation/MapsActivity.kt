package com.app.flaresdkimplementation

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.HandlerThread
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.sos.busbysideengine.BBSideEngine
import com.sos.busbysideengine.Constants
import com.sos.busbysideengine.rxjavaretrofit.network.model.BBSideEngineListener
import com.sos.busbysideengine.utils.Common
import org.json.JSONException
import org.json.JSONObject

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, BBSideEngineListener {

    private lateinit var map: GoogleMap
    private val TAG = MapsActivity::class.java.simpleName
    private val REQUEST_LOCATION_PERMISSION = 1
    var fusedLocationProviderClient: FusedLocationProviderClient? = null;
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private var locationUpdateState = false

    private lateinit var bbSideEngine: BBSideEngine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        bbSideEngine = BBSideEngine.getInstance(this)
        bbSideEngine.showLogs(true)
        bbSideEngine.setBBSideEngineListener(this)
        bbSideEngine.enableActivityTelemetry(true)
        bbSideEngine.setLocationNotificationTitle("Protection is active")
        bbSideEngine.setStickyEnable(true)

        //Sandbox mode used only for while developing your App (You can use theme STANDARD OR CUSTOM)
        //BBSideEngine.configure(this,
        //"Your license key here",
        //ENVIRONMENT_SANDBOX, STANDARD);

        BBSideEngine.configure(
            this,
            "Your license key here",
            Constants.ENVIRONMENT_PRODUCTION,
            Constants.BBTheme.CUSTOM
        )

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
                Log.e("","LocationCallback >>>>")
            }
        }
        createLocationRequest()
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        enableMyLocation()
        map.mapType = GoogleMap.MAP_TYPE_TERRAIN

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI()

        // Get the current location of the device and set the position of the map.
        getDeviceLocation()
    }

    @SuppressLint("MissingPermission")
    private fun updateLocationUI() {
        if (map == null) {
            return
        }
        try {
            if (isPermissionGranted()) {
                map?.isMyLocationEnabled = true
                map?.uiSettings?.isMyLocationButtonEnabled = true
            } else {
                map?.isMyLocationEnabled = false
                map?.uiSettings?.isMyLocationButtonEnabled = false
                enableMyLocation()
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    @SuppressLint("MissingPermission")
    private fun getDeviceLocation() {
        try {
            if (isPermissionGranted()) {
                val locationResult = fusedLocationProviderClient?.lastLocation
                locationResult?.addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Set the map's camera position to the current location of the device.
                        var lastKnownLocation = task.result
                        if (lastKnownLocation != null) {
                            map?.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                LatLng(lastKnownLocation!!.latitude,
                                    lastKnownLocation!!.longitude), 15f))
                        }
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }
    // Checks that users have given permission
    private fun isPermissionGranted() : Boolean {
       return ContextCompat.checkSelfPermission(
            this,
           Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    // Checks if users have given their location and sets location enabled if so.
    private fun enableMyLocation() {
        if (isPermissionGranted()) {
            map.isMyLocationEnabled = true
        }
        else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }

    // Callback for the result from requesting permissions.
    // This method is invoked for every call on requestPermissions(android.app.Activity, String[],
    // int).
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray) {
        // Check if location permissions are granted and if so enable the
        // location data layer.
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.contains(PackageManager.PERMISSION_GRANTED)) {
                enableMyLocation()
            }
        }
    }

    override fun onSideEngineCallback(
        status: Boolean,
        type: Constants.BBSideOperation?,
        response: JSONObject?
    ) {
        when (type) {
            Constants.BBSideOperation.CONFIGURE -> {
                // if status = true Now you can ready to start Side engine process
//                checkConfiguration = status
//                progressBar.visibility = View.GONE
                Toast.makeText(this, "CONFIGURE Side Engine",Toast.LENGTH_LONG).show();
                bbSideEngine.startSideEngine(this, false)
            }
            Constants.BBSideOperation.START -> {
                Toast.makeText(this, "START Side Engine",Toast.LENGTH_LONG).show();
                //Update your UI here (e.g. update START button color or text here when SIDE engine started)
            }
            Constants.BBSideOperation.STOP -> {
                Toast.makeText(this, "STOP Side Engine",Toast.LENGTH_SHORT).show();
                //Update your UI here (e.g. update STOP button color or text here when SIDE engine started)
            }
            Constants.BBSideOperation.SMS -> {
                //Returns SMS delivery status and response payload
            }
            Constants.BBSideOperation.EMAIL -> {
                //Returns email delivery status and response payload
            }
            Constants.BBSideOperation.INCIDENT_DETECTED -> {
                //Threshold reached and you will redirect to countdown page
                Log.w("CustomThemeActivity", "INCIDENT_DETECTED")

                Toast.makeText(this, "INCIDENT DETECTED Side Engine",Toast.LENGTH_LONG).show();

                //TODO: Set user id
                bbSideEngine.setUserId(CustomThemeActivity.getRandomNumberString())
                //TODO: Set rider name
                bbSideEngine.setRiderName("Testing User")
                if (status) {
                    try {
                        val mCustomTheme = response!!.getBoolean("customTheme")
                        val mConfidence = response.getString("confidence")
                        Log.e("", mCustomTheme.toString() + "")
                        Log.e("mConfidence", mConfidence.toString() + "")
                        //TODO: If SDK is configured custom UI to open your screen here (MAKE SURE CONFIGURE SDK SELECTED CUSTOM THEME)
                        if (mCustomTheme) {
                            if (Common.getInstance().isAppInBackground) {
                                //TODO: Set user id
                                BBSideEngine.getInstance(null).setUserId(CustomThemeActivity.getRandomNumberString())

                                //TODO: Set rider name
                                BBSideEngine.getInstance(null).setRiderName("Testing User")

                                //TODO: call method for fetching W3W Location data
                                BBSideEngine.getInstance(null).fetchWhat3WordLocation(this)

                                //TODO: Send Email
                                BBSideEngine.getInstance(null).sendEmail("test@gmail.com", false) // Replace your emergency email address

                                //TODO: notify to partner
                                BBSideEngine.getInstance(null).notifyPartner()

                                BBSideEngine.getInstance(null).resumeSensorIfAppInBackground()

                            } else {
                                val intent = Intent(this, CustomUiActivity::class.java)
                                intent.putExtra("userName", "Testing User")
                                intent.putExtra("email", "test@gmail.com")
                                intent.putExtra("btnTestClicked", false)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                startActivity(intent)
                            }
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
            }
            Constants.BBSideOperation.INCIDENT_CANCEL -> {
                //User canceled countdown countdown to get event here, this called only for if you configured standard theme.
            }
            Constants.BBSideOperation.INCIDENT_AUTO_CANCEL -> {
                //Auto canceled countdown countdown to get event here, this called only for if you configured standard theme.
            }
            Constants.BBSideOperation.INCIDENT_ALERT_SENT ->{
                //Return the alert sent (returns alert details (i.e. time, location, recipient, success/failure))
            }
            Constants.BBSideOperation.TIMER_STARTED -> {
                //Countdown timer started after breach delay, this called only if you configured standard theme.
            }
            Constants.BBSideOperation.TIMER_FINISHED -> {
                //Countdown timer finished and jump to the incident summary page, this called only if you configured standard theme.
            }
            else -> {
                Log.e("No Events Find",":")
            }
        }
    }

    override fun onPause() {
        super.onPause()
        locationUpdateState = false
        fusedLocationProviderClient?.removeLocationUpdates(locationCallback)
    }
    private fun createLocationRequest() {
        locationRequest = LocationRequest()
        locationRequest.interval = 5000
        locationRequest.fastestInterval = 1000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        val client = LocationServices.getSettingsClient(this)
        val task = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            locationUpdateState = true
            startLocationUpdates()
        }
        task.addOnFailureListener { e ->
            if (e is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    e.startResolutionForResult(this@MapsActivity,
                        Constants.RequestCode.REQUEST_CHECK_SETTINGS)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION)
            return
        }
        val handlerThread = HandlerThread("MyHandlerThread")
        handlerThread.start()
        val looper = handlerThread.looper
        fusedLocationProviderClient?.requestLocationUpdates(locationRequest, locationCallback, looper)
    }


    override fun onResume() {
        super.onResume()
        if (!locationUpdateState) {
            startLocationUpdates()
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        bbSideEngine.stopSideEngine()
    }
}
