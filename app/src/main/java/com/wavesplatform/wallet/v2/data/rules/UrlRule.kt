package com.wavesplatform.wallet.v2.data.rules

import android.support.annotation.StringRes
import android.util.Patterns
import io.github.anderscheow.validator.rules.BaseRule

class UrlRule(@StringRes errorRes: Int) : BaseRule(errorRes) {

    override fun validate(value: Any): Boolean {
        if (value is String) {
            return Patterns.WEB_URL.matcher(value).matches()
        }

        throw ClassCastException("Required String value")
    }
}
