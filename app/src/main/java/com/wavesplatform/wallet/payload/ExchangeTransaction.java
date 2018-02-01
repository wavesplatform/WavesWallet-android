package com.wavesplatform.wallet.payload;

import com.google.common.base.Optional;
import com.wavesplatform.wallet.api.NodeManager;
import com.wavesplatform.wallet.request.OrderRequest;
import com.wavesplatform.wallet.request.OrderType;
import com.wavesplatform.wallet.util.MoneyUtil;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;

public class ExchangeTransaction extends Transaction {


    public OrderRequest order1;
    public OrderRequest order2;

    public ExchangeTransaction() {
    }

    public ExchangeTransaction(int type, String id, String sender, long timestamp, long fee, OrderRequest order1, OrderRequest order2) {
        this.type = type;
        this.id = id;
        this.sender = sender;
        this.timestamp = timestamp;
        this.fee = fee;
        this.order1 = order1;
        this.order2 = order2;
    }

    private OrderRequest getMyOrder() {
        return order1.senderPublicKey.equals(NodeManager.get().getPublicKeyStr()) ? order1 : order2;
    }

    private AssetBalance getAssetBallance() {
        return NodeManager.get().getAssetBalance(getMyOrder().assetPair.amountAsset);
    }

    @Override
    public String getDisplayAmount() {
        if (getMyOrder().assetPair.priceAsset == null) {
            return MoneyUtil.getScaledText(amount, getAssetBallance());
        }
        return MoneyUtil.getDisplayWaves(amount);
    }

    @Override
    public int getDecimals() {
        if (getMyOrder().assetPair.priceAsset == null) {
            return getAssetBallance().issueTransaction.decimals;
        }
        return 8;
    }

    public int getDirection() {
        if (getMyOrder().orderType.equals(OrderType.buy))
            return RECEIVED;
        else
            return SENT;
    }

    public boolean isForAsset(String assetId) {
        return false;
    }

    @Override
    public String getAssetName() {
        if (getMyOrder().assetPair.priceAsset == null) {
            return NodeManager.get().getAssetName(getMyOrder().assetPair.amountAsset);
        } else {
            return "WAVES";
        }
    }

    public Optional<String> getConterParty() {
        return Optional.absent();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExchangeTransaction that = (ExchangeTransaction) o;
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
