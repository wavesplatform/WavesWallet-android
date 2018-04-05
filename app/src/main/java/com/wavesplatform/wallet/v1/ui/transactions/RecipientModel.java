package com.wavesplatform.wallet.v1.ui.transactions;

public class RecipientModel {

    private String mAddress;
    private String mValue;
    private String mDisplayUnits;

    public RecipientModel(String address, String value, String displayUnits) {
        mAddress = address;
        mValue = value;
        mDisplayUnits = displayUnits;
    }

    public String getAddress() {
        return mAddress;
    }

    public String getValue() {
        return mValue;
    }

    public String getDisplayUnits() {
        return mDisplayUnits;
    }
}
