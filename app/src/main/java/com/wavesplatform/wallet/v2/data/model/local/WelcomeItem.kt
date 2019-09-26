/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data.model.local

import android.support.annotation.DrawableRes
import android.support.annotation.StringRes

data class WelcomeItem(
        @DrawableRes val image: Int,
        @StringRes val title: Int,
        @StringRes val description: Int
)