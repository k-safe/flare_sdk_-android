package com.flare.sdk.android.model

data class Hazard(
    val partnerId: String,
    val userId: String,
    val hazardId: String,
    val lat: Double,
    val lon: Double,
    val name: String,
    val iconDrawableName: String
)