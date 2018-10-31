package com.wavesplatform.wallet.v1.payload;

import com.google.common.base.Optional;
import com.wavesplatform.wallet.v1.api.NodeManager;
import com.wavesplatform.wallet.v1.request.OrderRequest;
import com.wavesplatform.wallet.v1.request.OrderType;
import com.wavesplatform.wallet.v1.util.MoneyUtil;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

public class ExchangeTransaction extends Transaction {


    private OrderRequest order1;
    private OrderRequest order2;
    public String amountAsset;
    public long sellMatcherFee;
    public long buyMatcherFee;
    public long price;


    public OrderRequest getMyOrder() {
        return order1.senderPublicKey.equals(NodeManager.get().getPublicKeyStr()) ? order1 : order2;
    }

    public String getAmountAssetId() {
        return getMyOrder().assetPair.amountAsset;
    }

    public String getPriceAssetId() {
        return getMyOrder().assetPair.priceAsset;
    }

    @Override
    public String getAssetName() {
        return getAmountAssetName();
    }

    public String getAmountAssetName() {
            return NodeManager.get().getAssetName(getMyOrder().assetPair.amountAsset);
    }

    public String getPriceAssetName() {
            return NodeManager.get().getAssetName(getMyOrder().assetPair.priceAsset);

    }

    private AssetBalance getAssetBallance() {
        return NodeManager.get().getAssetBalance(getAmountAssetId());
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

    @Override
    public int getDirection() {
        if (getMyOrder().orderType.equals(OrderType.buy))
            return RECEIVED;
        else
            return SENT;
    }

    @Override
    public boolean isForAsset(String assetId) {
       return StringUtils.equals(assetId, getMyOrder().assetPair.amountAsset)
               || StringUtils.equals(assetId, getMyOrder().assetPair.priceAsset);
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
