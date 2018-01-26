package com.wavesplatform.wallet.payload;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by anonymous on 01.07.17.
 */

public class Price implements Parcelable{
    public OrderBook.Asks asks;
    public OrderBook.Bids bids;

    public Price(OrderBook.Asks asks) {
        this.asks = asks;
    }

    public Price(OrderBook.Bids bids) {
        this.bids = bids;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Price price = (Price) o;

        if (asks != null ? !asks.equals(price.asks) : price.asks != null) return false;
        return bids != null ? bids.equals(price.bids) : price.bids == null;

    }

    @Override
    public int hashCode() {
        int result = asks != null ? asks.hashCode() : 0;
        result = 31 * result + (bids != null ? bids.hashCode() : 0);
        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.asks, flags);
        dest.writeParcelable(this.bids, flags);
    }

    protected Price(Parcel in) {
        this.asks = in.readParcelable(OrderBook.Asks.class.getClassLoader());
        this.bids = in.readParcelable(OrderBook.Bids.class.getClassLoader());
    }

    public static final Creator<Price> CREATOR = new Creator<Price>() {
        @Override
        public Price createFromParcel(Parcel source) {
            return new Price(source);
        }

        @Override
        public Price[] newArray(int size) {
            return new Price[size];
        }
    };
}
