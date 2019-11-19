/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data.rules

import androidx.annotation.StringRes
import io.github.anderscheow.validator.rules.BaseRule
import java.util.*

class EqualRule : BaseRule {
    override fun validate(value: Any?): Boolean {
        if (value is String) {
            return value == keyword
        }

        throw ClassCastException("Required String value")
    }

    private var keyword: String? = null

    constructor(keyword: String?) : super(String.format(Locale.getDefault(), "Value does not equal to '%s'", keyword)) {
        this.keyword = keyword
    }

    constructor(keyword: String?, @StringRes errorRes: Int) : super(errorRes) {
        this.keyword = keyword
    }

    constructor(keyword: String?, errorMessage: String) : super(errorMessage) {
        this.keyword = keyword
    }
}
