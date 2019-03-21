package com.wavesplatform.wallet.v2.data.analytics.providers

import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics
import com.wavesplatform.wallet.v2.data.analytics.ProviderType
import com.wavesplatform.wallet.v2.util.toBundle
import timber.log.Timber

class FirebaseProvider(private val context: Context) : ProviderType {
    override fun init() {
        // nothing
    }

    override fun log(eventName: String, parameters: HashMap<String, Any>?) {
        try {
            FirebaseAnalytics.getInstance(context).logEvent(eventName, parameters?.toBundle())
        } catch (e: Exception) {
            Timber.d(e)
        }
    }
}