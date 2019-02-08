package com.wavesplatform.wallet.v1.ui.assets;

import com.wavesplatform.sdk.WavesWallet;
import com.wavesplatform.wallet.App;
import com.wavesplatform.wallet.v1.util.MoneyUtil;
import com.wavesplatform.wallet.v2.data.model.remote.request.TransactionsBroadcastRequest;
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



    public static PaymentConfirmationDetails fromRequest(AssetBalance ab, TransactionsBroadcastRequest req) {
        PaymentConfirmationDetails d = new PaymentConfirmationDetails();
        WavesWallet wallet = App.getAccessManager().getWallet();
        if (wallet != null) {
            d.fromLabel = wallet.getAddress();
        }
        d.toLabel = req.getRecipient();
        d.amount = MoneyUtil.getScaledText(req.getAmount(), ab);
        d.fee = MoneyUtil.getDisplayWaves(req.getFee());
        d.amountUnit = ab.getName();
        d.feeUnit = "WAVES";
        return d;
    }
}
