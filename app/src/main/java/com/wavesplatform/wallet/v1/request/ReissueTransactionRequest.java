package com.wavesplatform.wallet.v1.request;

import android.util.Log;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;
import com.wavesplatform.wallet.v1.crypto.Base58;
import com.wavesplatform.wallet.v1.crypto.CryptoProvider;
import com.wavesplatform.wallet.v1.crypto.Hash;
import com.wavesplatform.wallet.v1.payload.ReissueTransaction;
import com.wavesplatform.wallet.v2.util.AddressUtil;

public class ReissueTransactionRequest {
    public static int MinFee = 100000000;

    final public String assetId;
    final public String senderPublicKey;
    final public long quantity;
    final public boolean reissuable;
    final public long fee;
    final public long timestamp;
    public String signature;

    public transient final int txType = 5;

    public ReissueTransactionRequest(String assetId, String senderPublicKey, long quantity,
                                     boolean reissuable, long timestamp) {
        this.assetId = assetId;
        this.senderPublicKey = senderPublicKey;
        this.quantity = quantity;
        this.reissuable = reissuable;
        this.fee = MinFee;
        this.timestamp = timestamp;
    }

    public byte[] toSignBytes() {
        try {
            byte[] reissuableBytes = reissuable ? new byte[]{1} : new byte[]{0};

            return Bytes.concat(new byte[]{txType},
                    Base58.decode(senderPublicKey),
                    Base58.decode(assetId),
                    Longs.toByteArray(quantity),
                    reissuableBytes,
                    Longs.toByteArray(fee),
                    Longs.toByteArray(timestamp));
        } catch (Exception e) {
            Log.e("ReissueRequest", "Couldn't create toSignBytes", e);
            return new byte[0];
        }
    }

    public void sign(byte[] privateKey) {
        if (signature == null) {
            signature = Base58.encode(CryptoProvider.sign(privateKey, toSignBytes()));
        }
    }
}
