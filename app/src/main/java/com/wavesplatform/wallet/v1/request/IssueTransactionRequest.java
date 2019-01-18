package com.wavesplatform.wallet.v1.request;

import android.util.Log;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;
import com.google.common.primitives.Shorts;
import com.wavesplatform.wallet.v1.crypto.Base58;
import com.wavesplatform.wallet.v1.crypto.CryptoProvider;
import com.wavesplatform.wallet.v1.crypto.Hash;
import com.wavesplatform.wallet.v1.payload.AssetBalance;
import com.wavesplatform.wallet.v1.payload.IssueTransaction;
import com.wavesplatform.wallet.v2.util.AddressUtil;

import org.apache.commons.lang3.ArrayUtils;

public class IssueTransactionRequest {
    public static int MaxDescriptionLength = 1000;
    public static int MinFee = 100000000;
    public static int MaxAssetNameLength = 16;
    public static int MinAssetNameLength = 4;
    public static int MaxDecimals = 8;

    final public String id;
    final public String senderPublicKey;
    final public String name;
    final public String description;
    final public long quantity;
    final public byte decimals;
    final public boolean reissuable;
    final public long fee;
    final public long timestamp;
    public String signature;

    public transient final int txType = 3;
    public transient final byte[] nameBytes;
    public transient final byte[] descriptionBytes;

    public IssueTransactionRequest(String senderPublicKey, String name, String description,
                                   long quantity, byte decimals, boolean reissuable, long timestamp) {
        this.senderPublicKey = senderPublicKey;
        this.nameBytes = name != null ? name.getBytes(org.apache.commons.io.Charsets.UTF_8) : ArrayUtils.EMPTY_BYTE_ARRAY;
        this.name = name;
        this.descriptionBytes = description != null ? description.getBytes(org.apache.commons.io.Charsets.UTF_8) : ArrayUtils.EMPTY_BYTE_ARRAY;
        this.description = description != null ? description : "";
        this.quantity = quantity;
        this.decimals = decimals;
        this.reissuable = reissuable;
        this.fee = MinFee;
        this.timestamp = timestamp;
        this.id = Base58.encode(Hash.fastHash(toSignBytes()));
    }

    private static byte[] arrayWithSize(byte[] b) {
        return Bytes.concat(Shorts.toByteArray((short) b.length), b);
    }

    public byte[] toSignBytes() {
        try {
            byte[] reissuableBytes = reissuable ? new byte[]{1} : new byte[]{0};

            return Bytes.concat(new byte[]{txType},
                    Base58.decode(senderPublicKey),
                    arrayWithSize(nameBytes),
                    arrayWithSize(descriptionBytes),
                    Longs.toByteArray(quantity),
                    new byte[]{decimals},
                    reissuableBytes,
                    Longs.toByteArray(fee),
                    Longs.toByteArray(timestamp));
        } catch (Exception e) {
            Log.e("Wallet", "Couldn't create issue transaction sign", e);
            return new byte[0];
        }
    }

    public void sign(byte[] privateKey) {
        if (signature == null) {
            signature = Base58.encode(CryptoProvider.sign(privateKey, toSignBytes()));
        }
    }
}
