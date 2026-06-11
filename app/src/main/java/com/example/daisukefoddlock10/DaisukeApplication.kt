package com.example.daisukefoddlock10

import android.app.Application
import com.midtrans.sdk.uikit.api.model.CustomColorTheme
import com.midtrans.sdk.uikit.external.SdkUIFlowBuilder
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class DaisukeApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initMidtransSdk()
    }

    private fun initMidtransSdk() {
        SdkUIFlowBuilder.init()
            .setClientKey("Mid-client-RpymohopFHkKIfR3") // Client Key Anda
            .setContext(applicationContext)
            .setTransactionFinishedCallback { result ->
                /* Handle transaction result here if needed globally */
            }
            .setMerchantBaseUrl("http://10.0.2.2:8080/") // URL Backend Anda (Emulator localhost)
            .enableLog(true)
            .setColorTheme(CustomColorTheme("#FF5722", "#E64A19", "#FFCCBC"))
            .buildSDK()
    }
}
