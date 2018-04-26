package com.wavesplatform.wallet.ui.assets;

import android.content.Context;
import android.databinding.Bindable;
import android.support.annotation.StringRes;
import android.util.Log;

import com.appsflyer.AppsFlyerLib;
import com.google.common.base.Charsets;
import com.google.common.primitives.Ints;
import com.wavesplatform.wallet.BR;
import com.wavesplatform.wallet.R;
import com.wavesplatform.wallet.api.NodeManager;
import com.wavesplatform.wallet.data.access.AccessState;
import com.wavesplatform.wallet.data.rxjava.RxUtil;
import com.wavesplatform.wallet.databinding.ActivityIssueAssetBinding;
import com.wavesplatform.wallet.injection.Injector;
import com.wavesplatform.wallet.request.IssueTransactionRequest;
import com.wavesplatform.wallet.ui.base.BaseViewModel;
import com.wavesplatform.wallet.ui.customviews.ToastCustom;
import com.wavesplatform.wallet.util.MoneyUtil;

import java.util.HashMap;
import java.util.Map;


@SuppressWarnings("WeakerAccess")
public class IssueViewModel extends BaseViewModel {

    private final String TAG = getClass().getSimpleName();

    DataListener dataListener;
    ActivityIssueAssetBinding binding;
    private Context context;

    private String name;
    private String description;
    private String quantity;
    private String decimals;
    private boolean reissuable;

    private IssueTransactionRequest request;

    IssueViewModel(Context context, DataListener dataListener, ActivityIssueAssetBinding binding) {
        Injector.getInstance().getDataManagerComponent().inject(this);

        this.context = context;
        this.binding = binding;
        this.dataListener = dataListener;
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
        void onShowTransactionDetails(IssueTransactionRequest request);

        void onShowTransactionSuccess(IssueTransactionRequest signed);

        void finishPage();

        void onShowToast(@StringRes int message, @ToastCustom.ToastType String toastType);

        void hideKeyboard();
    }

    void sendClicked() {
        if (dataListener != null)
            dataListener.hideKeyboard();

        validateAndCreate();
        if (request != null) {
            if (dataListener != null)
                dataListener.onShowTransactionDetails(request);
        } else {
            if (dataListener != null)
                dataListener.onShowToast(R.string.correct_errors, ToastCustom.TYPE_ERROR);
        }
    }

    private String getString(@StringRes int resId) {
        return context.getString(resId);
    }

    public int getArrayByteSize(String str) {
        return str != null ? str.getBytes(Charsets.UTF_8).length : 0;
    }

    private void validateAndCreate() {
        boolean isError = false;
        if (getArrayByteSize(name) < IssueTransactionRequest.MinAssetNameLength) {
            binding.name.setError(getString(R.string.name_too_short));
            isError = true;
        } else if (getArrayByteSize(name) > IssueTransactionRequest.MaxAssetNameLength) {
            binding.name.setError(getString(R.string.name_too_long));
            isError = true;
        }

        if (getArrayByteSize(description) > IssueTransactionRequest.MaxDescriptionLength) {
            binding.description.setError(getString(R.string.description_too_long));
            isError = true;
        }

        int theDecimals = -1;
        long theQuantity = -1;

        if (decimals == null || Ints.tryParse(decimals) > IssueTransactionRequest.MaxDecimals
                || Ints.tryParse(decimals) < 0) {
            binding.decimals.setError(getString(R.string.decimals_error));
            isError = true;
        } else {
            theDecimals = Ints.tryParse(decimals);
            theQuantity = MoneyUtil.getUnscaledValue(quantity, theDecimals);
            if (theQuantity <= 0) {
                binding.quantity.setError(getString(R.string.invalid_quantity));
                isError = true;
            }
        }

        if (!isError) {
            request = new IssueTransactionRequest(NodeManager.get().getPublicKeyStr(),
                    name, description, theQuantity,
                    (byte)theDecimals, reissuable, System.currentTimeMillis());
        } else {
            request = null;
        }
    }


    public boolean signTransaction() {
        byte[] pk = AccessState.getInstance().getPrivateKey();
        if (pk != null) {
            request.sign(pk);
            return true;
        } else {
            return false;
        }
    }

    private void trackIssueAsset(IssueTransactionRequest request) {
        Map<String, Object> eventValue = new HashMap<String, Object>();
        eventValue.put("af_asset_name", request.name);
        eventValue.put("af_asset_id", request.id);
        AppsFlyerLib.getInstance().trackEvent(context, "af_issue_tx", eventValue);
    }

    public void submitIssue() {
        NodeManager.get().broadcastIssue(request)
            .compose(RxUtil.applySchedulersToObservable()).subscribe(tx -> {
            trackIssueAsset(tx);
            if (dataListener != null)
                dataListener.onShowTransactionSuccess(request);
        }, err -> {
            Log.e(TAG, "submitIssue: ", err);
            if (dataListener != null)
                ToastCustom.makeText(context, context.getString(R.string.transaction_failed), ToastCustom.LENGTH_LONG, ToastCustom.TYPE_ERROR);
        });

    }

    @Bindable
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Bindable
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        notifyPropertyChanged(BR.description);
    }

    @Bindable
    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
        notifyPropertyChanged(BR.quantity);
    }

    @Bindable
    public String getDecimals() {
        return decimals;
    }

    public void setDecimals(String decimals) {
        this.decimals = decimals;
        notifyPropertyChanged(BR.decimals);
    }

    @Bindable
    public boolean getReissuable() {
        return reissuable;
    }

    public void setReissuable(boolean reissuable) {
        this.reissuable = reissuable;
        notifyPropertyChanged(BR.reissuable);
    }
}
