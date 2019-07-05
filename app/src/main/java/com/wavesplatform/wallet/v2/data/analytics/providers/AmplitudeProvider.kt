/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data.analytics.providers

import android.content.Context
import com.amplitude.api.Amplitude
import com.appsflyer.AppsFlyerLib
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.gson.JsonObject
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.BuildConfig
import com.wavesplatform.wallet.v2.data.analytics.ProviderType
import com.wavesplatform.wallet.v2.util.toBundle
import org.json.JSONObject
import pers.victor.ext.app
import timber.log.Timber

class AmplitudeProvider(context: Context, key: String) : ProviderType {

    init {
        Amplitude.getInstance()
                .initialize(context, key)
                .enableForegroundTracking(app)
    }

    override fun log(eventName: String, parameters: HashMap<String, Any>) {
        try {
            Amplitude.getInstance().logEvent(eventName, JSONObject(parameters))
        } catch (e: Exception) {
            Timber.d(e)
        }
    }
}