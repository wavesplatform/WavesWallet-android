package com.wavesplatform.wallet.payload;

import com.google.common.base.Optional;
import com.wavesplatform.wallet.api.NodeManager;
import com.wavesplatform.wallet.util.MoneyUtil;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;

public class TransferTransaction extends Transaction {
    public String assetId;
    public String recipient;
    public String attachment;

    public TransferTransaction(int type, String id, String sender, long timestamp, long amount,
                               long fee, String assetId, String recipient, String attachment) {
        super(type, id, sender, timestamp, amount, fee);
        this.assetId = assetId;
        this.recipient = recipient;
        this.attachment = attachment;
    }

    @Override
    public boolean isForAsset(String assetId) {
        return ObjectUtils.equals(this.assetId, assetId);
    }

    @Override
    public String getAssetName() {
        return assetId != null ? NodeManager.get().getAssetName(assetId) : "WAVES";
    }

    @Override
    public String getDisplayAmount() {
        return MoneyUtil.getScaledText(amount, NodeManager.get().getAssetBalance(assetId));
    }

    @Override
    public int getDecimals() {
        return assetId != null ? NodeManager.get().getAssetBalance(assetId).issueTransaction.decimals : 8;
    }

    @Override
    public Optional<String> getConterParty() {
        return Optional.of(getDirection() == SENT ? recipient : sender);
    }

    @Override
    public boolean isOwn() {
        return ArrayUtils.isEquals(NodeManager.get().getAddress(), sender)
                || ArrayUtils.isEquals(NodeManager.get().getAddress(), recipient);
    }

    @Override
    public String getAssetId() {
        return assetId;
    }
}
