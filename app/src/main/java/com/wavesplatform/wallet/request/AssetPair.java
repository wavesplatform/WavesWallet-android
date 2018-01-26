package com.wavesplatform.wallet.request;

import android.util.Log;

import com.google.common.primitives.Bytes;
import com.wavesplatform.wallet.util.SignUtil;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class AssetPair {
    public String amountAsset;
    public String priceAsset;

    public AssetPair(String amountAsset, String priceAsset) {
        this.amountAsset = amountAsset;
        this.priceAsset = priceAsset;
    }

    public String getKey() {
        return amountAsset + "-" + priceAsset;
    }

    public byte[] toBytes() {
        try {
            return Bytes.concat(SignUtil.arrayOption(amountAsset),
                    SignUtil.arrayOption(priceAsset));
        } catch (Exception e) {
            Log.e("Wallet", "Couldn't create bytes for AssetPair: " + getKey(), e);
            return new byte[0];
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof AssetPair)) return false;

        AssetPair assetPair = (AssetPair) o;

        return new EqualsBuilder()
                .append(amountAsset, assetPair.amountAsset)
                .append(priceAsset, assetPair.priceAsset)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(amountAsset)
                .append(priceAsset)
                .toHashCode();
    }
}
