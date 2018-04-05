package com.wavesplatform.wallet.v1.ui.dex.details.orderbook;

import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;

import com.wavesplatform.wallet.v1.payload.Price;

import java.util.List;

class PriceDiffUtil extends DiffUtil.Callback {

    private List<Price> oldPrices;
    private List<Price> newPrices;

    PriceDiffUtil(List<Price> oldPrice, List<Price> newPrices) {
        this.oldPrices = oldPrice;
        this.newPrices = newPrices;
    }

    @Override
    public int getOldListSize() {
        return oldPrices != null ? oldPrices.size() : 0;
    }

    @Override
    public int getNewListSize() {
        return newPrices != null ? newPrices.size() : 0;
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return oldPrices.get(oldItemPosition).equals(newPrices.get(newItemPosition));
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return oldPrices.get(oldItemPosition).equals(newPrices.get(newItemPosition));
    }

    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        return super.getChangePayload(oldItemPosition, newItemPosition);
    }
}
