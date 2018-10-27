package com.wavesplatform.wallet.ui.dex.details.order;

import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import com.google.gson.internal.LinkedTreeMap;
import com.wavesplatform.wallet.R;
import com.wavesplatform.wallet.api.NodeManager;
import com.wavesplatform.wallet.api.matcher.MatcherManager;
import com.wavesplatform.wallet.data.Events;
import com.wavesplatform.wallet.data.access.AccessState;
import com.wavesplatform.wallet.data.exception.RetrofitException;
import com.wavesplatform.wallet.data.rxjava.RxUtil;
import com.wavesplatform.wallet.injection.Injector;
import com.wavesplatform.wallet.payload.Error;
import com.wavesplatform.wallet.payload.WatchMarket;
import com.wavesplatform.wallet.request.OrderRequest;
import com.wavesplatform.wallet.ui.base.BaseViewModel;
import com.wavesplatform.wallet.ui.customviews.ToastCustom;
import com.wavesplatform.wallet.util.PrefsUtil;
import com.wavesplatform.wallet.util.RxEventBus;
import com.wavesplatform.wallet.util.SSLVerifyUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;

import javax.inject.Inject;

@SuppressWarnings("WeakerAccess")
public class PlaceOrderViewModel extends BaseViewModel {

    @Inject protected PrefsUtil prefsUtil;
    private DataListener dataListener;
    public PlaceOrderModel placeOrderModel;
    private OrderRequest mOrderRequest;
    @Inject
    RxEventBus mRxEventBus;

    @Inject
    SSLVerifyUtil sslVerifyUtil;

    public PlaceOrderViewModel(WatchMarket watchMarket, DataListener dataListener) {
        Injector.getInstance().getDataManagerComponent().inject(this);

        this.dataListener = dataListener;

        placeOrderModel = new PlaceOrderModel();

        placeOrderModel.setWatchMarket(watchMarket);

        mOrderRequest = new OrderRequest(NodeManager.get().getPublicKeyStr(), placeOrderModel.getAssetPair());

        sslVerifyUtil.validateSSL();
    }


    public PrefsUtil getPrefsUtil() {
        return prefsUtil;
    }

    @Override
    public void onViewReady() {
        getMatcherKey();
        getBalanceFromAssetPair();
    }

    public interface DataListener {

        void onShowToast(@StringRes int message, @ToastCustom.ToastType String toastType);

        void onShowToast(String message, @ToastCustom.ToastType String toastType);

        void showBalanceFromPair(LinkedTreeMap<String, Long> stringIntegerHashMap);

        void afterSuccessfullyPlaceOrder();

        void showProgressDialog(@StringRes int messageId, @Nullable String suffix);

        void dismissProgressDialog();

        void trackPlaceOrder(OrderRequest signed);
    }

    public void getMatcherKey() {
        compositeDisposable.add(MatcherManager.get().getMatcherKey()
                .compose(RxUtil.applySchedulersToObservable())
                .subscribe(key -> {
                    mOrderRequest.matcherPublicKey = key;
                }, throwable -> {
                    dataListener.onShowToast(R.string.unexpected_error, ToastCustom.TYPE_ERROR);
                }));
    }

    private void getBalanceFromAssetPair() {
        compositeDisposable.add(MatcherManager.get().getBalanceFromAssetPair(placeOrderModel.getWatchMarket().market.amountAsset, placeOrderModel.getWatchMarket().market.priceAsset, NodeManager.get().getAddress())
                .compose(RxUtil.applySchedulersToObservable())
                .subscribe(stringIntegerHashMap -> {
                    dataListener.showBalanceFromPair(stringIntegerHashMap);
                }, throwable -> {
                    dataListener.onShowToast(R.string.unexpected_error, ToastCustom.TYPE_ERROR);
                }));
    }

    public void placeOrder() {
        dataListener.showProgressDialog(R.string.please_wait, "...");
        compositeDisposable.add(MatcherManager.get().placeOrder(mOrderRequest)
                .compose(RxUtil.applySchedulersToObservable())
                .subscribe(o -> {
                    if (dataListener != null){
                        mRxEventBus.post(new Events.NeedUpdateDataAfterPlaceOrder());
                        dataListener.trackPlaceOrder(mOrderRequest);
                        dataListener.afterSuccessfullyPlaceOrder();
                        dataListener.dismissProgressDialog();
                    }
                }, throwable -> {
                    Error response = ((RetrofitException) throwable).getErrorBodyAs(Error.class);
                    if (dataListener != null){
                        dataListener.dismissProgressDialog();
                        dataListener.onShowToast(response.message, ToastCustom.TYPE_ERROR);
                    }
                }));
    }

    public boolean validateFields(String amount, String price){
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

    public boolean signTransaction(String amount, String price) {
        BigDecimal bdPrice = new BigDecimal(price);
        BigDecimal bdAmount = new BigDecimal(amount);

        mOrderRequest.amount = bdAmount.setScale(placeOrderModel.getAmountDecimals(), RoundingMode.HALF_UP).unscaledValue().longValue();
        mOrderRequest.price = bdPrice.setScale(placeOrderModel.getPriceValueDecimals(),RoundingMode.HALF_UP).unscaledValue().longValue();
        byte[] pk = AccessState.getInstance().getPrivateKey();
        if (pk != null) {
            mOrderRequest.sign(pk);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        dataListener = null;
    }

    public OrderRequest getOrderRequest() {
        return mOrderRequest;
    }
}
