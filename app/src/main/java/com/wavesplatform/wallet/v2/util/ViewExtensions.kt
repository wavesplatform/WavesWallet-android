/*
 * Created by Eduard Zaydel on 24/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.util

import android.view.View
import android.view.ViewGroup
import android.widget.EditText

fun EditText.clearText() {
    setText("")
}

fun View.safeThrottledClick(waitMillis: Long = 500, listener: (View) -> Unit) {
    var lastClickTime = 0L
    setOnClickListener { view ->
        if (System.currentTimeMillis() > lastClickTime + waitMillis) {
            listener.invoke(view)
            lastClickTime = System.currentTimeMillis()
        }
    }
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
