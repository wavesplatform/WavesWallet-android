package com.wavesplatform.wallet.ui.send;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.StringRes;
import android.util.Log;

import com.appsflyer.AppsFlyerLib;
import com.wavesplatform.wallet.R;
import com.wavesplatform.wallet.api.NodeManager;
import com.wavesplatform.wallet.data.access.AccessState;
import com.wavesplatform.wallet.data.rxjava.RxUtil;
import com.wavesplatform.wallet.injection.Injector;
import com.wavesplatform.wallet.payload.AssetBalance;
import com.wavesplatform.wallet.request.TransferTransactionRequest;
import com.wavesplatform.wallet.ui.assets.AssetsHelper;
import com.wavesplatform.wallet.ui.assets.ItemAccount;
import com.wavesplatform.wallet.ui.assets.PaymentConfirmationDetails;
import com.wavesplatform.wallet.ui.base.BaseViewModel;
import com.wavesplatform.wallet.ui.customviews.ToastCustom;
import com.wavesplatform.wallet.util.AddressUtil;
import com.wavesplatform.wallet.util.MoneyUtil;
import com.wavesplatform.wallet.util.PrefsUtil;
import com.wavesplatform.wallet.util.SSLVerifyUtil;
import com.wavesplatform.wallet.util.StringUtils;
import com.wavesplatform.wallet.util.annotations.Thunk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

@SuppressWarnings("WeakerAccess")
public class SendViewModel extends BaseViewModel {

    private final String TAG = getClass().getSimpleName();

    @Thunk
    DataListener dataListener;
    private Context context;

    public SendModel sendModel;

    @Inject
    PrefsUtil prefsUtil;
    @Inject
    AssetsHelper assetsHelper;
    @Inject
    SSLVerifyUtil sslVerifyUtil;
    @Inject
    StringUtils stringUtils;

    SendViewModel(Context context, DataListener dataListener) {

        Injector.getInstance().getDataManagerComponent().inject(this);

        this.context = context;
        this.dataListener = dataListener;

        sendModel = new SendModel();
        sendModel.defaultSeparator = MoneyUtil.getDefaultDecimalSeparator();

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

    String getDefaultSeparator() {
        return sendModel.defaultSeparator;
    }

    public interface DataListener {

        void onHideSendingAddressField();

        void onShowInvalidAmount();

        void onShowPaymentDetails(PaymentConfirmationDetails confirmationDetails);

        void onShowToast(@StringRes int message, @ToastCustom.ToastType String toastType);

        void onShowTransactionSuccess(TransferTransactionRequest signed);

        void finishPage();

        void hideKeyboard();

        void onSetSelection(int pos);
    }

    List<ItemAccount> getAssetsList() {

        ArrayList<ItemAccount> result = new ArrayList<ItemAccount>() {{
            addAll(assetsHelper.getAccountItems());
        }};

        if (result.size() == 1) {
            //Only a single account/address available in wallet
            if (dataListener != null)
                dataListener.onHideSendingAddressField();
            setSendingAssets(result.get(0));
        }

        return result;
    }

    void handleIncomingUri(String strUri) {
        try {
            if (strUri == null) return;

            Uri uri = Uri.parse(strUri);
            if (uri == null) return;

            sendModel.setDestinationAddress(uri.getHost());
            String assetParam = uri.getQueryParameter("asset");

            List<ItemAccount> assets = getAssetsList();
            AssetBalance selAsset = assets.get(0).accountObject;

            if (assetParam != null) {
                for (int i = 0; i < assets.size(); ++i) {
                    if (assetParam.equals(assets.get(i).accountObject.assetId)) {
                        selAsset = assets.get(i).accountObject;
                        if (dataListener != null) dataListener.onSetSelection(i);
                    }
                }
            }

            String amountParam = uri.getQueryParameter("amount");
            if (amountParam != null) {
                long amount = MoneyUtil.getUnscaledValue(amountParam, 0);
                sendModel.setAmount(MoneyUtil.getScaledText(amount, selAsset));
            }

            String attachment = uri.getQueryParameter("attachment");
            if (attachment != null) {
                sendModel.setAttachment(attachment);
            }
        } catch (UnsupportedOperationException e) {
            // TODO: 09.08.17 What is means? Need to reproduce. Hot fix
        }
    }

    /**
     * Wrapper for calculateTransactionAmounts
     */
    void spendAllClicked() {
        sendModel.setAmount(MoneyUtil.getScaledText(sendModel.maxAvailable, sendModel.sendingAsset));
    }


//    private boolean isSameSendingAndFeeAssets() {
//        if (sendModel.feeAsset != null && sendModel.sendingAsset != null)
//            return (sendModel.feeAsset.assetId == null && sendModel.sendingAsset.assetId == null) ||
//                    sendModel.feeAsset.assetId.equals(sendModel.sendingAsset.assetId);
//        else
//            return false;
//    }

    private boolean isSameSendingAndFeeAssets() {
        if (sendModel.feeAsset != null && sendModel.sendingAsset != null) {
            if (sendModel.feeAsset.assetId == null && sendModel.sendingAsset.assetId == null) {
                return true;
            } else {
                if (sendModel.feeAsset.assetId != null && sendModel.sendingAsset.assetId != null)
                    return sendModel.feeAsset.assetId.equals(sendModel.sendingAsset.assetId);
            }
        }
        return false;
    }

    /**
     * Update max available. Values are bound to UI, so UI will update automatically
     */
    public void updateMaxAvailable() {
        if (sendModel.sendingAsset != null) {
            sendModel.maxAvailable = isSameSendingAndFeeAssets() ? sendModel.sendingAsset.balance - sendModel.feeAmount :
                    sendModel.sendingAsset.balance;

            if (sendModel.maxAvailable <= 0 && context != null) {
                sendModel.setMaxAvailable(stringUtils.getString(R.string.insufficient_funds));
            } else {
                String amountFormatted = MoneyUtil.getScaledText(sendModel.maxAvailable, sendModel.sendingAsset);
                sendModel.setMaxAvailable(stringUtils.getString(R.string.max_available) + " " + amountFormatted + " " + sendModel.sendingAsset.getName());
            }
        }
    }

    void sendClicked() {
        if (dataListener != null)
            dataListener.hideKeyboard();

        int res = validateTransfer(sendModel.getTxRequest());
        if (res == 0) {
            confirmPayment();
        } else {
            if (dataListener != null)
                dataListener.onShowToast(res, ToastCustom.TYPE_ERROR);
        }
    }

    /**
     * Sets payment confirmation details to be displayed to user and fires callback to display
     * this.
     */
    @Thunk
    void confirmPayment() {
        PaymentConfirmationDetails details = PaymentConfirmationDetails.fromRequest(
                sendModel.sendingAsset, sendModel.getTxRequest());

        if (dataListener != null)
            dataListener.onShowPaymentDetails(details);
    }

    private boolean isFundSufficient(TransferTransactionRequest tx) {
        if (isSameSendingAndFeeAssets()) {
            return tx.amount + tx.fee <= sendModel.sendingAsset.balance;
        } else {
            return tx.amount <= sendModel.sendingAsset.balance &&
                    tx.fee <= NodeManager.get().getWavesBalance();
        }
    }

    private int validateTransfer(TransferTransactionRequest tx) {
        if (!AddressUtil.isValidAddress(tx.recipient)) {
            return R.string.invalid_address;
        } else if (tx.getAttachmentSize() > TransferTransactionRequest.MaxAttachmentSize) {
            return R.string.attachment_too_long;
        } else if (tx.amount <= 0) {
            return R.string.invalid_amount;
        } else if (tx.amount > Long.MAX_VALUE - tx.fee) {
            return R.string.invalid_amount;
        } else if (tx.fee <= 0 || tx.fee < TransferTransactionRequest.MinFee) {
            return R.string.insufficient_fee;
        } else if (NodeManager.get().getAddress().equals(tx.recipient)) {
            return R.string.send_to_same_address_warning;
        } else if (!isFundSufficient(tx)) {
            return R.string.insufficient_funds;
        }

        return 0;
    }

    void setSendingAssets(ItemAccount selectedItem) {
        if (selectedItem.accountObject != null) {
            sendModel.sendingAsset = selectedItem.accountObject;
            updateMaxAvailable();
        }
    }

    public TransferTransactionRequest signTransaction() {
        byte[] pk = AccessState.getInstance().getPrivateKey();
        if (pk != null) {
            TransferTransactionRequest signed = sendModel.getTxRequest();
            signed.sign(pk);
            return signed;
        } else {
            return null;
        }
    }

    private void trackSendPayment(TransferTransactionRequest request) {
        Map<String, Object> eventValue = new HashMap<String, Object>();
        eventValue.put("af_amount", request.amount);
        eventValue.put("af_asset_id", request.assetId);
        AppsFlyerLib.getInstance().trackEvent(context, "af_transfer_tx", eventValue);
    }

    public void submitPayment(TransferTransactionRequest signed) {
        NodeManager.get().broadcastTransfer(signed)
                .compose(RxUtil.applySchedulersToObservable()).subscribe(tx -> {
            trackSendPayment(signed);
            if (dataListener != null)
                dataListener.onShowTransactionSuccess(signed);
        }, err -> {
            Log.e(TAG, "submitPayment: ", err);
            if (dataListener != null)
                dataListener.onShowToast(R.string.transaction_failed, ToastCustom.TYPE_ERROR);
        });

    }

    public PrefsUtil getPrefsUtil() {
        return prefsUtil;
    }
}
