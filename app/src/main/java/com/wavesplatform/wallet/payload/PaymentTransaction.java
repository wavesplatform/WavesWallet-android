package com.wavesplatform.wallet.payload;

public class PaymentTransaction extends TransferTransaction {

    public PaymentTransaction(int type, String id, String sender, long timestamp,
                              long amount, long fee, String recipient) {
        super(type, id, sender, timestamp, amount, fee, null, recipient, null);
    }


}
