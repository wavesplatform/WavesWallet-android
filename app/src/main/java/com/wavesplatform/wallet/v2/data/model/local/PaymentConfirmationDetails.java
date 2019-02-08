package com.wavesplatform.wallet.v2.data.model.local;

import com.wavesplatform.sdk.WavesWallet;
import com.wavesplatform.wallet.App;
import com.wavesplatform.sdk.utils.MoneyUtil;
import com.wavesplatform.sdk.model.request.TransactionsBroadcastRequest;
import com.wavesplatform.sdk.model.response.AssetBalance;


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



    public static PaymentConfirmationDetails fromRequest(AssetBalance ab, TransactionsBroadcastRequest req) {
        PaymentConfirmationDetails d = new PaymentConfirmationDetails();
        WavesWallet wallet = App.getAccessManager().getWallet();
        if (wallet != null) {
            d.fromLabel = wallet.getAddress();
        }
        d.toLabel = req.getRecipient();
        d.amount = MoneyUtil.Companion.getScaledText(req.getAmount(), ab);
        d.fee = MoneyUtil.Companion.getDisplayWaves(req.getFee());
        d.amountUnit = ab.getName();
        d.feeUnit = "WAVES";
        return d;
    }
}
