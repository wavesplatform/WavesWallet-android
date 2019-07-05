/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data.analytics.providers

import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics
import com.wavesplatform.wallet.v2.data.analytics.ProviderType
import com.wavesplatform.wallet.v2.util.toBundle
import timber.log.Timber

class FirebaseProvider(private val context: Context) : ProviderType {

    override fun log(eventName: String, parameters: HashMap<String, Any>) {
        try {
            val event = eventName.replace(" ", "_") // need to replace space to underscore for Firebase
            FirebaseAnalytics.getInstance(context).logEvent(event, parameters.toBundle())
        } catch (e: Exception) {
            Timber.d(e)
        }
    }

}