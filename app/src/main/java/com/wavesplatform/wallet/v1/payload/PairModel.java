package com.wavesplatform.wallet.v1.payload;

import android.os.Parcel;
import android.os.Parcelable;

public class PairModel implements Parcelable{

    private String amountAssetName;
    private String amountAssetId;
    private String priceAssetName;
    private String priceAssetId;

    public PairModel() {
    }

    public PairModel(String amountAssetName, String amountAssetId, String priceAssetName, String priceAssetId) {
        this.amountAssetName = amountAssetName;
        this.amountAssetId = amountAssetId;
        this.priceAssetName = priceAssetName;
        this.priceAssetId = priceAssetId;
    }

    public void setAmountAssetName(String amountAssetName) {
        this.amountAssetName = amountAssetName;
    }

    public void setAmountAssetId(String amountAssetId) {
        this.amountAssetId = amountAssetId;
    }

    public void setPriceAssetName(String priceAssetName) {
        this.priceAssetName = priceAssetName;
    }

    public void setPriceAssetId(String priceAssetId) {
        this.priceAssetId = priceAssetId;
    }

    public String getAmountAssetName() {
        return amountAssetName;
    }

    public String getAmountAssetId() {
        return amountAssetId;
    }

    public String getPriceAssetName() {
        return priceAssetName;
    }

    public String getPriceAssetId() {
        return priceAssetId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.amountAssetName);
        dest.writeString(this.amountAssetId);
        dest.writeString(this.priceAssetName);
        dest.writeString(this.priceAssetId);
    }

    protected PairModel(Parcel in) {
        this.amountAssetName = in.readString();
        this.amountAssetId = in.readString();
        this.priceAssetName = in.readString();
        this.priceAssetId = in.readString();
    }

    public static final Creator<PairModel> CREATOR = new Creator<PairModel>() {
        @Override
        public PairModel createFromParcel(Parcel source) {
            return new PairModel(source);
        }

        @Override
        public PairModel[] newArray(int size) {
            return new PairModel[size];
        }
    };
}
