package com.wavesplatform.wallet.ui.send;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

import com.wavesplatform.wallet.BR;
import com.wavesplatform.wallet.api.NodeManager;
import com.wavesplatform.wallet.payload.AssetBalance;
import com.wavesplatform.wallet.request.TransferTransactionRequest;
import com.wavesplatform.wallet.util.MoneyUtil;

public class SendModel extends BaseObservable {

    //Views
    private String destinationAddress;
    private String maxAvailableString;
    private String customFee;
    private String amount;
    private String attachment;

    public AssetBalance sendingAsset;
    public AssetBalance feeAsset = NodeManager.get().wavesAsset;

    public String defaultSeparator;//Decimal separator based on locale

    public long maxAvailable;
    public long feeAmount;

    public String verifiedSecondPassword;

    public TransferTransactionRequest getTxRequest() {
        TransferTransactionRequest tx = new TransferTransactionRequest(
                sendingAsset.assetId,
                NodeManager.get().getPublicKeyStr(),
                destinationAddress,
                MoneyUtil.getUnscaledValue(amount, sendingAsset),
                System.currentTimeMillis(),
                MoneyUtil.getUnscaledWaves(customFee),
                attachment);
        return tx;
    }

    //Vars used for warning user of large tx
    public static final int LARGE_TX_SIZE = 516;//kb
    public static final long LARGE_TX_FEE = 80000;//USD
    public static final double LARGE_TX_PERCENTAGE = 1.0;//%

    @Bindable
    public String getDestinationAddress() {
        return destinationAddress;
    }

    public void setDestinationAddress(String destinationAddress) {
        this.destinationAddress = destinationAddress;
        notifyPropertyChanged(BR.destinationAddress);
    }

    @Bindable
    public String getMaxAvailable() {
        return maxAvailableString;
    }

    public void setMaxAvailable(String maxAvailable) {
        maxAvailableString = maxAvailable;
        notifyPropertyChanged(BR.maxAvailable);
    }

    @Bindable
    public String getCustomFee() {
        return customFee;
    }

    public void setCustomFee(String customFee) {
        this.customFee = customFee;
        notifyPropertyChanged(BR.customFee);
    }

    @Bindable
    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
        notifyPropertyChanged(BR.amount);
    }

    @Bindable
    public String getAttachment() {
        return attachment;
    }

    public void setAttachment(String attachment) {
        this.attachment = attachment;
        notifyPropertyChanged(BR.attachment);
    }

}