package com.wavesplatform.wallet.payload;

import com.google.common.base.Optional;
import com.wavesplatform.wallet.api.NodeManager;
import com.wavesplatform.wallet.request.OrderRequest;
import com.wavesplatform.wallet.request.OrderType;
import com.wavesplatform.wallet.util.MoneyUtil;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.math.BigInteger;

public class ExchangeTransaction extends Transaction {


    private OrderRequest order1;
    private OrderRequest order2;
    public String amountAsset;
    public long sellMatcherFee;
    public long buyMatcherFee;
    public long price;

    public ExchangeTransaction() {
    }

    public ExchangeTransaction(int type, String id, String sender, long timestamp, OrderRequest order1, OrderRequest order2,
                               long sellMatcherFee, long buyMatcherFee, long price) {
        this.type = type;
        this.id = id;
        this.sender = sender;
        this.timestamp = timestamp;
        this.order1 = order1;
        this.order2 = order2;
        this.sellMatcherFee = sellMatcherFee;
        this.buyMatcherFee = buyMatcherFee;
        this.price = price;
    }

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

    private AssetBalance getPriceAssetBallance() {
        return NodeManager.get().getAssetBalance(getPriceAssetId());
    }

    public long getTransactionFee(){
        if (getMyOrder().orderType.equals(OrderType.buy)){
            return buyMatcherFee;
        } else
            return sellMatcherFee;
    }

    public String getDisplayPrice() {
        return MoneyUtil.getScaledPrice(price, getDecimals(), getPriceDecimals());
    }

    public String getDisplayPriceAmount(){
        return MoneyUtil.getTextStripZeros(MoneyUtil.getTextStripZeros(
                BigInteger.valueOf(amount)
                        .multiply(BigInteger.valueOf(price))
                        .divide(BigInteger.valueOf(100000000)).longValue(),
                getPriceDecimals()));
    }

    @Override
    public String getDisplayAmount() {
        if (getMyOrder().assetPair.priceAsset == null) {
            return MoneyUtil.getScaledText(amount, getAssetBallance());
        }
        return MoneyUtil.getDisplayWaves(amount);
    }

    public int getPriceDecimals() {
        if (getMyOrder().assetPair.priceAsset == null) {
            return 8;
        }
        return getPriceAssetBallance().issueTransaction.decimals;
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
