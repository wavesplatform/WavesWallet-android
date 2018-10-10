package com.wavesplatform.wallet.v1.payload;

import com.wavesplatform.wallet.v1.util.MoneyUtil;

import org.apache.commons.lang3.ArrayUtils;

public class AssetBalance {
    public String assetId;
    public long balance;
    public boolean reissuable;
    public long quantity;
    public IssueTransaction issueTransaction;

    public transient boolean isPending;

    public AssetBalance() {

    }

    public AssetBalance(String assetId, long balance, boolean reissuable, long quantity, IssueTransaction issueTransaction) {
        this.assetId = assetId;
        this.balance = balance;
        this.reissuable = reissuable;
        this.quantity = quantity;
        this.issueTransaction = issueTransaction;
    }

    public String getDisplayBalance() {
        return MoneyUtil.getScaledText(balance, this);
    }

    public boolean isAssetId(String assetId) {
        return ArrayUtils.isEquals(this.assetId, assetId);
    }

    public String getName() {
        return issueTransaction.name;
    }

    public String getDisplayBalanceWithUnit() {
        return getDisplayBalance() + " " + getName();
    }

    public int getDecimals() {
        return issueTransaction != null ? issueTransaction.decimals : 8;
    }
    public boolean isWaves() {
        return assetId == null;
    }
}
