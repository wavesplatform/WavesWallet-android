package com.wavesplatform.wallet.v2.data.analytics

import android.content.Context
import com.wavesplatform.wallet.BuildConfig
import com.wavesplatform.wallet.v2.data.analytics.providers.AmplitudeProvider
import com.wavesplatform.wallet.v2.data.analytics.providers.AppsFlyerProvider
import com.wavesplatform.wallet.v2.data.analytics.providers.FirebaseProvider
import com.wavesplatform.wallet.v2.data.analytics.providers.LoggerProvider

var analytics = Analytics.instance

interface EventType {
    fun provideName(provider: ProviderType): String?
    fun provideParameters(provider: ProviderType): HashMap<String, Any>
}

interface AnalyticsType {
    fun register(provider: ProviderType)
    fun trackEvent(event: EventType)
}

interface ProviderType {
    fun log(eventName: String, parameters: HashMap<String, Any>)
}

class Analytics : AnalyticsType {
    private var providers = arrayListOf<ProviderType>()

    override fun register(provider: ProviderType) {
        this.providers.add(provider)
    }

    override fun trackEvent(event: EventType) {
        this.providers.forEach { provider ->
            event.provideName(provider)?.let { name ->
                val parameters = event.provideParameters(provider)
                provider.log(name, parameters)
            }
        }
    }

    companion object {
        var instance = Analytics()

        @JvmStatic
        fun init(context: Context) {
            instance.register(LoggerProvider())
            instance.register(FirebaseProvider(context))
            instance.register(AppsFlyerProvider(context, BuildConfig.APPS_FLYER_KEY))
            instance.register(AmplitudeProvider(context, BuildConfig.AMPLITUDE_API_KEY))
        }
    }
}