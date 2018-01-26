package com.wavesplatform.wallet.request;

import android.util.Log;

import com.google.common.base.Charsets;
import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;
import com.google.common.primitives.Shorts;
import com.wavesplatform.wallet.crypto.Base58;
import com.wavesplatform.wallet.crypto.CryptoProvider;
import com.wavesplatform.wallet.crypto.Hash;
import com.wavesplatform.wallet.payload.TransferTransaction;
import com.wavesplatform.wallet.util.AddressUtil;
import com.wavesplatform.wallet.util.SignUtil;

public class TransferTransactionRequest {
    public static int SignatureLength = 64;
    public static int KeyLength = 32;
    public static int MaxAttachmentSize = 140;
    public static int MinFee = 100000;

    public String assetId;
    public String senderPublicKey;
    public String recipient;
    public long amount;
    public long timestamp;
    public String feeAssetId;
    public long fee;
    public String attachment;
    public String signature;

    public transient final int txType = 4;

    public TransferTransactionRequest() {
    }

    public TransferTransactionRequest(String assetId, String senderPublicKey, String recipient, long amount,
                                      long timestamp, long fee, String attachment) {
        this.assetId = assetId;
        this.senderPublicKey = senderPublicKey;
        this.recipient = recipient;
        this.amount = amount;
        this.timestamp = timestamp;
        this.fee = fee;
        this.attachment = attachment != null ? Base58.encode(attachment.getBytes(Charsets.UTF_8)) : null;
    }

    public byte[] toSignBytes() {
        try {
            byte[] timestampBytes  = Longs.toByteArray(timestamp);
            byte[] assetIdBytes = SignUtil.arrayOption(assetId);
            byte[] amountBytes     = Longs.toByteArray(amount);
            byte[] feeAssetIdBytes = SignUtil.arrayOption(feeAssetId);
            byte[] feeBytes        = Longs.toByteArray(fee);

            return Bytes.concat(new byte[] {txType},
                    Base58.decode(senderPublicKey),
                    assetIdBytes,
                    feeAssetIdBytes,
                    timestampBytes,
                    amountBytes,
                    feeBytes,
                    Base58.decode(recipient),
                    SignUtil.arrayWithSize(attachment));
        } catch (Exception e) {
            Log.e("Wallet", "Couldn't create seed", e);
            return new byte[0];
        }
    }

    public void sign(byte[] privateKey)  {
        if (signature == null) {
            signature = Base58.encode(CryptoProvider.sign(privateKey, toSignBytes()));
        }
    }

    public int getAttachmentSize() {
        if (attachment == null) {
            return 0;
        }
        try {
            return Base58.decode(attachment).length;
        } catch (Base58.InvalidBase58 invalidBase58) {
            invalidBase58.printStackTrace();
            return 0;
        }
    }

    public TransferTransaction createDisplayTransaction() {
        TransferTransaction tt = new TransferTransaction(4, Base58.encode(Hash.fastHash(toSignBytes())),
                AddressUtil.addressFromPublicKey(senderPublicKey), timestamp, amount, fee,
                assetId, recipient, attachment);
        tt.isPending = true;
        return tt;
    }
}
