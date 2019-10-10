/*
 * Created by Eduard Zaydel on 24/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.util

import android.view.View
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