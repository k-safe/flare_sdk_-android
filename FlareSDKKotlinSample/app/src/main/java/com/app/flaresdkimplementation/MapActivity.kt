package com.app.flaresdkimplementation

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.app.flaresdkimplementation.databinding.ActivityMapBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.sos.busbysideengine.BBSideEngine
import com.sos.busbysideengine.IncidentTypeCallback
import com.sos.busbysideengine.rxjavaretrofit.network.model.BBSideEngineUIListener
import org.json.JSONObject

class MapActivity : AppCompatActivity(), BBSideEngineUIListener {

    private var latitude = 0.0
    private var longitude = 0.0
    private var word = ""
    private var mapUri = ""
    private val binding: ActivityMapBinding by lazy {
        ActivityMapBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initializeView()
    }

    private fun initializeView() {
        BBSideEngine.getInstance().setBBSideEngineListenerInLib(this)
        BBSideEngine.getInstance().fetchWhat3WordLocation(this@MapActivity)
        binding.tvCUIWord.setOnClickListener {
            if (mapUri != "") {
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse(mapUri)
                startActivity(i)
            }
        }
        binding.llCUIAlertTop.setOnClickListener {
            BBSideEngine.getInstance().launchIncidentClassification(this, object :
                IncidentTypeCallback<String> {
                override fun onSubmit(incidentType: String) {
                    Log.d("MS onSubmit:", incidentType)
                    BBSideEngine.getInstance().resumeSideEngine()
                    finish()
                }

                override fun onClose() {
                    Log.d("MS onClose:", "")
                    BBSideEngine.getInstance().resumeSideEngine()
                    finish()
                }
            })
        }
    }

    private fun setMap() {
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.fragment_map) as SupportMapFragment
        mapFragment.getMapAsync { googleMap ->
            val currentLoc = LatLng(latitude, longitude)
            googleMap.setMinZoomPreference(10.0f)
            googleMap.setMaxZoomPreference(20.0f)
            googleMap.addMarker(
                MarkerOptions()
                    .position(currentLoc)
                    .title("Current Location")
            )
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(currentLoc))
            googleMap.animateCamera(CameraUpdateFactory.newLatLng(currentLoc))
        }
    }

    override fun onIncidentAlertCallback(status: Boolean, response: JSONObject?) {
        if (response != null) {
            val mJSONObjectResult = response.getJSONObject("result")
            if (mJSONObjectResult.has("words")) {
                word = mJSONObjectResult.getString("words")
            }
            if (mJSONObjectResult.has("map")) {
                mapUri = mJSONObjectResult.getString("map")
            }
            binding.tvCUIWord.text = "//$word"
            if (mJSONObjectResult.has("latitude")) {
                latitude = mJSONObjectResult.getDouble("latitude")
            }
            if (mJSONObjectResult.has("longitude")) {
                longitude = mJSONObjectResult.getDouble("longitude")
            }
            binding.tvCUIlatlong.text = "Latitude: $latitude Longitude: $longitude"
            setMap()
        }
    }

    override fun onSendSMSCallback(status: Boolean, response: JSONObject?) {
        TODO("Not yet implemented")
    }

    override fun onSendEmailCallback(status: Boolean, response: JSONObject?) {
        TODO("Not yet implemented")
    }

    override fun onIncidentCancelCallback(status: Boolean, response: JSONObject?) {
        TODO("Not yet implemented")
    }

    override fun onIncidentAutoCancelCallback(status: Boolean, response: JSONObject?) {
        TODO("Not yet implemented")
    }

    override fun onIncidentVerifiedByUser(status: Boolean, response: JSONObject?) {
        TODO("Not yet implemented")
    }

    override fun onCloseSurveyVideoActivityCallback() {
        TODO("Not yet implemented")
    }
}