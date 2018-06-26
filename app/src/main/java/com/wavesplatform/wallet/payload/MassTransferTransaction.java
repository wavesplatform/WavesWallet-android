package com.wavesplatform.wallet.payload;

import com.google.common.base.Optional;
import com.wavesplatform.wallet.api.NodeManager;
import com.wavesplatform.wallet.util.MoneyUtil;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;

import java.util.List;

public class MassTransferTransaction extends Transaction {
    public static final class Transfer {
        String recipient;
        long amount;
    }

    public void init() {
        this.amount = getSum();
    }

    public String assetId;
    public String attachment;
    public List<Transfer> transfers;

    @Override
    public boolean isForAsset(String assetId) {
        return ObjectUtils.equals(this.assetId, assetId);
    }

    @Override
    public String getAssetName() {
        return assetId != null ? NodeManager.get().getAssetName(assetId) : "WAVES";
    }

    public long getSum() {
        long sum = 0;
        for (Transfer t: transfers) {
            sum += t.amount;
        }

        return sum;
    }

    public String getRecipient() {
        String recipient = "Unknown";
        if (transfers.size() > 0) {
            recipient = transfers.get(0).recipient;
        }

        return recipient;
    }

    @Override
    public String getDisplayAmount() {
        return MoneyUtil.getScaledText(getSum(), NodeManager.get().getAssetBalance(assetId));
    }

    @Override
    public int getDecimals() {
        return assetId != null ? NodeManager.get().getAssetBalance(assetId).issueTransaction.decimals : 8;
    }

    @Override
    public Optional<String> getConterParty() {
        return Optional.of(getDirection() == SENT ? getRecipient() : sender);
    }

    @Override
    public boolean isOwn() {
        return ArrayUtils.isEquals(NodeManager.get().getAddress(), sender)
                || ArrayUtils.isEquals(NodeManager.get().getAddress(), getRecipient());
    }

    @Override
    public String getAssetId() {
        return assetId;
    }
}
