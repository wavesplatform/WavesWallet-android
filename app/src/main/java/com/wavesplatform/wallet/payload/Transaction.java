package com.wavesplatform.wallet.payload;

import com.google.common.base.Optional;
import com.wavesplatform.wallet.api.NodeManager;
import com.wavesplatform.wallet.util.MoneyUtil;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;

import java.math.BigDecimal;
import java.util.Objects;

public class Transaction {
    public int type;
    public String id;
    public String sender;
    public long timestamp;
    public long amount;
    public long fee;

    public static final int RECEIVED = 1;
    public static final int SENT = 2;

    public boolean isPending;

    public Transaction() {
    }

    public Transaction(int type, String id, String sender, long timestamp, long amount, long fee) {
        this.type = type;
        this.id = id;
        this.sender = sender;
        this.timestamp = timestamp;
        this.amount = amount;
        this.fee = fee;
    }

    public String getDisplayAmount() {
        return MoneyUtil.getDisplayWaves(amount);
    }

    public int getDecimals() {
        return 8;
    }

    public int getDirection() {
        if (sender.equals(NodeManager.get().getAddress()))  {
            return SENT;
        } else {
            return RECEIVED;
        }
    }

    public boolean isForAsset(String assetId) {
        return false;
    }

    public String getAssetName() {
        return "WAVES";
    }

    public Optional<String> getConterParty() {
        return Optional.absent();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return ObjectUtils.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return ObjectUtils.hashCode(id);
    }

    public byte[] toBytes() {
        return ArrayUtils.EMPTY_BYTE_ARRAY;
    }

    public boolean isOwn() {
        return ArrayUtils.isEquals(NodeManager.get().getAddress(), sender);
    }
}
