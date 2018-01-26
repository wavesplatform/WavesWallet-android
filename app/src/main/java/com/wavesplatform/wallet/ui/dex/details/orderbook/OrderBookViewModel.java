package com.wavesplatform.wallet.ui.dex.details.orderbook;

import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import com.wavesplatform.wallet.R;
import com.wavesplatform.wallet.api.mather.MatherManager;
import com.wavesplatform.wallet.data.rxjava.RxUtil;
import com.wavesplatform.wallet.injection.Injector;
import com.wavesplatform.wallet.payload.OrderBook;
import com.wavesplatform.wallet.payload.WatchMarket;
import com.wavesplatform.wallet.ui.base.BaseViewModel;
import com.wavesplatform.wallet.ui.customviews.ToastCustom;
import com.wavesplatform.wallet.util.RxEventBus;
import com.wavesplatform.wallet.util.SSLVerifyUtil;
import com.wavesplatform.wallet.util.annotations.Thunk;

import javax.inject.Inject;

@SuppressWarnings("WeakerAccess")
public class OrderBookViewModel extends BaseViewModel {

    @Thunk DataListener dataListener;

    @Inject
    SSLVerifyUtil sslVerifyUtil;
    @Inject
    RxEventBus mRxEventBus;

    OrderBookViewModel(DataListener dataListener) {

        Injector.getInstance().getDataManagerComponent().inject(this);

        this.dataListener = dataListener;

        sslVerifyUtil.validateSSL();
    }

    @Override
    public void onViewReady() {
        // No-op
    }

    @Override
    public void destroy() {
        super.destroy();
        dataListener = null;
    }

    public interface DataListener {
        void onShowToast(@StringRes int message, @ToastCustom.ToastType String toastType);

        void successfullyGetOrderBook(OrderBook orderBook);

        void finishPage();

        void showProgressDialog(@StringRes int messageId, @Nullable String suffix);

        void dismissProgressDialog();

    }

    public void getOrderBook(WatchMarket watchMarket) {
        dataListener.showProgressDialog(R.string.dex_fetching_markets, null);
        compositeDisposable.add(MatherManager.get().getOrderBookInterval(watchMarket.market.amountAsset, watchMarket.market.priceAsset)
                .compose(RxUtil.applySchedulersToObservable())
                .subscribe(orderBook -> {
                    if (dataListener != null){
                        dataListener.successfullyGetOrderBook(orderBook);
                        dataListener.dismissProgressDialog();
                    }
                }, throwable -> {
                    if (dataListener != null){
                        dataListener.onShowToast(R.string.unexpected_error, ToastCustom.TYPE_ERROR);
                        dataListener.dismissProgressDialog();
                    }
                }));

    }
}
