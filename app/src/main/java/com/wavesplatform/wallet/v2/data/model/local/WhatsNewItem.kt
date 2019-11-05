/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data.model.local

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes


data class WhatsNewItem(
    @DrawableRes var image: Int,
    @StringRes var title: Int,
    @StringRes var description: Int
)