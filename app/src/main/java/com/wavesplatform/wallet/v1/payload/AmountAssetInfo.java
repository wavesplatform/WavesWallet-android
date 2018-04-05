package com.wavesplatform.wallet.v1.payload;

import android.os.Parcel;
import android.os.Parcelable;

import io.realm.RealmObject;

public class AmountAssetInfo extends RealmObject implements Parcelable {
    public int decimals = 8;

    public AmountAssetInfo(int decimals) {
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

    public AmountAssetInfo() {
    }

    protected AmountAssetInfo(Parcel in) {
        this.decimals = in.readInt();
    }

    public static final Creator<AmountAssetInfo> CREATOR = new Creator<AmountAssetInfo>() {
        @Override
        public AmountAssetInfo createFromParcel(Parcel source) {
            return new AmountAssetInfo(source);
        }

        @Override
        public AmountAssetInfo[] newArray(int size) {
            return new AmountAssetInfo[size];
        }
    };
}