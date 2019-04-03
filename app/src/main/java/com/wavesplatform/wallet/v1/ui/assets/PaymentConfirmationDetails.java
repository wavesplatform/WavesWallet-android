/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v1.ui.assets;

import com.wavesplatform.wallet.App;
import com.wavesplatform.wallet.v1.data.auth.WavesWallet;
import com.wavesplatform.wallet.v1.request.TransferTransactionRequest;
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
