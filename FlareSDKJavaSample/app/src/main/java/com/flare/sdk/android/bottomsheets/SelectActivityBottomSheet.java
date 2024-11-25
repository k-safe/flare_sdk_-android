package com.flare.sdk.android.bottomsheets;



import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.flare.sdk.android.R;
import com.flare.sdk.android.interfaces.OnBottomSheetDismissListener;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;

public class SelectActivityBottomSheet extends BottomSheetDialogFragment {

    public static final int llCycling = com.flare.sdk.android.R.id.llCycling;
    public static final int llDriving = com.flare.sdk.android.R.id.llDriving;
    public static final int llEBike = com.flare.sdk.android.R.id.llEBike;
    public static final int llHiking =  com.flare.sdk.android.R.id.llHiking;
    public static final int llHorseRiding = com.flare.sdk.android.R.id.llHorseRiding;
    public static final int llMotorbiking = com.flare.sdk.android.R.id.llMotorbiking;
    final static int llMultiActivity = com.flare.sdk.android.R.id.llMultiActivity;
    public static final int llRunning = com.flare.sdk.android.R.id.llRunning;
    public static final int llScooter = com.flare.sdk.android.R.id.llScooter;
    public static final int llSkiing = com.flare.sdk.android.R.id.llSkiing;
    public static final int llWalking = com.flare.sdk.android.R.id.llWalking;

    private String selectedActivityType = "Bike";
    private OnBottomSheetDismissListener dismissListener;

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
        View view = inflater.inflate(R.layout.dialog_activity, container, false);
        initializeView(view);
        return view;
    }

    private void initializeView(View view) {
        LinearLayout llMultiActivity = view.findViewById(R.id.llMultiActivity);
        LinearLayout llCycling = view.findViewById(R.id.llCycling);
        LinearLayout llDriving = view.findViewById(R.id.llDriving);
        LinearLayout llBike = view.findViewById(llEBike);
        LinearLayout llHiking = view.findViewById(R.id.llHiking);
        LinearLayout llHorseRiding = view.findViewById(R.id.llHorseRiding);
        LinearLayout llMotorbiking = view.findViewById(R.id.llMotorbiking);
        LinearLayout llRunning = view.findViewById(R.id.llRunning);
        LinearLayout llScooter = view.findViewById(R.id.llScooter);
        LinearLayout llSkiing = view.findViewById(R.id.llSkiing);
        LinearLayout llWalking = view.findViewById(R.id.llWalking);
        LinearLayout llCancel = view.findViewById(R.id.llCancel);

        // Set visibility to GONE
        llMultiActivity.setVisibility(View.GONE);
        llCycling.setVisibility(View.GONE);
        llDriving.setVisibility(View.GONE);
        llHiking.setVisibility(View.GONE);
        llHorseRiding.setVisibility(View.GONE);
        llBike.setVisibility(View.GONE);
        llRunning.setVisibility(View.GONE);
        llSkiing.setVisibility(View.GONE);
        llWalking.setVisibility(View.GONE);

        // Create a list of clickable views
        List<LinearLayout> clickableViews = new ArrayList<>();
        clickableViews.add(llMultiActivity);
        clickableViews.add(llCycling);
        clickableViews.add(llDriving);
        clickableViews.add(llBike);
        clickableViews.add(llHiking);
        clickableViews.add(llHorseRiding);
        clickableViews.add(llMotorbiking);
        clickableViews.add(llRunning);
        clickableViews.add(llScooter);
        clickableViews.add(llSkiing);
        clickableViews.add(llWalking);

        // Set click listeners
        for (LinearLayout clickableView : clickableViews) {
            clickableView.setOnClickListener(this::manageClickEvent);
        }

        llCancel.setOnClickListener(v -> dismiss());
    }

    private void manageClickEvent(View view) {
        String activityType;
        int id = view.getId();
        if (id == llMultiActivity) {
            activityType = "Multi-Activity";
        } else if (id == llCycling) {
            activityType = "Cycling";
        } else if (id == llDriving) {
            activityType = "Driving";
        } else if (id == llEBike) {
            activityType = "eBike";
        } else if (id == llHiking) {
            activityType = "Hiking";
        } else if (id == llHorseRiding) {
            activityType = "Horse Riding";
        } else if (id == llMotorbiking) {
            activityType = "Bike";
        } else if (id == llRunning) {
            activityType = "Running";
        } else if (id == llScooter) {
            activityType = "Scooter";
        } else if (id == llSkiing) {
            activityType = "Skiing";
        } else if (id == llWalking) {
            activityType = "Walking";
        } else {
            return;
        }
        selectedActivityType = activityType;
        if (dismissListener != null) {
            dismissListener.onActivitySelected(activityType);
        }
        dismiss();
    }
}
