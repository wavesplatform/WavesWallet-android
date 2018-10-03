package com.wavesplatform.wallet.v2.data.rules

import android.support.annotation.StringRes
import android.util.Patterns
import com.wavesplatform.wallet.R
import io.github.anderscheow.validator.rules.BaseRule
import pers.victor.ext.app

class UrlRule : BaseRule {

    constructor() : super(app.getString(R.string.network_spam_url_validation_bad_url))

    constructor(@StringRes errorRes: Int) : super(errorRes)

    constructor(errorMessage: String) : super(errorMessage)

    override fun validate(value: Any): Boolean {
        if (value is String) {
            return Patterns.WEB_URL.matcher(value).matches()
        }

        throw ClassCastException("Required String value")
    }
}
