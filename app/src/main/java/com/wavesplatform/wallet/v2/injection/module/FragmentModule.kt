package com.wavesplatform.wallet.v2.injection.module

import com.wavesplatform.wallet.v2.injection.scope.PerFragment
import com.wavesplatform.wallet.v2.ui.home.dex.trade.buy_and_sell.buy.TradeBuyFragment
import com.wavesplatform.wallet.v2.ui.home.dex.DexFragment
import com.wavesplatform.wallet.v2.ui.home.dex.trade.last_trades.TradeLastTradesFragment
import com.wavesplatform.wallet.v2.ui.home.history.HistoryFragment
import com.wavesplatform.wallet.v2.ui.home.history.item.HistoryDateItemFragment
import com.wavesplatform.wallet.v2.ui.home.profile.ProfileFragment
import com.wavesplatform.wallet.v2.ui.home.quick_action.receive.bank.BankFragment
import com.wavesplatform.wallet.v2.ui.home.quick_action.receive.card.CardFragment
import com.wavesplatform.wallet.v2.ui.home.quick_action.receive.cryptocurrency.СryptocurrencyFragment
import com.wavesplatform.wallet.v2.ui.home.quick_action.receive.invoice.InvoiceFragment
import com.wavesplatform.wallet.v2.ui.home.wallet.WalletFragment
import com.wavesplatform.wallet.v2.ui.home.wallet.assets.AssetsFragment
import com.wavesplatform.wallet.v2.ui.home.wallet.assets.details.content.AssetDetailsContentFragment
import com.wavesplatform.wallet.v2.ui.home.wallet.leasing.LeasingFragment
import com.wavesplatform.wallet.v2.ui.home.dex.trade.my_orders.TradeMyOrdersFragment
import com.wavesplatform.wallet.v2.ui.home.dex.trade.orderbook.TradeOrderbookFragment
import com.wavesplatform.wallet.v2.ui.home.dex.trade.buy_and_sell.sell.TradeSellFragment
import com.wavesplatform.wallet.v2.ui.home.dex.trade.chart.TradeChartFragment
import com.wavesplatform.wallet.v2.ui.home.history.details.HistoryDetailsBottomSheetFragment
import com.wavesplatform.wallet.v2.ui.home.profile.addresses.alias.AddressesAndKeysBottomSheetFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class FragmentModule {

    @PerFragment
    @ContributesAndroidInjector
    internal abstract fun walletFragment(): WalletFragment

    @PerFragment
    @ContributesAndroidInjector
    internal abstract fun dexFragment(): DexFragment

    @PerFragment
    @ContributesAndroidInjector
    internal abstract fun historyFragment(): HistoryFragment

    @PerFragment
    @ContributesAndroidInjector
    internal abstract fun profileFragment(): ProfileFragment

    @PerFragment
    @ContributesAndroidInjector
    internal abstract fun assetsFragment(): AssetsFragment

    @PerFragment
    @ContributesAndroidInjector
    internal abstract fun leasingFragment(): LeasingFragment

    @PerFragment
    @ContributesAndroidInjector
    internal abstract fun historyDateItemFragment(): HistoryDateItemFragment

    @PerFragment
    @ContributesAndroidInjector
    internal abstract fun assetDetailsContentFragment(): AssetDetailsContentFragment

    @PerFragment
    @ContributesAndroidInjector
    internal abstract fun cryptocurrencyFragment(): СryptocurrencyFragment

    @PerFragment
    @ContributesAndroidInjector
    internal abstract fun invoiceFragment(): InvoiceFragment

    @PerFragment
    @ContributesAndroidInjector
    internal abstract fun cardFragment(): CardFragment

    @PerFragment
    @ContributesAndroidInjector
    internal abstract fun bankFragment(): BankFragment

    @PerFragment
    @ContributesAndroidInjector
    internal abstract fun tradeMyOrdersFragment(): TradeMyOrdersFragment

    @PerFragment
    @ContributesAndroidInjector
    internal abstract fun tradeLastTradesFragment(): TradeLastTradesFragment

    @PerFragment
    @ContributesAndroidInjector
    internal abstract fun tradeOrderbookFragment(): TradeOrderbookFragment

    @PerFragment
    @ContributesAndroidInjector
    internal abstract fun tradeBuyFragment(): TradeBuyFragment

    @PerFragment
    @ContributesAndroidInjector
    internal abstract fun tradeSellFragment(): TradeSellFragment

    @PerFragment
    @ContributesAndroidInjector
    internal abstract fun tradeChartFragment(): TradeChartFragment

    @PerFragment
    @ContributesAndroidInjector
    internal abstract fun addressesAndKeysBottomSheetFragment(): AddressesAndKeysBottomSheetFragment

    @PerFragment
    @ContributesAndroidInjector
    internal abstract fun h2istoryDetailsBottomSheetFragment(): HistoryDetailsBottomSheetFragment
}
