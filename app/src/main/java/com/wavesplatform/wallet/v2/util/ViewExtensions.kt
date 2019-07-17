/*
 * Created by Eduard Zaydel on 24/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.util

import android.support.annotation.DrawableRes
import android.widget.EditText
import com.flyco.tablayout.SlidingTabLayout

fun SlidingTabLayout.setTabIcon(position: Int, @DrawableRes tabIcon: Int, iconPadding: Int = 0) {
    val tabTitleView = getTitleView(position)
    tabTitleView.setCompoundDrawablesWithIntrinsicBounds(tabIcon, 0, 0, 0)
    tabTitleView.compoundDrawablePadding = iconPadding
}

fun EditText.clearText() {
    setText("")
}