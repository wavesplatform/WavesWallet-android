package com.wavesplatform.wallet.request;

import android.util.Log;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;
import com.wavesplatform.wallet.crypto.Base58;
import com.wavesplatform.wallet.crypto.CryptoProvider;

public class MyOrdersRequest {
    public String senderPublicKey;
    public long timestamp;
    public String signature;

    public MyOrdersRequest(String senderPublicKey) {
        this.senderPublicKey = senderPublicKey;
        this.timestamp = System.currentTimeMillis();
    }

    public byte[] toSignBytes() {
        try {
            return Bytes.concat(
                    Base58.decode(senderPublicKey),
                    Longs.toByteArray(timestamp)
            );
        } catch (Exception e) {
            Log.e("Wallet", "Couldn't create MyOrdersRequest bytes", e);
            return new byte[0];
        }
    }

    public void sign(byte[] privateKey)  {
        if (signature == null) {
            signature = Base58.encode(CryptoProvider.sign(privateKey, toSignBytes()));
        }
    }

    public String getSignature() {
        return signature;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
