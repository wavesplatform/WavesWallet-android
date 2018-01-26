package com.wavesplatform.wallet.ui.assets;

import com.wavesplatform.wallet.api.NodeManager;
import com.wavesplatform.wallet.util.MoneyUtil;
import com.wavesplatform.wallet.payload.AssetBalance;
import com.wavesplatform.wallet.request.TransferTransactionRequest;

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

    public static PaymentConfirmationDetails fromRequest(AssetBalance ab, TransferTransactionRequest req) {
        PaymentConfirmationDetails d = new PaymentConfirmationDetails();
        d.fromLabel = NodeManager.get().getAddress();
        d.toLabel = req.recipient;
        d.amount = MoneyUtil.getScaledText(req.amount, ab);
        d.fee = MoneyUtil.getDisplayWaves(req.fee);
        d.amountUnit = ab.getName();
        d.feeUnit = "WAVES";
        return d;

    }
}
