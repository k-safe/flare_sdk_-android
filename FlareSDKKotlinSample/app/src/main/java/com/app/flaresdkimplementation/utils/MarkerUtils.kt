package com.app.flaresdkimplementation.utils

import android.content.Context
import android.util.DisplayMetrics
import kotlin.math.roundToInt

object MarkerUtils {

    fun getMarkerSize(context: Context, baseSize: Float): Int {
        // Get the display metrics from the context
        val displayMetrics: DisplayMetrics = context.resources.displayMetrics
        
        // Get screen width, height, and pixel density
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels
        val density = displayMetrics.density

        // Calculate screen area (width x height)
        val screenArea = screenWidth * screenHeight

        // Example logic for scaling marker size based on screen area
        return when {
            screenArea <= 480 * 800 -> (baseSize * 0.75f).roundToInt()  // Low resolution (small phones)
            screenArea <= 1080 * 1920 -> (baseSize * 1.0f).roundToInt()  // Medium resolution (standard phones)
            screenArea <= 1440 * 2560 -> (baseSize * 1.25f).roundToInt() // High resolution (large phones)
            else -> (baseSize * 1.5f).roundToInt()  // Extra-high resolution (tablets, phablets)
        }
    }
}
