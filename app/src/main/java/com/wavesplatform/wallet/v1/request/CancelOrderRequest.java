/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v1.request;

import android.util.Log;

import com.google.common.primitives.Bytes;
import com.wavesplatform.wallet.v1.crypto.Base58;
import com.wavesplatform.wallet.v1.crypto.CryptoProvider;

public class CancelOrderRequest {
    public String sender;
    public String orderId;
    public String signature;

    public CancelOrderRequest(String sender, String orderId) {
        this.sender = sender;
        this.orderId = orderId;
    }

    public CancelOrderRequest(String sender) {
        this.sender = sender;
    }

    public byte[] toSignBytes() {
        try {
            return Bytes.concat(
                    Base58.decode(sender),
                    Base58.decode(orderId)
                    );
        } catch (Exception e) {
            Log.e("Wallet", "Couldn't create CancelOrderRequest bytes", e);
            return new byte[0];
        }
    }

    public void sign(byte[] privateKey)  {
        signature = Base58.encode(CryptoProvider.sign(privateKey, toSignBytes()));
    }
}
