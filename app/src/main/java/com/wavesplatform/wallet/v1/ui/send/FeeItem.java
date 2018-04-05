package com.wavesplatform.wallet.v1.ui.send;

import android.support.annotation.NonNull;

import com.wavesplatform.wallet.v1.util.MoneyUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FeeItem {
    @NonNull
    public String name;
    @NonNull
    public String fee;
    public long feeAmount;

    public FeeItem(@NonNull String name, long feeAmount) {
        this.name = name;
        this.feeAmount = feeAmount;
        this.fee = MoneyUtil.getDisplayWaves(feeAmount);
    }
}
