package com.wavesplatform.wallet.v1.injection;

import com.wavesplatform.wallet.v1.data.datamanagers.AddressBookManager;
import com.wavesplatform.wallet.v1.payload.ExchangeTransaction;
import com.wavesplatform.wallet.v1.ui.assets.IssueViewModel;
import com.wavesplatform.wallet.v1.ui.auth.LandingViewModel;
import com.wavesplatform.wallet.v1.ui.auth.PinEntryViewModel;
import com.wavesplatform.wallet.v1.ui.balance.TransactionsViewModel;
import com.wavesplatform.wallet.v1.ui.dex.details.DexDetailsViewModel;
import com.wavesplatform.wallet.v1.ui.dex.details.chart.ChartViewModel;
import com.wavesplatform.wallet.v1.ui.dex.details.orderbook.OrderBookViewModel;
import com.wavesplatform.wallet.v1.ui.dex.details.last_trades.LastTradesViewModel;
import com.wavesplatform.wallet.v1.ui.dex.details.my_orders.MyOrdersViewModel;
import com.wavesplatform.wallet.v1.ui.dex.markets.MarketsViewModel;
import com.wavesplatform.wallet.v1.ui.dex.markets.add.AddMarketViewModel;
import com.wavesplatform.wallet.v1.ui.dex.watchlist_markets.WatchlistMarketsViewModel;
import com.wavesplatform.wallet.v1.ui.fingerprint.FingerprintDialogViewModel;
import com.wavesplatform.wallet.v1.ui.home.MainViewModel;
import com.wavesplatform.wallet.v1.ui.launcher.LauncherViewModel;
import com.wavesplatform.wallet.v1.ui.pairing.PairingViewModel;
import com.wavesplatform.wallet.v1.ui.dex.details.order.PlaceOrderViewModel;
import com.wavesplatform.wallet.v1.ui.receive.ReceiveViewModel;
import com.wavesplatform.wallet.v1.ui.send.SendViewModel;
import com.wavesplatform.wallet.v1.ui.transactions.ExchangeTransactionDetailViewModel;
import com.wavesplatform.wallet.v1.ui.transactions.IssueDetailViewModel;
import com.wavesplatform.wallet.v1.ui.transactions.ReissueDetailViewModel;
import com.wavesplatform.wallet.v1.ui.transactions.TransactionDetailViewModel;
import com.wavesplatform.wallet.v1.ui.transactions.UnknownDetailViewModel;

import dagger.Subcomponent;

/**
 * Subcomponents have access to all upstream objects in the graph but can have their own scope -
 * they don't need to explicitly state their dependencies as they have access anyway
 */
@SuppressWarnings("WeakerAccess")
@ViewModelScope
@Subcomponent(modules = DataManagerModule.class)
public interface DataManagerComponent {

    void inject(LauncherViewModel launcherViewModel);

    void inject(AddMarketViewModel addMarketViewModel);

    void inject(OrderBookViewModel orderBookViewModel);

    void inject(WatchlistMarketsViewModel watchlistMarketsViewModel);

    void inject(MarketsViewModel marketsViewModel);

    void inject(SendViewModel sendViewModel);

    void inject(ChartViewModel chartViewModel);

    void inject(PinEntryViewModel pinEntryViewModel);

    void inject(MainViewModel mainViewModel);

    void inject(TransactionsViewModel transactionsViewModel);

    void inject(PairingViewModel pairingViewModel);

    void inject(ReceiveViewModel receiveViewModel);

    void inject(TransactionDetailViewModel transactionDetailViewModel);

    void inject(FingerprintDialogViewModel fingerprintDialogViewModel);

    void inject(LandingViewModel landingViewModel);

    void inject(AddressBookManager addressBookManager);

    void inject(IssueDetailViewModel issueDetailViewModel);

    void inject(IssueViewModel issueViewModel);

    void inject(ReissueDetailViewModel reissueDetailViewModel);

    void inject(ExchangeTransactionDetailViewModel exchangeTransactionDetailViewModel);

    void inject(UnknownDetailViewModel unknownDetailViewModel);

    void inject(PlaceOrderViewModel placeOrderViewModel);

    void inject(DexDetailsViewModel dexDetailsViewModel);

    void inject(LastTradesViewModel lastTradesViewModel);

    void inject(MyOrdersViewModel myOrdersViewModel);
}
