package com.wavesplatform.wallet.payload;

import android.os.Parcel;
import android.os.Parcelable;

import io.realm.RealmObject;

public class PriceAssetInfo extends RealmObject implements Parcelable {
    public int decimals = 8;

    public PriceAssetInfo(int decimals) {
        this.decimals = decimals;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.decimals);
    }

    public PriceAssetInfo() {
    }

    protected PriceAssetInfo(Parcel in) {
        this.decimals = in.readInt();
    }

    public static final Creator<PriceAssetInfo> CREATOR = new Creator<PriceAssetInfo>() {
        @Override
        public PriceAssetInfo createFromParcel(Parcel source) {
            return new PriceAssetInfo(source);
        }

        @Override
        public PriceAssetInfo[] newArray(int size) {
            return new PriceAssetInfo[size];
        }
    };
}