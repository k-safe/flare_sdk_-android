package com.flare.sdk.android.utils;

import android.content.Context;
import android.util.DisplayMetrics;

public class MarkerUtils {

    public static int getMarkerSize(Context context, float baseSize) {
        // Get the display metrics from the context
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();

        // Get screen width, height, and pixel density
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;

        // Calculate screen area (width x height)
        int screenArea = screenWidth * screenHeight;

        // Example logic for scaling marker size based on screen area
        if (screenArea <= 480 * 800) {
            return Math.round(baseSize * 0.75f); // Low resolution (small phones)
        } else if (screenArea <= 1080 * 1920) {
            return Math.round(baseSize * 1.0f); // Medium resolution (standard phones)
        } else if (screenArea <= 1440 * 2560) {
            return Math.round(baseSize * 1.25f); // High resolution (large phones)
        } else {
            return Math.round(baseSize * 1.5f); // Extra-high resolution (tablets, phablets)
        }
    }
}
