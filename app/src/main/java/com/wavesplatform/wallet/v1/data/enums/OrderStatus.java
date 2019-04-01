package com.wavesplatform.wallet.v1.data.enums;

import android.support.annotation.ColorRes;

import com.wavesplatform.wallet.R;

public enum OrderStatus {
    Accepted("Open", R.color.dex_orderbook_left_bg),
    PartiallyFilled("Partial", R.color.dex_orderbook_left_bg),
    Cancelled("Cancelled", R.color.dex_orderbook_right_bg),
    Filled("Filled", R.color.dex_orderbook_right_bg);

    private String status;
    private @ColorRes int color;

    OrderStatus(String status, @ColorRes int color) {
        this.status = status;
        this.color = color;
    }

    public String getStatus() {
        return status;
    }

    public int getColor() {
        return color;
    }
}
