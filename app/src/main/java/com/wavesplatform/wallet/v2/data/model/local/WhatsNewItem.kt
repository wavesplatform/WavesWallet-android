package com.wavesplatform.wallet.v2.data.model.local

import android.support.annotation.DrawableRes
import android.support.annotation.StringRes


data class WhatsNewItem(
    @DrawableRes var image: Int,
    @StringRes var title: Int,
    @StringRes var description: Int
)