package com.wavesplatform.wallet.ui.dex.details.my_orders;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import com.wavesplatform.wallet.R;
import com.wavesplatform.wallet.api.NodeManager;
import com.wavesplatform.wallet.api.matcher.MatcherManager;
import com.wavesplatform.wallet.data.access.AccessState;
import com.wavesplatform.wallet.data.rxjava.RxUtil;
import com.wavesplatform.wallet.injection.Injector;
import com.wavesplatform.wallet.payload.MyOrder;
import com.wavesplatform.wallet.request.CancelOrderRequest;
import com.wavesplatform.wallet.request.MyOrdersRequest;
import com.wavesplatform.wallet.ui.base.BaseViewModel;
import com.wavesplatform.wallet.ui.customviews.ToastCustom;
import com.wavesplatform.wallet.util.PrefsUtil;
import com.wavesplatform.wallet.util.RxEventBus;
import com.wavesplatform.wallet.util.SSLVerifyUtil;
import com.wavesplatform.wallet.util.annotations.Thunk;

import java.util.List;

import javax.inject.Inject;

@SuppressWarnings("WeakerAccess")
public class MyOrdersViewModel extends BaseViewModel {

    @Inject
    SSLVerifyUtil sslVerifyUtil;
    @Inject
    PrefsUtil prefsUtil;
    @Thunk
    DataListener dataListener;
    private Context context;
    private MyOrdersRequest myOrdersRequest;
    private CancelOrderRequest mCancelOrderRequest;
    public MyOrderModel myOrderModel;
    @Inject
    RxEventBus mRxEventBus;

    MyOrdersViewModel(Context context, DataListener dataListener) {

        Injector.getInstance().getDataManagerComponent().inject(this);
        this.context = context;
        this.dataListener = dataListener;

        myOrderModel = new MyOrderModel();
        myOrdersRequest = new MyOrdersRequest(prefsUtil.getValue(PrefsUtil.KEY_PUB_KEY, ""));
        mCancelOrderRequest = new CancelOrderRequest(NodeManager.get().getPublicKeyStr());

        sslVerifyUtil.validateSSL();
    }

    public interface DataListener {
        void showMyOrders(List<MyOrder> myOrders);

        void afterSuccessfullyDelete(int position);

        void showProgressDialog(@StringRes int messageId, @Nullable String suffix);

        void onShowToast(@StringRes int message, @ToastCustom.ToastType String toastType);

        void dismissProgressDialog();
    }

    @Override
    public void onViewReady() {
        getMyOrders();
    }

    public boolean signTransaction() {
        byte[] pk = AccessState.getInstance().getPrivateKey();
        if (pk != null) {
            myOrdersRequest.sign(pk);
            return true;
        } else {
            return false;
        }
    }

    public void getMyOrders() {
        dataListener.showProgressDialog(R.string.please_wait,"...");
        compositeDisposable.add(MatcherManager.get().getMyOrders(myOrderModel.getPairModel().market.amountAsset,
                myOrderModel.getPairModel().market.priceAsset, prefsUtil.getValue(PrefsUtil.KEY_PUB_KEY, ""), myOrdersRequest.getSignature(), myOrdersRequest.getTimestamp())
                .compose(RxUtil.applySchedulersToObservable())
                .subscribe(myOrders -> {
                    if (dataListener!=null){
                        dataListener.showMyOrders(myOrders);
                        dataListener.dismissProgressDialog();
                    }
                }, throwable -> {
                    if (dataListener!=null){
                        dataListener.onShowToast(R.string.unexpected_error, ToastCustom.TYPE_ERROR);
                        dataListener.dismissProgressDialog();
                    }
                }));
    }


    public boolean signTransaction(String orderId) {
        mCancelOrderRequest.orderId = orderId;

        byte[] pk = AccessState.getInstance().getPrivateKey();
        if (pk != null) {
            mCancelOrderRequest.sign(pk);
            return true;
        } else {
            return false;
        }
    }

    public void cancelOrder() {
        dataListener.showProgressDialog(R.string.please_wait,"...");
        compositeDisposable.add(MatcherManager.get().cancelOrder(myOrderModel.getPairModel().market.amountAsset,
                myOrderModel.getPairModel().market.priceAsset, mCancelOrderRequest)
                .compose(RxUtil.applySchedulersToObservable())
                .subscribe(o -> {
                    if (dataListener!=null){
                        dataListener.dismissProgressDialog();
                        getMyOrders();
                    }
                }, throwable -> {
                    if (dataListener!=null){
                        dataListener.onShowToast(R.string.unexpected_error, ToastCustom.TYPE_ERROR);
                        dataListener.dismissProgressDialog();
                    }
                }));
    }

    public void deleteOrder(int position) {
        dataListener.showProgressDialog(R.string.please_wait,"...");
        compositeDisposable.add(MatcherManager.get().deleteOrder(myOrderModel.getPairModel().market.amountAsset,
                myOrderModel.getPairModel().market.priceAsset, mCancelOrderRequest)
                .compose(RxUtil.applySchedulersToObservable())
                .subscribe(o -> {
                    if (dataListener!=null){
                        dataListener.afterSuccessfullyDelete(position);
                        dataListener.dismissProgressDialog();
                    }
                }, throwable -> {
                    if (dataListener!=null){
                        dataListener.onShowToast(R.string.unexpected_error, ToastCustom.TYPE_ERROR);
                        dataListener.dismissProgressDialog();
                    }
                }));
    }
}
