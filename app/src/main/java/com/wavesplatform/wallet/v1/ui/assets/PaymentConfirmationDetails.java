package com.wavesplatform.wallet.v1.ui.assets;

import com.wavesplatform.wallet.App;
import com.wavesplatform.wallet.v1.request.TransferTransactionRequest;
import com.wavesplatform.wallet.v1.util.MoneyUtil;
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance;

public class PaymentConfirmationDetails {

    public String fromLabel;
    public String toLabel;
    public String amountUnit;
    public String amount;
    public String fee;
    public String feeUnit;

    @Override
    public String toString() {
        return "PaymentConfirmationDetails{" +
                "fromLabel='" + fromLabel + '\'' +
                ", toLabel='" + toLabel + '\'' +
                ", amountUnit='" + amountUnit + '\'' +
                ", amount='" + amount + '\'' +
                ", fee='" + fee + '\'' +
                ", feeUnit='" + feeUnit + '\'' +
                '}';
    }

    // todo to remove
    public static PaymentConfirmationDetails fromRequest(
            com.wavesplatform.wallet.v1.payload.AssetBalance ab, TransferTransactionRequest req) {
        PaymentConfirmationDetails d = new PaymentConfirmationDetails();
        d.fromLabel = App.getAccessManager().getWallet().getAddress();
        d.toLabel = req.recipient;
        d.amount = MoneyUtil.getScaledText(req.amount, ab);
        d.fee = MoneyUtil.getDisplayWaves(req.fee);
        d.amountUnit = ab.getName();
        d.feeUnit = "WAVES";
        return d;

    }

    public static PaymentConfirmationDetails fromRequest(AssetBalance ab, TransferTransactionRequest req) {
        PaymentConfirmationDetails d = new PaymentConfirmationDetails();
        d.fromLabel = App.getAccessManager().getWallet().getAddress();
        d.toLabel = req.recipient;
        d.amount = MoneyUtil.getScaledText(req.amount, ab);
        d.fee = MoneyUtil.getDisplayWaves(req.fee);
        d.amountUnit = ab.getName();
        d.feeUnit = "WAVES";
        return d;

    }
}
