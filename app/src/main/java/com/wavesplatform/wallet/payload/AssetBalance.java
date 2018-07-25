package com.wavesplatform.wallet.payload;

import com.wavesplatform.wallet.util.MoneyUtil;

import org.apache.commons.lang3.ArrayUtils;

import java.util.Objects;

public class AssetBalance {
    public String assetId;
    public long balance;
    public boolean reissuable;
    public long quantity;
    public IssueTransaction issueTransaction;

    public transient boolean isPending;

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

    public static AssetBalance fromTransactionInfo(TransactionsInfo ti) {
        AssetBalance ab = new AssetBalance();
        ab.assetId = ti.assetId;
        ab.balance = 0;
        ab.reissuable = ti.reissuable;
        ab.quantity = ti.quantity;
        ab.issueTransaction = new IssueTransaction(3, ti.id, ti.sender, Long.valueOf(ti.timestamp), ti.quantity, ti.fee,
                ti.name, ti.description, ti.quantity, ti.decimals, ti.reissuable);

        return ab;
    }
}
