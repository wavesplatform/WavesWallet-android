package com.wavesplatform.wallet.v2.data.analytics

sealed class AnalyticEvents(private var eventName: String) : EventType {

    // Нажата кнопка «Start Lease» на экране Wallet.
    object LeasingStartTapEvent : AnalyticEvents("Leasing Start Tap")
    // Нажата кнопка «Start Lease» на экране с заполненными полями.
    object LeasingSendTapEvent : AnalyticEvents("Leasing Send Tap")
    // Нажата кнопка «Confirm» на экране подтверждения лизинга.
    object LeasingConfirmTapEvent : AnalyticEvents("Leasing Confirm Tap")
    // Нажата кнопка «Create a new alias» на экране профайла.
    object AliasCreateProfileEvent : AnalyticEvents("Alias Create Profile")
    // Нажата кнопка «Create a new alias» на экране визитки.
    object AliasCreateVcardEvent : AnalyticEvents("Alias Create Vcard")
    // Нажата кнопка «Buy» на экране просмотра пары.
    class DEXBuyTapEvent(amountName: String, priceName: String) : DexEvent("DEX Buy Tap", amountName, priceName)
    // Нажата кнопка «Okay» на экране созданного ордера.
    class DEXBuyOrderSuccessEvent(amountName: String, priceName: String) : DexEvent("DEX Buy Order Success", amountName, priceName)
    // Нажата кнопка «Sell» на экране просмотра пары.
    class DEXSellTapEvent(amountName: String, priceName: String) : DexEvent("DEX Sell Tap", amountName, priceName)
    // Нажата кнопка «Okay» на экране созданного ордера.
    class DEXSellOrderSuccessEvent(amountName: String, priceName: String) : DexEvent("DEX Sell Order Success", amountName, priceName)
    // Нажата кнопка «Continue» у любой криптовалюты или токена.
    class WalletAssetsSendTapEvent(assetName: String) : CurrencyEvent("Wallet Assets Send Tap", assetName)
    // Нажата кнопка «Confirm» у любой криптовалюты или токена.
    class WalletAssetsSendConfirmEvent(assetName: String) : CurrencyEvent("Wallet Assets Send Confirm", assetName)
    // Нажата кнопка «Continue» у любой криптовалюты или токена.
    class WalletAssetsReceiveTapEvent(assetName: String) : CurrencyEvent("Wallet Assets Receive Tap", assetName)
    // Нажата кнопка «Continue» на экране с заполненными полями карты.
    object WalletAssetsCardReceiveTapEvent : AnalyticEvents("Wallet Assets Card Receive Tap")
    // Нажата кнопка «Token Burn» на экране ассета.
    object BurnTokenTapEvent : AnalyticEvents("Burn Token Tap")
    // Нажата кнопка «Burn» на экране с заполненными полями.
    object BurnTokenContinueTapEvent : AnalyticEvents("Burn Token Continue Tap")
    // Нажата кнопка «Burn» на экране подтверждения.
    object BurnTokenConfirmTapEvent : AnalyticEvents("Burn Token Confirm Tap")
    // Необходимо запоминать нулевые балансы для нашего general листа и при пополнении ассета любым способом отправлять событие
    class WalletStartBalanceFromZeroEvent(assetName: String) : CurrencyEvent("Wallet Start Balance from Zero", assetName)
    // Проставлены 3 чекбокса с условиями использования и нажата кнопка "Begin".
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