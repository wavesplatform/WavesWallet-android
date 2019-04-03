/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v1.payload;

import android.os.Parcel;
import android.os.Parcelable;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Market extends RealmObject implements Parcelable {

    @PrimaryKey
    public String id;

    public String amountAsset;
    public String amountAssetName;
    public String priceAsset;
    public String priceAssetName;

    public Boolean checked = false;
    public Boolean verified = false;
    public AmountAssetInfo amountAssetInfo;
    public PriceAssetInfo priceAssetInfo;
    public Integer currentTimeFrame;

    public Market() {
    }

    public Market(String amountAsset, String amountAssetName, String priceAsset, String priceAssetName, AmountAssetInfo amountAssetInfo, PriceAssetInfo priceAssetInfo) {
        this.amountAsset = amountAsset;
        this.amountAssetName = amountAssetName;
        this.priceAsset = priceAsset;
        this.priceAssetName = priceAssetName;
        this.amountAssetInfo = amountAssetInfo;
        this.priceAssetInfo = priceAssetInfo;
        this.id = amountAsset + priceAsset;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        Market market = (Market) o;

        if (amountAsset != null ? !amountAsset.equals(market.amountAsset) : market.amountAsset != null)
            return false;
        return priceAsset != null ? priceAsset.equals(market.priceAsset) : market.priceAsset == null;
    }

    @Override
    public int hashCode() {
        int result = amountAsset != null ? amountAsset.hashCode() : 0;
        result = 31 * result + (priceAsset != null ? priceAsset.hashCode() : 0);
        return result;
    }

    public AmountAssetInfo getAmountAssetInfo() {
        return amountAssetInfo != null ? amountAssetInfo : new AmountAssetInfo();
    }

    public PriceAssetInfo getPriceAssetInfo() {
        return priceAssetInfo != null ? priceAssetInfo : new PriceAssetInfo();
    }

    public void setAmountAsset(String amountAsset) {
        this.amountAsset = amountAsset;
    }

    public void setAmountAssetName(String amountAssetName) {
        this.amountAssetName = amountAssetName;
    }

    public void setPriceAsset(String priceAsset) {
        this.priceAsset = priceAsset;
    }

    public void setPriceAssetName(String priceAssetName) {
        this.priceAssetName = priceAssetName;
    }

    public void setAmountAssetInfo(AmountAssetInfo amountAssetInfo) {
        this.amountAssetInfo = amountAssetInfo;
    }

    public void setPriceAssetInfo(PriceAssetInfo priceAssetInfo) {
        this.priceAssetInfo = priceAssetInfo;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.amountAsset);
        dest.writeString(this.amountAssetName);
        dest.writeString(this.priceAsset);
        dest.writeString(this.priceAssetName);
        dest.writeValue(this.checked);
        dest.writeValue(this.verified);
        dest.writeParcelable(this.amountAssetInfo, flags);
        dest.writeParcelable(this.priceAssetInfo, flags);
        dest.writeValue(this.currentTimeFrame);
        dest.writeString(this.id);
    }

    protected Market(Parcel in) {
        this.amountAsset = in.readString();
        this.amountAssetName = in.readString();
        this.priceAsset = in.readString();
        this.priceAssetName = in.readString();
        this.checked = (Boolean) in.readValue(Boolean.class.getClassLoader());
        this.verified = (Boolean) in.readValue(Boolean.class.getClassLoader());
        this.amountAssetInfo = in.readParcelable(AmountAssetInfo.class.getClassLoader());
        this.priceAssetInfo = in.readParcelable(PriceAssetInfo.class.getClassLoader());
        this.currentTimeFrame = (Integer) in.readValue(Integer.class.getClassLoader());
        this.id = in.readString();
    }

    public static final Creator<Market> CREATOR = new Creator<Market>() {
        @Override
        public Market createFromParcel(Parcel source) {
            return new Market(source);
        }

        @Override
        public Market[] newArray(int size) {
            return new Market[size];
        }
    };
}
