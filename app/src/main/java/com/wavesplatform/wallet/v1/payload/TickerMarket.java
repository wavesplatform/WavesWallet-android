package com.wavesplatform.wallet.v1.payload;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class TickerMarket implements Parcelable{

    @SerializedName("symbol")
    public String symbol;
    @SerializedName("amountAssetID")
    public String amountAssetID;
    @SerializedName("amountAssetName")
    public String amountAssetName;
    @SerializedName("amountAssetDecimals")
    public int amountAssetDecimals;
    @SerializedName("priceAssetID")
    public String priceAssetID;
    @SerializedName("priceAssetName")
    public String priceAssetName;
    @SerializedName("priceAssetDecimals")
    public int priceAssetDecimals = 0;
    @SerializedName("24h_open")
    public String open24h = "0.0";
    @SerializedName("24h_high")
    public String high24h = "0.0";
    @SerializedName("24h_low")
    public String low24h = "0.0";
    @SerializedName("24h_close")
    public String close24h = "0.0";
    @SerializedName("24h_vwap")
    public String vwap24h;
    @SerializedName("24h_volume")
    public String volume24h;
    @SerializedName("24h_priceVolume")
    public String priceVolume24h;
    @SerializedName("timestamp")
    public String timestamp;


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.symbol);
        dest.writeString(this.amountAssetID);
        dest.writeString(this.amountAssetName);
        dest.writeInt(this.amountAssetDecimals);
        dest.writeString(this.priceAssetID);
        dest.writeString(this.priceAssetName);
        dest.writeInt(this.priceAssetDecimals);
        dest.writeString(this.open24h);
        dest.writeString(this.high24h);
        dest.writeString(this.low24h);
        dest.writeString(this.close24h);
        dest.writeString(this.vwap24h);
        dest.writeString(this.volume24h);
        dest.writeString(this.priceVolume24h);
        dest.writeString(this.timestamp);
    }

    public TickerMarket() {
    }

    protected TickerMarket(Parcel in) {
        this.symbol = in.readString();
        this.amountAssetID = in.readString();
        this.amountAssetName = in.readString();
        this.amountAssetDecimals = in.readInt();
        this.priceAssetID = in.readString();
        this.priceAssetName = in.readString();
        this.priceAssetDecimals = in.readInt();
        this.open24h = in.readString();
        this.high24h = in.readString();
        this.low24h = in.readString();
        this.close24h = in.readString();
        this.vwap24h = in.readString();
        this.volume24h = in.readString();
        this.priceVolume24h = in.readString();
        this.timestamp = in.readString();
    }

    public static final Creator<TickerMarket> CREATOR = new Creator<TickerMarket>() {
        @Override
        public TickerMarket createFromParcel(Parcel source) {
            return new TickerMarket(source);
        }

        @Override
        public TickerMarket[] newArray(int size) {
            return new TickerMarket[size];
        }
    };
}
