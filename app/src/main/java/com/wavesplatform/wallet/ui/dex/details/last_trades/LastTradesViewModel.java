package com.wavesplatform.wallet.ui.dex.details.last_trades;

import android.content.Context;

import com.wavesplatform.wallet.api.datafeed.DataFeedManager;
import com.wavesplatform.wallet.injection.Injector;
import com.wavesplatform.wallet.payload.TradesMarket;
import com.wavesplatform.wallet.ui.base.BaseViewModel;
import com.wavesplatform.wallet.util.RxEventBus;
import com.wavesplatform.wallet.util.SSLVerifyUtil;
import com.wavesplatform.wallet.util.annotations.Thunk;

import java.util.List;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

@SuppressWarnings("WeakerAccess")
public class LastTradesViewModel extends BaseViewModel {

    @Inject
    SSLVerifyUtil sslVerifyUtil;
    @Thunk
    DataListener dataListener;
    private Context context;
    public LastTradeModel lastTradeModel;

    @Inject
    RxEventBus mRxEventBus;

    LastTradesViewModel(Context context, DataListener dataListener) {

        Injector.getInstance().getDataManagerComponent().inject(this);
        this.context = context;
        this.dataListener = dataListener;

        lastTradeModel = new LastTradeModel();
        sslVerifyUtil.validateSSL();
    }

    public interface DataListener {

        void successfullyGetLastTrades(List<TradesMarket> tradesMarket);
    }

    public void getTradesByPair(String amountAsset, String priceAsset, String limit) {
        compositeDisposable.add(DataFeedManager.get().getTradesByPairWithoutInterval(amountAsset, priceAsset, limit)
                .subscribeOn(Schedulers.from(Executors.newSingleThreadExecutor()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(tradesMarket -> {
                    if (dataListener != null)
                        dataListener.successfullyGetLastTrades(tradesMarket);
                }, Throwable::printStackTrace));

    }

    @Override
    public void onViewReady() {
        getTradesByPair(lastTradeModel.getPairModel().market.amountAsset, lastTradeModel.getPairModel().market.priceAsset, "50");
    }
}
