package com.wavesplatform.wallet.v2.util

import android.app.Application
import android.os.Bundle
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import com.google.firebase.analytics.FirebaseAnalytics
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.BuildConfig
import pyxis.uzuki.live.richutilskt.utils.toMap

class Analytics {

    companion object {

        @JvmStatic
        fun appsFlyerInit(app: Application) {
            val conversionDataListener = object : AppsFlyerConversionListener {

                override fun onInstallConversionDataLoaded(conversionData: Map<String, String>) {
                }

                override fun onInstallConversionFailure(errorMessage: String) {
                }

                override fun onAppOpenAttribution(attributionData: Map<String, String>) {
                }

                override fun onAttributionFailure(errorMessage: String) {
                }
            }
            AppsFlyerLib.getInstance().init(BuildConfig.APPS_FLYER_KEY, conversionDataListener,
                    app.applicationContext)
            AppsFlyerLib.getInstance().startTracking(app)
        }

        fun sendEvent(
            firebaseAnalytics: FirebaseAnalytics,
            eventName: String,
            eventBundle: Bundle
        ) {
            AppsFlyerLib.getInstance().trackEvent(App.getAppContext(), eventName, eventBundle.toMap())
            firebaseAnalytics.logEvent(eventName, eventBundle)
        }
    }
}