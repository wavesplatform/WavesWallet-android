package com.wavesplatform.wallet.ui.assets;

import android.support.annotation.NonNull;

public class AssetItem {

    private String label;
    private String address;
    private String amount;
    private int correctedPosition;
    boolean isPending;

    public AssetItem(int correctedPosition, String title, String address, String amount, boolean isPending) {
        this.correctedPosition = correctedPosition;
        this.label = title;
        this.address = address;
        this.amount = amount;
        this.isPending = isPending;
    }

    @NonNull
    public String getLabel() {
        return label != null ? label : "";
    }

    @NonNull
    public String getAddress() {
        return address != null ? address : "";
    }

    @NonNull
    public String getAmount() {
        return amount != null ? amount : "";
    }

    int getCorrectPosition() {
        return correctedPosition;
    }
}