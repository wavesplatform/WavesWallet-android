package com.wavesplatform.wallet.v2.data.analytics.providers

import com.wavesplatform.wallet.v2.data.analytics.ProviderType
import timber.log.Timber

class LoggerProvider : ProviderType {

    override fun log(eventName: String, parameters: HashMap<String, Any>?) {
        Timber.tag(TAG).i("Tracking event $eventName")
    }

    companion object {
        const val TAG = "LoggerProvider"
    }
}