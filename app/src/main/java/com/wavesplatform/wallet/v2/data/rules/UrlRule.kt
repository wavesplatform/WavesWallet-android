/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data.rules

import androidx.annotation.StringRes
import com.wavesplatform.sdk.utils.isWebUrl
import io.github.anderscheow.validator.rules.BaseRule

class UrlRule(@StringRes errorRes: Int) : BaseRule(errorRes) {

    override fun validate(value: Any?): Boolean {
        if (value is String) {
            return value.isWebUrl()
        }

        throw ClassCastException("Required String value")
    }
}
