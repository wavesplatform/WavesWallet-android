package com.wavesplatform.wallet.ui.dex.markets.add;

import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.util.Pair;

import com.wavesplatform.wallet.R;
import com.wavesplatform.wallet.api.NodeManager;
import com.wavesplatform.wallet.api.mather.MatherManager;
import com.wavesplatform.wallet.data.exception.RetrofitException;
import com.wavesplatform.wallet.data.rxjava.RxUtil;
import com.wavesplatform.wallet.db.DBHelper;
import com.wavesplatform.wallet.injection.Injector;
import com.wavesplatform.wallet.payload.AmountAssetInfo;
import com.wavesplatform.wallet.payload.Error;
import com.wavesplatform.wallet.payload.Market;
import com.wavesplatform.wallet.payload.PriceAssetInfo;
import com.wavesplatform.wallet.payload.TransactionsInfo;
import com.wavesplatform.wallet.ui.base.BaseViewModel;
import com.wavesplatform.wallet.ui.customviews.ToastCustom;
import com.wavesplatform.wallet.util.SSLVerifyUtil;
import com.wavesplatform.wallet.util.annotations.Thunk;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.functions.BiFunction;
import io.realm.RealmResults;

@SuppressWarnings("WeakerAccess")
public class AddMarketViewModel extends BaseViewModel {

    public static final String WAVES = "WAVES";
    @Thunk
    DataListener dataListener;

    @Inject
    SSLVerifyUtil sslVerifyUtil;

    AddMarketViewModel(DataListener dataListener) {

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

        void onShowToast(String message, @ToastCustom.ToastType String toastType);

        void afterSuccessfullyOrderBook(Market market);

        void showProgressDialog(@StringRes int messageId, @Nullable String suffix);

        void dismissProgressDialog();

    }

    public boolean validateFields(String amount, String price) {
        if (price.trim().isEmpty()) {
            dataListener.onShowToast(R.string.place_order_price_is_missing, ToastCustom.TYPE_ERROR);
            return false;
        }
        if (amount.trim().isEmpty()) {
            dataListener.onShowToast(R.string.place_order_amount_is_missing, ToastCustom.TYPE_ERROR);
            return false;
        }
        return true;
    }

    public void getOrderBook(String amountAsset, String priceAsset) {
        dataListener.showProgressDialog(R.string.please_wait, "...");
        compositeDisposable.add(MatherManager.get().getOrderBook(amountAsset, priceAsset)
                .flatMap(orderBook -> {
                    if (orderBook.pair.amountAsset.trim().equals(WAVES)) {
                        return Observable.zip(Observable.just(new TransactionsInfo(WAVES, WAVES, 8)),
                                NodeManager.get().getTransactionsInfo(orderBook.pair.priceAsset),
                                (BiFunction<TransactionsInfo, TransactionsInfo, Pair>) Pair::new);
                    } else if (orderBook.pair.priceAsset.trim().equals(WAVES)) {
                        return Observable.zip(NodeManager.get().getTransactionsInfo(orderBook.pair.amountAsset),
                                Observable.just(new TransactionsInfo(WAVES, WAVES, 8)),
                                (BiFunction<TransactionsInfo, TransactionsInfo, Pair>) Pair::new);
                    } else {
                        return Observable.zip(NodeManager.get().getTransactionsInfo(orderBook.pair.amountAsset),
                                NodeManager.get().getTransactionsInfo(orderBook.pair.priceAsset),
                                (BiFunction<TransactionsInfo, TransactionsInfo, Pair>) Pair::new);
                    }
                })
                .map(pair -> {
                    TransactionsInfo amount = (TransactionsInfo) pair.first;
                    TransactionsInfo price = (TransactionsInfo) pair.second;
                    return new Market(amount.assetId, amount.name, price.assetId, price.name, new AmountAssetInfo(amount.decimals), new PriceAssetInfo(price.decimals));
                })
                .map(market -> {
                    DBHelper.getInstance().getRealm().executeTransaction(realm -> {
                        RealmResults<Market> realmResults = realm.where(Market.class)
                                .equalTo("amountAsset", market.amountAsset)
                                .equalTo("priceAsset", market.priceAsset)
                                .findAll();
                        if (realmResults.isEmpty()) realm.copyToRealm(market);
                    });
                    return market;
                })
                .compose(RxUtil.applySchedulersToObservable())
                .subscribe(market -> {
                    if (dataListener != null) {
                        dataListener.dismissProgressDialog();
                        dataListener.afterSuccessfullyOrderBook(market);
                    }
                }, throwable -> {
                    Error response = ((RetrofitException) throwable).getErrorBodyAs(Error.class);
                    if (dataListener != null) {
                        dataListener.dismissProgressDialog();
                        dataListener.onShowToast(response.message, ToastCustom.TYPE_ERROR);
                    }
                }));
    }
}
