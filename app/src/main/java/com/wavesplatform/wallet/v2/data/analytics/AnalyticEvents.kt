package com.wavesplatform.wallet.v2.data.analytics

sealed class AnalyticEvents(private var eventName: String) : EventType {

    object LeasingStartTapEvent : AnalyticEvents("Leasing Start Tap")
    object LeasingSendTapEvent : AnalyticEvents("Leasing Send Tap")
    object LeasingConfirmTapEvent : AnalyticEvents("Leasing Confirm Tap")
    object AliasCreateProfileEvent : AnalyticEvents("Alias Create Profile")
    object AliasCreateVcardEvent : AnalyticEvents("Alias Create Vcard")
    class DEXBuyTapEvent(amountName: String, priceName: String) : DexEvent("DEX Buy Tap", amountName, priceName)
    class DEXBuyOrderSuccessEvent(amountName: String, priceName: String) : DexEvent("DEX Buy Order Success", amountName, priceName)
    class DEXSellTapEvent(amountName: String, priceName: String) : DexEvent("DEX Sell Tap", amountName, priceName)
    class DEXSellOrderSuccessEvent(amountName: String, priceName: String) : DexEvent("DEX Sell Order Success", amountName, priceName)
    class WalletAssetsSendTapEvent(assetName: String) : CurrencyEvent("Wallet Assets Send Tap", assetName)
    class WalletAssetsSendConfirmEvent(assetName: String) : CurrencyEvent("Wallet Assets Send Confirm", assetName)
    class WalletAssetsReceiveTapEvent(assetName: String) : CurrencyEvent("Wallet Assets Receive Tap", assetName)
    class WalletAssetsReceiveConfirmEvent(assetName: String) : CurrencyEvent("Wallet Assets Receive Confirm", assetName)
    object WalletAssetsCardReceiveTapEvent : AnalyticEvents("Wallet Assets Card Receive Tap")
    object BurnTokenTapEvent : AnalyticEvents("Burn Token Tap")
    object BurnTokenContinueTapEvent : AnalyticEvents("Burn Token Continue Tap")
    object BurnTokenConfirmTapEvent : AnalyticEvents("Burn Token Confirm Tap")
    class WalletStartBalanceFromZeroEvent(assetName: String) : CurrencyEvent("Wallet Start Balance from Zero", assetName)
    object NewUserConfirmEvent : AnalyticEvents("New User Confirm")

    override fun provideName(provider: ProviderType): String? {
        // rewrite eventName if need for different analytics with when(this) conditions
        return this.eventName
    }

    override fun provideParameters(provider: ProviderType): HashMap<String, Any>? {
        return when (this) {
            is DexEvent -> {
                hashMapOf(PAIR_KEY to "$amountAssetName/$priceAssetName")
            }
            is CurrencyEvent -> {
                hashMapOf(CURRENCY_KEY to currency)
            }
            else -> {
                hashMapOf()
            }
        }
    }

    abstract class DexEvent(eventName: String, var amountAssetName: String, var priceAssetName: String) : AnalyticEvents(eventName)
    abstract class CurrencyEvent(eventName: String, var currency: String) : AnalyticEvents(eventName)

    companion object {
        const val PAIR_KEY = "Pair"
        const val CURRENCY_KEY = "Currency"
    }
}