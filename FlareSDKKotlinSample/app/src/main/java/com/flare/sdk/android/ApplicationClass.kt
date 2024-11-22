package com.flare.sdk.android

import android.app.Application
import androidx.multidex.MultiDexApplication
import com.flaresafety.sideengine.BBSideEngine

open class ApplicationClass : Application() {
    override fun onCreate() {
        super.onCreate()
//        BBSideEngine.init(this)
    }
}