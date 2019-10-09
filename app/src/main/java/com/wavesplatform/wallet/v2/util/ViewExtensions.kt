/*
 * Created by Eduard Zaydel on 24/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.util

import android.support.annotation.DrawableRes
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.flyco.tablayout.SlidingTabLayout
import pers.victor.ext.dp

fun SlidingTabLayout.setTabIcon(position: Int, @DrawableRes tabIcon: Int, iconPadding: Int = 0) {
    val tabTitleView = getTitleView(position)
    tabTitleView.setCompoundDrawablesWithIntrinsicBounds(tabIcon, 0, 0, 0)
    tabTitleView.compoundDrawablePadding = iconPadding
}

fun EditText.clearText() {
    setText("")
}

fun View.margin(left: Int? = null, top: Int? = null, right: Int? = null, bottom: Int? = null) {
    layoutParams<ViewGroup.MarginLayoutParams> {
        left?.run { leftMargin = this }
        top?.run { topMargin = this }
        right?.run { rightMargin = this }
        bottom?.run { bottomMargin = this }
    }
}

inline fun <reified T : ViewGroup.LayoutParams> View.layoutParams(block: T.() -> Unit) {
    if (layoutParams is T) block(layoutParams as T)
}
