package com.wavesplatform.wallet.v2.data.analytics.providers

import android.content.Context
import com.appsflyer.AppsFlyerLib
import com.google.firebase.analytics.FirebaseAnalytics
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.BuildConfig
import com.wavesplatform.wallet.v2.data.analytics.ProviderType
import com.wavesplatform.wallet.v2.util.toBundle
import pers.victor.ext.app
import timber.log.Timber

class AppsFlyerProvider(private val context: Context, key: String) : ProviderType {

    init {
        AppsFlyerLib.getInstance().init(key, null, context)
        AppsFlyerLib.getInstance().startTracking(app)
    }

    override fun log(eventName: String, parameters: HashMap<String, Any>?) {
        try {
            AppsFlyerLib.getInstance().trackEvent(context, eventName, parameters)
        } catch (e: Exception) {
            Timber.d(e)
        }
    }
}