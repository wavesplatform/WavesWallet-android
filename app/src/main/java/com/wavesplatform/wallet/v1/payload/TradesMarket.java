package com.wavesplatform.wallet.v1.payload;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

public class TradesMarket implements Parcelable{
    public String timestamp = String.valueOf(new Date().getTime());
    public String id = "0.0";
    public boolean confirmed = false;
    public String type;
    public String price = "0.0";
    public String amount = "0.0";
    public String buyer;
    public String seller;
    public String matcher;


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.timestamp);
        dest.writeString(this.id);
        dest.writeByte(this.confirmed ? (byte) 1 : (byte) 0);
        dest.writeString(this.type);
        dest.writeString(this.price);
        dest.writeString(this.amount);
        dest.writeString(this.buyer);
        dest.writeString(this.seller);
        dest.writeString(this.matcher);
    }

    public TradesMarket() {
    }

    protected TradesMarket(Parcel in) {
        this.timestamp = in.readString();
        this.id = in.readString();
        this.confirmed = in.readByte() != 0;
        this.type = in.readString();
        this.price = in.readString();
        this.amount = in.readString();
        this.buyer = in.readString();
        this.seller = in.readString();
        this.matcher = in.readString();
    }

    public static final Creator<TradesMarket> CREATOR = new Creator<TradesMarket>() {
        @Override
        public TradesMarket createFromParcel(Parcel source) {
            return new TradesMarket(source);
        }

        @Override
        public TradesMarket[] newArray(int size) {
            return new TradesMarket[size];
        }
    };
}
