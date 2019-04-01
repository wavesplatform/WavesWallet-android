/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data.analytics.providers

import com.wavesplatform.wallet.v2.data.analytics.ProviderType
import timber.log.Timber

class LoggerProvider : ProviderType {

    override fun log(eventName: String, parameters: HashMap<String, Any>) {
        Timber.tag(TAG).i("Tracking event '$eventName' ${logParameters(parameters)}")
    }

    private fun logParameters(parameters: HashMap<String, Any>): String {
        return if (parameters.isEmpty()) {
            ""
        } else {
            val args = parameters.entries
                    .map {
                        return@map "${it.key}=${it.value}"
                    }
                    .joinToString(", ")
            "with args($args)"
        }
    }

    companion object {
        const val TAG = "LoggerProvider"
    }
}