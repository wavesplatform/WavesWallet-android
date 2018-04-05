package com.wavesplatform.wallet.v1.ui.dex.watchlist_markets;

import android.content.Context;
import android.support.annotation.StringRes;

import com.wavesplatform.wallet.v1.api.datafeed.DataFeedManager;
import com.wavesplatform.wallet.v1.data.rxjava.RxUtil;
import com.wavesplatform.wallet.v1.db.DBHelper;
import com.wavesplatform.wallet.v1.injection.Injector;
import com.wavesplatform.wallet.v1.payload.AmountAssetInfo;
import com.wavesplatform.wallet.v1.payload.Market;
import com.wavesplatform.wallet.v1.payload.PriceAssetInfo;
import com.wavesplatform.wallet.v1.payload.TickerMarket;
import com.wavesplatform.wallet.v1.payload.TradesMarket;
import com.wavesplatform.wallet.v1.payload.WatchMarket;
import com.wavesplatform.wallet.v1.ui.base.BaseViewModel;
import com.wavesplatform.wallet.v1.ui.customviews.ToastCustom;
import com.wavesplatform.wallet.v1.util.SSLVerifyUtil;
import com.wavesplatform.wallet.v1.util.annotations.Thunk;

import java.util.ArrayList;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.realm.RealmResults;

@SuppressWarnings("WeakerAccess")
public class WatchlistMarketsViewModel extends BaseViewModel {

    @Thunk
    DataListener dataListener;
    private Context context;
    public final String defaultAmount = "WAVES";
    public final String defaultPrice = "8LQW8f7P5d5PZM7GtZEBgaqRPGSzS3DfPuiXrURJ4AJS";
    private Market defaultMarket;

    @Inject
    SSLVerifyUtil sslVerifyUtil;

    WatchlistMarketsViewModel(Context context, DataListener dataListener) {

        Injector.getInstance().getDataManagerComponent().inject(this);

        this.context = context;
        this.dataListener = dataListener;

        defaultMarket = new Market(defaultAmount, defaultAmount, defaultPrice, "BTC", new AmountAssetInfo(8), new PriceAssetInfo(8));

        sslVerifyUtil.validateSSL();
    }

    @Override
    public void onViewReady() {
        // No-op
    }

    @Override
    public void destroy() {
        super.destroy();
        context = null;
        dataListener = null;
    }

    public interface DataListener {
        void onShowToast(@StringRes int message, @ToastCustom.ToastType String toastType);

        void successfullyGetTickerByPair(int position, TickerMarket markets);

        void successfullyGetTradesByPair(int position, TradesMarket markets);

        void finishPage();
    }

    public void getTickerByPair(int position, WatchMarket watchMarket) {
        compositeDisposable.add(DataFeedManager.get().getTickerByPair(watchMarket.market.amountAsset, watchMarket.market.priceAsset)
                .compose(RxUtil.applySchedulersToObservable())
                .subscribe(tickerMarket -> {
                    if (dataListener != null)
                        dataListener.successfullyGetTickerByPair(position, tickerMarket);
                }, Throwable::printStackTrace));

    }

    public void getTradesByPair(int position, WatchMarket watchMarket) {
        compositeDisposable.add(DataFeedManager.get().getTradesByPair(watchMarket.market.amountAsset, watchMarket.market.priceAsset)
                .subscribeOn(Schedulers.from(Executors.newSingleThreadExecutor()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(tradesMarket -> {
                    if (dataListener != null)
                        dataListener.successfullyGetTradesByPair(position, tradesMarket);
                }, throwable -> {
                    if (dataListener != null)
                        dataListener.successfullyGetTradesByPair(position, new TradesMarket());
                }));

    }

    public RealmResults<Market> getCurrentWatchlistMarkets() {
        RealmResults<Market> all = DBHelper.getInstance().getRealm().where(Market.class)
                .equalTo("amountAsset", defaultAmount)
                .equalTo("priceAsset", defaultPrice)
                .findAll();
        if (all.isEmpty()) DBHelper.getInstance().getRealm().executeTransaction(realm -> realm.copyToRealm(defaultMarket));
        return DBHelper.getInstance().getRealm().where(Market.class).findAll();
    }

    public void deleteMarketFromWathclist(Market market) {
        DBHelper.getInstance().getRealm().executeTransaction(realm -> {
            findMarket(market).deleteFromRealm();
        });
    }

    public Market findMarket(Market market) {
        return DBHelper.getInstance().getRealm().where(Market.class)
                .equalTo("id", market.id)
                .findFirst();
    }

    public void updateCurrentWatchlistMarkets(ArrayList<Market> list) {
        DBHelper.getInstance().getRealm().executeTransaction(realm -> {
            for (Market market : list) {
                market.id = market.amountAsset + market.priceAsset;
                if (realm.where(Market.class).equalTo("id", market.id).count() == 0) {
                    realm.copyToRealm(market);
                }
            }
        });
    }


}
