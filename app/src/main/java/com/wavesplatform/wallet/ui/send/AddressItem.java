package com.wavesplatform.wallet.ui.send;

import android.support.annotation.NonNull;

public class AddressItem {
    @NonNull
    public String name;
    @NonNull
    public String address;

    public AddressItem(@NonNull String name, @NonNull String address) {
        this.name = name;
        this.address = address;
    }
}
