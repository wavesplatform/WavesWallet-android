/*
 * Created by Eduard Zaydel on 24/7/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data.model.local.widget

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import com.wavesplatform.wallet.R

enum class MarketWidgetStyleColors(@ColorRes var currencyActiveColor: Int, // color for active text on switch of USD / EUR
                                   @ColorRes var currencyInactiveColor: Int, // color for inactive text on switch of USD / EUR
                                   @ColorRes var percentIncreaseTextColor: Int, // color for Increase percent text
                                   @ColorRes var percentDropTextColor: Int, // color for Drop percent text
                                   @ColorRes var percentWithoutChangeTextColor: Int, // color for Without Change percent text
                                   @DrawableRes var percentIncreaseBackground: Int, // bg color for Increase percent text
                                   @DrawableRes var percentDropBackground: Int, // bg color for Drop percent text
                                   @DrawableRes var percentWithoutChangeBackground: Int // bg color for Without Change percent text
) {
    CLASSIC(R.color.black, R.color.basic500,
            R.color.white, R.color.white, R.color.white,
            R.drawable.bg_widget_percent_up, R.drawable.bg_widget_percent_down, R.drawable.bg_widget_percent_not_changed),
    DARK(R.color.white, R.color.disabled700,
            R.color.successLime, R.color.error500, R.color.disabled500,
            R.drawable.bg_widget_percent_dark, R.drawable.bg_widget_percent_dark, R.drawable.bg_widget_percent_dark);

}