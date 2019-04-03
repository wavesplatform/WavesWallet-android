/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v1.payload;

import com.wavesplatform.wallet.v1.api.NodeManager;
import com.wavesplatform.wallet.v1.util.MoneyUtil;

public class ReissueTransaction extends Transaction {
    public String assetId;
    public long quantity;
    public boolean reissuable;

    public ReissueTransaction(int type, String id, String sender, String assetId, long timestamp,
                              long amount, long fee, long quantity, boolean reissuable) {
        super(type, id, sender, timestamp, amount, fee);
        this.assetId = assetId;
        this.quantity = quantity;
        this.reissuable = reissuable;
    }

    @Override
    public String getAssetName() {
        return assetId != null ? NodeManager.get().getAssetName(assetId) : super.getAssetName();
    }

    @Override
    public boolean isForAsset(String assetId) {
        return this.assetId.equals(assetId);
    }

    @Override
    public int getDecimals() {
        return assetId != null ? NodeManager.get().getAssetBalance(assetId).issueTransaction.decimals : getDecimals();
    }

    @Override
    public String getDisplayAmount() {
        return MoneyUtil.getScaledText(quantity, getDecimals());
    }

    @Override
    public int getDirection() {
        return RECEIVED;
    }
}
