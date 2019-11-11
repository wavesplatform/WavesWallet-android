/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data.analytics

import com.wavesplatform.wallet.v2.util.WavesWallet

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

    // Нажат баннер про необходимость обновления на экране Wallet
    object WalletUpdateBannerEvent : AnalyticEvents("Wallet Update Banner")

    // Нажат кнопка "Search" на экране Wallet
    object WalletTokenSearchEvent : AnalyticEvents("Wallet Token Search")

    // Нажата кнопка перехода в сортировку на экране Wallet
    object WalletTokenSortingPageEvent : AnalyticEvents("Wallet Token Sorting Page")

    // Нажата кнопка "Position" на экране Sorting
    object WalletTokenSortingPositionEvent : AnalyticEvents("Wallet Token Sorting Position")

    // Нажата кнопка "Visability" на экране Sorting
    object WalletTokenSortingVisabilityEvent : AnalyticEvents("Wallet Token Sorting Visability")

    // Нажата кнопка "qr" (переход на стриницу с адресом) экране Wallet
    object WalletQRCardEvent : AnalyticEvents("Wallet QRCard")

    // Нажата кнопка "Waves" (синий ромб) на экране Wallet
    object WavesActionPanelEvent : AnalyticEvents("Waves Action Panel")

    // Нажата кнопка "Send" на экшен панеле
    object WavesActionSendEvent : AnalyticEvents("Waves Action Send")

    // Нажата кнопка "Receive" на экшен панеле
    object WavesActionReceiveEvent : AnalyticEvents("Waves Action Receive")

    // Нажата кнопка "Save address" на детализации транзакции
    object TransactionAddressSaveEvent : AnalyticEvents("Transaction Address Save")

    // Нажата кнопка "Edit name" на детализации транзакции
    object TransactionAddressEditEvent : AnalyticEvents("Transaction Address Edit")

    // Нажата кнопка "Address book" на экране Profile
    object ProfileAddressBookPageEvent : AnalyticEvents("Profile Address Book Page")

    // Нажата кнопка "Add" на экране Address book
    object ProfileAddressBookAddEvent : AnalyticEvents("Profile Address Book Add")

    // Нажата кнопка "Edit" на экране Address book
    object ProfileAddressBookEditEvent : AnalyticEvents("Profile Address Book Edit")

    // Нажата кнопка "Delete" на алерте после "Delete address" на экране Edit Address
    object ProfileAddressBookDeleteEvent : AnalyticEvents("Profile Address Book Delete")

    // Нажата кнопка "Address, key" на экране Profile
    object ProfileAddressAndKeysEvent : AnalyticEvents("Profile Address And Keys")

    // Нажата кнопка "Language" на экране Profile
    object ProfileLanguageEvent : AnalyticEvents("Profile Language")

    // Нажата кнопка "Backup phrase" на экране Profile
    object ProfileBackupPhraseEvent : AnalyticEvents("Profile Backup Phrase")

    // Нажата кнопка "Change password" на экране Profile
    object ProfileChangePasswordEvent : AnalyticEvents("Profile Change Password")

    // Нажата кнопка "Change passcode" на экране Profile
    object ProfileChangePasscodeEvent : AnalyticEvents("Profile Change Passcode")

    // Нажата кнопка "Network" на экране Profile
    object ProfileNetworkEvent : AnalyticEvents("Profile Network")

    // Нажата кнопка "Rate app" на экране Profile
    object ProfileRateAppEvent : AnalyticEvents("Profile Rate App")

    // Нажата кнопка "Feedback" на экране Profile
    object ProfileFeedbackEvent : AnalyticEvents("Profile Feedback")

    // Нажата кнопка "Support Wavesplatform" на экране Profile
    object ProfileSupportEvent : AnalyticEvents("Profile Support")

    // Нажата кнопка "Delete" на алерте после "Delete account from device" на экране Profile
    object ProfileDeleteAccountEvent : AnalyticEvents("Profile Delete Account")

    // Нажата кнопка "logout" в Navigation bar на экране Profile
    object ProfileLogoutUpEvent : AnalyticEvents("Profile Logout Up")

    // Нажата кнопка "Logout of account" на экране Profile
    object ProfileLogoutDownEvent : AnalyticEvents("Profile Logout Down")

    // Отлавливаем событие что меню было открыто (или по кнопке или свайпом)
    object WavesMenuPageEvent : AnalyticEvents("Waves Menu Page")

    // Нажата ссылка "forum.wavesplatform.com" на экране Menu
    object WavesMenuForumEvent : AnalyticEvents("Waves Menu Forum")

    // Нажата ссылка "Whitepaper" на экране Menu
    object WavesMenuWhitepaperEvent : AnalyticEvents("Waves Menu Whitepaper")

    // Нажата ссылка "Terms and conditions" на экране Menu
    object WavesMenuTermsAndConditionsEvent : AnalyticEvents("Waves Menu Terms And Conditions")

    // Нажата ссылка "Support Wavesplatform" на экране Menu
    object WavesMenuSupportEvent : AnalyticEvents("Waves Menu Support")

    // Нажата ссылка "Github" на экране Menu
    object WavesMenuGithubEvent : AnalyticEvents("Waves Menu Github")

    // Нажата ссылка "Telegram" на экране Menu
    object WavesMenuTelegramEvent : AnalyticEvents("Waves Menu Telegram")

    // Нажата ссылка "Discord" на экране Menu
    object WavesMenuDiscordEvent : AnalyticEvents("Waves Menu Discord")

    // Нажата ссылка "Twitter" на экране Menu
    object WavesMenuTwitterEvent : AnalyticEvents("Waves Menu Twitter")

    // Нажата ссылка "Reddit" на экране Menu
    object WavesMenuRedditEvent : AnalyticEvents("Waves Menu Reddit")

    // Нажата кнопка "Scan pairing code" на экране Import account > Scan
    object StartImportScanEvent : AnalyticEvents("Start Import Scan")

    // Нажата кнопка "Continue" на экране Import account > Manually
    object StartImportManuallyEvent : AnalyticEvents("Start Import Manually")

    // Нажата кнопка "Edit" (карандаш) на экране Choose account
    object StartAccountEditEvent : AnalyticEvents("Start Account Edit")

    // Нажата кнопка "Delete" на алерте после "Delete" (корзина) на экране Choose account
    object StartAccountDeleteEvent : AnalyticEvents("Start Account Delete")

    // При появлении у пользователя сообщения "Нужно забэкапить SEED"
    class NewUserWithoutBackup(count: Int) : CountEvent("New User Without Backup", count)

    // Считаем количество сохраненных аккаунтов у пользователя
    class StartAccountCounter(count: Int) : CountEvent("Start Account Counter", count)

    // Добавление первого AppWidget с курсами асетов
    object MarketPulseAddedEvent : AnalyticEvents("Market Pulse Added")

    // Удаление последнего AppWidget с курсами асетов
    object MarketPulseRemovedEvent : AnalyticEvents("Market Pulse Removed")

    // Изменение настроек AppWidget с курсами асетов
    class MarketPulseSettingsChangedEvent(var style: String,
                                          var interval: String,
                                          var assets: List<String>) : AnalyticEvents("Market Pulse Settings Changed")

    // Любая активность AppWidget с курсами асетов, нажата кнопка "update" или "смена курса валюты" или "настройки"
    object MarketPulseActiveEvent : AnalyticEvents("Market Pulse Active")

    override fun provideName(provider: ProviderType): String? {
        // rewrite eventName if need for different analytics with when(this) conditions
        return this.eventName
    }

    override fun provideParameters(provider: ProviderType): HashMap<String, Any> {
        val params = hashMapOf<String, Any>(AUUID_KEY to WavesWallet.getAuuid())
        when (this) {
            is DexEvent -> {
                params[PAIR_KEY] = "$amountAssetName/$priceAssetName"
            }
            is CurrencyEvent -> {
                params[CURRENCY_KEY] = currency
            }
            is CountEvent -> {
                params[COUNT_KEY] = count
            }
            is MarketPulseSettingsChangedEvent -> {
                params[STYLE_KEY] = style
                params[INTERVAL_KEY] = interval
                params[ASSETS_KEY] = assets
            }
        }
        return params
    }

    abstract class DexEvent(eventName: String, var amountAssetName: String, var priceAssetName: String) : AnalyticEvents(eventName)
    abstract class CurrencyEvent(eventName: String, var currency: String) : AnalyticEvents(eventName)
    abstract class CountEvent(eventName: String, var count: Int) : AnalyticEvents(eventName)

    companion object {
        const val AUUID_KEY = "AUUID"
        const val PAIR_KEY = "Pair"
        const val CURRENCY_KEY = "Currency"
        const val COUNT_KEY = "Count"

        const val STYLE_KEY = "Style"
        const val INTERVAL_KEY = "Interval"
        const val ASSETS_KEY = "Assets"
    }
}