package com.wavesplatform.wallet.payload;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by anonymous on 01.07.17.
 */

public class OrderBook {

    @SerializedName("timestamp")
    public String timestamp;
    @SerializedName("pair")
    public Pair pair;
    @SerializedName("bids")
    public List<Bids> bids;
    @SerializedName("asks")
    public List<Asks> asks;

    public static class Pair {
        @SerializedName("amountAsset")
        public String amountAsset;
        @SerializedName("priceAsset")
        public String priceAsset;
    }

    public static class Bids implements Parcelable{
        @SerializedName("price")
        public long price;
        @SerializedName("amount")
        public String amount;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Bids bids = (Bids) o;

            if (price != bids.price) return false;
            return amount != null ? amount.equals(bids.amount) : bids.amount == null;

        }

        @Override
        public int hashCode() {
            int result = (int) (price ^ (price >>> 32));
            result = 31 * result + (amount != null ? amount.hashCode() : 0);
            return result;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeLong(this.price);
            dest.writeString(this.amount);
        }

        public Bids() {
        }

        protected Bids(Parcel in) {
            this.price = in.readLong();
            this.amount = in.readString();
        }

        public static final Creator<Bids> CREATOR = new Creator<Bids>() {
            @Override
            public Bids createFromParcel(Parcel source) {
                return new Bids(source);
            }

            @Override
            public Bids[] newArray(int size) {
                return new Bids[size];
            }
        };
    }

    public static class Asks implements Parcelable{
        @SerializedName("price")
        public long price;
        @SerializedName("amount")
        public String amount;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Asks asks = (Asks) o;

            if (price != asks.price) return false;
            return amount != null ? amount.equals(asks.amount) : asks.amount == null;

        }

        @Override
        public int hashCode() {
            int result = (int) (price ^ (price >>> 32));
            result = 31 * result + (amount != null ? amount.hashCode() : 0);
            return result;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeLong(this.price);
            dest.writeString(this.amount);
        }

        public Asks() {
        }

        protected Asks(Parcel in) {
            this.price = in.readLong();
            this.amount = in.readString();
        }

        public static final Creator<Asks> CREATOR = new Creator<Asks>() {
            @Override
            public Asks createFromParcel(Parcel source) {
                return new Asks(source);
            }

            @Override
            public Asks[] newArray(int size) {
                return new Asks[size];
            }
        };
    }
}
