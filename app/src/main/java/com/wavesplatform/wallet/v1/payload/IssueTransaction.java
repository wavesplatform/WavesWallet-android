package com.wavesplatform.wallet.v1.payload;

import com.wavesplatform.wallet.v1.util.MoneyUtil;

public class IssueTransaction extends Transaction {
    public String name;
    public String description;
    public long quantity;
    public int decimals;
    public boolean reissuable;

    public IssueTransaction() {
    }

    public IssueTransaction(int type, String id, String sender, long timestamp,
                            long amount, long fee, String name, String description,
                            long quantity, int decimals, boolean reissuable) {
        super(type, id, sender, timestamp, amount, fee);
        this.name = name;
        this.description = description;
        this.quantity = quantity;
        this.decimals = decimals;
        this.reissuable = reissuable;
    }

    @Override
    public String getAssetName() {
        return name;
    }

    @Override
    public boolean isForAsset(String assetId) {
        return this.id.equals(assetId);
    }

    @Override
    public int getDecimals() {
        return decimals;
    }

    @Override
    public String getDisplayAmount() {
        return MoneyUtil.getScaledText(quantity, decimals);
    }

    @Override
    public int getDirection() {
        return RECEIVED;
    }
}
