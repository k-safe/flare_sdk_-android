package com.flare.sdk.android.bottomsheets;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.flare.sdk.android.R;
import com.flare.sdk.android.interfaces.OnBottomSheetDismissListener;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.flaresafety.sideengine.BBSideEngine;

public class CustomUIBottomSheet extends BottomSheetDialogFragment {

    private TextView tvCountDown;
    private LinearLayout llYesIncident;
    private LinearLayout llNoIncident;
    private Vibrator vibrator;
    private CountDownTimer countDownTimer;
    private OnBottomSheetDismissListener dismissListener;
    private boolean showMap = false;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnBottomSheetDismissListener) {
            dismissListener = (OnBottomSheetDismissListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnBottomSheetDismissListener");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialogTheme);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_countdown, container, false);
        initializeView(view);
        setListener();
        return view;
    }

    private void initializeView(View view) {
        tvCountDown = view.findViewById(R.id.tvCountDown);
        llYesIncident = view.findViewById(R.id.llYesIncident);
        llNoIncident = view.findViewById(R.id.llNoIncident);
    }

    private void setListener() {
        startVibrate();

        llYesIncident.setOnClickListener(v -> {
            showMap = true;
            if (countDownTimer != null) {
                countDownTimer.cancel();
            }
            stopVibrate();
            BBSideEngine.getInstance().confirmIncident(requireActivity());
            if (dismissListener != null) {
                dismissListener.onReportAnIncident();
            }
            dismiss();
        });

        llNoIncident.setOnClickListener(v -> {
            if (countDownTimer != null) {
                countDownTimer.cancel();
            }
            stopVibrate();
            BBSideEngine.getInstance().incidentDecline();
            BBSideEngine.getInstance().resumeSideEngine();
            dismiss();
        });

        countDownTimer = new CountDownTimer(30000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tvCountDown.setText(String.valueOf(millisUntilFinished / 1000));
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onFinish() {
                stopVibrate();
                BBSideEngine.getInstance().resumeSideEngine();
                dismiss();
            }
        }.start();
    }

    private void startVibrate() {
        long[] pattern = {0, 100, 1000, 1500, 2000, 2500, 3000};
        vibrator = (Vibrator) requireActivity().getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null) {
            vibrator.vibrate(pattern, 0);
        }
    }

    private void stopVibrate() {
        if (vibrator != null) {
            vibrator.cancel();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        dismissListener = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopVibrate();
    }
}
