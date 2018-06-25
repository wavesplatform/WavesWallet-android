package com.wavesplatform.wallet.injection;

import com.wavesplatform.wallet.data.datamanagers.AddressBookManager;
import com.wavesplatform.wallet.payload.ExchangeTransaction;
import com.wavesplatform.wallet.ui.assets.IssueViewModel;
import com.wavesplatform.wallet.ui.auth.LandingViewModel;
import com.wavesplatform.wallet.ui.auth.PinEntryViewModel;
import com.wavesplatform.wallet.ui.balance.TransactionsViewModel;
import com.wavesplatform.wallet.ui.dex.details.DexDetailsViewModel;
import com.wavesplatform.wallet.ui.dex.details.chart.ChartViewModel;
import com.wavesplatform.wallet.ui.dex.details.orderbook.OrderBookViewModel;
import com.wavesplatform.wallet.ui.dex.details.last_trades.LastTradesViewModel;
import com.wavesplatform.wallet.ui.dex.details.my_orders.MyOrdersViewModel;
import com.wavesplatform.wallet.ui.dex.markets.MarketsViewModel;
import com.wavesplatform.wallet.ui.dex.markets.add.AddMarketViewModel;
import com.wavesplatform.wallet.ui.dex.watchlist_markets.WatchlistMarketsViewModel;
import com.wavesplatform.wallet.ui.fingerprint.FingerprintDialogViewModel;
import com.wavesplatform.wallet.ui.home.MainViewModel;
import com.wavesplatform.wallet.ui.launcher.LauncherViewModel;
import com.wavesplatform.wallet.ui.pairing.PairingViewModel;
import com.wavesplatform.wallet.ui.dex.details.order.PlaceOrderViewModel;
import com.wavesplatform.wallet.ui.receive.ReceiveViewModel;
import com.wavesplatform.wallet.ui.send.SendViewModel;
import com.wavesplatform.wallet.ui.transactions.ExchangeTransactionDetailViewModel;
import com.wavesplatform.wallet.ui.transactions.IssueDetailViewModel;
import com.wavesplatform.wallet.ui.transactions.MassTransferDetailViewModel;
import com.wavesplatform.wallet.ui.transactions.ReissueDetailViewModel;
import com.wavesplatform.wallet.ui.transactions.TransactionDetailViewModel;
import com.wavesplatform.wallet.ui.transactions.UnknownDetailViewModel;

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

    void inject(MassTransferDetailViewModel massTransferDetailViewModel);

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
