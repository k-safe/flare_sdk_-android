package com.flare.sdk.android.bottomsheets

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.flare.sdk.android.R
import com.flare.sdk.android.interfaces.OnBottomSheetDismissListener
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.flaresafety.sideengine.BBSideEngine
class CustomUIBottomSheet : BottomSheetDialogFragment() {

    private lateinit var tvCountDown: TextView
    private lateinit var llYesIncident: LinearLayout
    private lateinit var llNoIncident: LinearLayout
    private var vibrator: Vibrator? = null
    private var countDownTimer: CountDownTimer? = null
    private var dismissListener: OnBottomSheetDismissListener? = null
    private var showMap = false

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
    ): View {
        val view = inflater.inflate(R.layout.dialog_countdown, container, false)
        initializeView(view)
        setListener()
        return view
    }

    private fun initializeView(view: View) {
        tvCountDown = view.findViewById(R.id.tvCountDown)
        llYesIncident = view.findViewById(R.id.llYesIncident)
        llNoIncident = view.findViewById(R.id.llNoIncident)
    }

    private fun setListener() {
        startVibrate()

        llYesIncident.setOnClickListener {
            showMap = true
            if (countDownTimer != null) {
                countDownTimer!!.cancel()
            }
            stopVibrate()
            BBSideEngine.getInstance().confirmIncident(requireActivity())
         //   BBSideEngine.getInstance().resumeSideEngine()
            dismissListener?.onReportAnIncident()
            dialog?.dismiss()
        }

        llNoIncident.setOnClickListener {
            if (countDownTimer != null) {
                countDownTimer!!.cancel()
            }
            stopVibrate()
            BBSideEngine.getInstance().incidentDecline()
            BBSideEngine.getInstance().resumeSideEngine()
            dialog?.dismiss()
        }

        countDownTimer = object : CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                tvCountDown.text = (millisUntilFinished / 1000).toString()
            }

            @SuppressLint("SetTextI18n")
            override fun onFinish() {
                stopVibrate()
                BBSideEngine.getInstance().resumeSideEngine()
                dialog?.dismiss()
            }
        }.start()
    }

    private fun startVibrate() {
        val pattern = longArrayOf(0, 100, 1000, 1500, 2000, 2500, 3000)
        vibrator = requireActivity().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator?
        vibrator!!.vibrate(pattern, 0)
    }

    private fun stopVibrate() {
        if (vibrator != null) {
            vibrator!!.cancel()
        }
    }

    override fun onDetach() {
        super.onDetach()
        dismissListener = null
    }

    override fun onDestroy() {
        super.onDestroy()
        stopVibrate()
    }
}