package com.wavesplatform.wallet.v1.request;

import android.util.Log;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;
import com.wavesplatform.wallet.v1.crypto.Base58;
import com.wavesplatform.wallet.v1.crypto.CryptoProvider;

public class OrderRequest {

    public static int MinFee = 300000;

    public String senderPublicKey;
    public String matcherPublicKey;
    public AssetPair assetPair;
    public OrderType orderType;

    public long price;
    public long amount;

    public long timestamp;
    public long expiration;

    public long matcherFee;

    public String signature;

    public OrderRequest() {
    }

    public OrderRequest(String senderPublicKey, String matcherPublicKey, AssetPair assetPair,
                        OrderType orderType, long price, long amount) {
        this.senderPublicKey = senderPublicKey;
        this.matcherPublicKey = matcherPublicKey;
        this.assetPair = assetPair;
        this.orderType = orderType;
        this.price = price;
        this.amount = amount;
        this.timestamp = System.currentTimeMillis();
        this.expiration = this.timestamp + 29L*24*60*60*1000;
        this.matcherFee = MinFee;
    }

    public OrderRequest(String senderPublicKey, AssetPair assetPair) {
        this.senderPublicKey = senderPublicKey;
        this.assetPair = assetPair;
        this.timestamp = System.currentTimeMillis();
        this.expiration = this.timestamp + 29L*24*60*60*1000;
        this.matcherFee = MinFee;
    }

    public byte[] toSignBytes() {
        try {
            return Bytes.concat(
                    Base58.decode(senderPublicKey),
                    Base58.decode(matcherPublicKey),
                    assetPair.toBytes(),
                    orderType.toBytes(),
                    Longs.toByteArray(price),
                    Longs.toByteArray(amount),
                    Longs.toByteArray(timestamp),
                    Longs.toByteArray(expiration),
                    Longs.toByteArray(matcherFee));
        } catch (Exception e) {
            Log.e("Wallet", "Couldn't create seed", e);
            return new byte[0];
        }
    }

    public void sign(byte[] privateKey)  {
        signature = Base58.encode(CryptoProvider.sign(privateKey, toSignBytes()));
    }
}
