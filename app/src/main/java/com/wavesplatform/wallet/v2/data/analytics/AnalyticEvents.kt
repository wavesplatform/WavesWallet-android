package com.wavesplatform.wallet.v2.data.analytics

sealed class AnalyticEvents : EventType {

    object Example : AnalyticEvents()

    override fun provideName(provider: ProviderType): String? {
        when (this) {
            is Example -> return "example"
        }
    }

    override fun provideParameters(provider: ProviderType): HashMap<String, Any>? {
        return when (this) {
            is Example -> {
                hashMapOf()
            }
            else -> {
                hashMapOf()
            }
        }
    }
}