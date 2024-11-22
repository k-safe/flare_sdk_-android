package com.flare.sdk.android.bottomsheets

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.flare.sdk.android.R
import com.flare.sdk.android.interfaces.OnBottomSheetDismissListener
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SelectActivityBottomSheet: BottomSheetDialogFragment() {

    private var selectedActivityType = "Bike"
    private var dismissListener: OnBottomSheetDismissListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnBottomSheetDismissListener) {
            dismissListener = context
        } else {
            throw RuntimeException("$context must implement OnBottomSheetDismissListener")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_activity, container, false)
        initializeView(view)
        return view
    }

    private fun initializeView(view: View) {
        val llMultiActivity = view.findViewById<LinearLayout>(R.id.llMultiActivity)
        val llCycling = view.findViewById<LinearLayout>(R.id.llCycling)
        val llDriving = view.findViewById<LinearLayout>(R.id.llDriving)
        val llBike = view.findViewById<LinearLayout>(R.id.llEBike)
        val llHiking = view.findViewById<LinearLayout>(R.id.llHiking)
        val llHorseRiding = view.findViewById<LinearLayout>(R.id.llHorseRiding)
        val llMotorbiking = view.findViewById<LinearLayout>(R.id.llMotorbiking)
        val llRunning = view.findViewById<LinearLayout>(R.id.llRunning)
        val llScooter = view.findViewById<LinearLayout>(R.id.llScooter)
        val llSkiing = view.findViewById<LinearLayout>(R.id.llSkiing)
        val llWalking = view.findViewById<LinearLayout>(R.id.llWalking)
        val llCancel = view.findViewById<LinearLayout>(R.id.llCancel)

        llMultiActivity.visibility = View.GONE
        llCycling.visibility = View.GONE
        llDriving.visibility = View.GONE
        llHiking.visibility = View.GONE
        llHorseRiding.visibility = View.GONE
        llBike.visibility = View.GONE
        llRunning.visibility = View.GONE
        llSkiing.visibility = View.GONE
        llWalking.visibility = View.GONE

        val clickableViews = listOf(
            llMultiActivity, llCycling, llDriving, llBike,
            llHiking, llHorseRiding, llMotorbiking, llRunning, llSkiing, llWalking,
            llScooter
        )

        clickableViews.forEach { it.setOnClickListener(::manageClickEvent) }

        llCancel.setOnClickListener {
            dialog?.dismiss()
        }
    }

    private fun manageClickEvent(view: View) {
        val activityType = when (view.id) {
            R.id.llMultiActivity -> "Multi-Activity"
            R.id.llCycling -> "Cycling"
            R.id.llDriving -> "Driving"
            R.id.llEBike -> "eBike"
            R.id.llHiking -> "Hiking"
            R.id.llHorseRiding -> "Horse Riding"
            R.id.llMotorbiking -> "Bike"
            R.id.llRunning -> "Running"
            R.id.llScooter -> "Scooter"
            R.id.llSkiing -> "Skiing"
            R.id.llWalking -> "Walking"
            else -> return
        }
        selectedActivityType = activityType
        dismissListener?.onActivitySelected(activityType)
        dialog?.dismiss()
    }
}