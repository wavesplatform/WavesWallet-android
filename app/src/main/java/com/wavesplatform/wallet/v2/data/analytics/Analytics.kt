package com.wavesplatform.wallet.v2.data.analytics

var analytics = Analytics.instance

interface EventType {
    fun provideName(provider: ProviderType): String?
    fun provideParameters(provider: ProviderType): HashMap<String, Any>?
}

interface AnalyticsType {
    fun register(provider: ProviderType)
    fun log(event: EventType)
}

interface ProviderType {
    fun init()
    fun log(eventName: String, parameters: HashMap<String, Any>?)
}

class Analytics : AnalyticsType {
    private var providers = arrayListOf<ProviderType>()

    override fun register(provider: ProviderType) {
        this.providers.add(provider)
    }

    override fun log(event: EventType) {
        this.providers.forEach { provider ->
            event.provideName(provider)?.let { name ->
                val parameters = event.provideParameters(provider)
                provider.log(name, parameters)
            }
        }
    }

    companion object {
        var instance = Analytics()
    }
}