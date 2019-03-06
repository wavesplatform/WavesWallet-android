package com.wavesplatform.wallet.v2.data.model.local

import android.support.annotation.DrawableRes

/**
 * Created by anonymous on 16.12.17.
 */

data class WelcomeItem(
    @DrawableRes var image: Int,
    var title: String,
    var description: String
)