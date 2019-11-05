/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data.rules

import androidx.annotation.StringRes
import io.github.anderscheow.validator.rules.common.RegexRule

class AliasRule : RegexRule {

    constructor() : super(ALIAS_REGEX, "Value does not match alphabet regex")

    constructor(@StringRes errorRes: Int) : super(ALIAS_REGEX, errorRes)

    constructor(errorMessage: String) : super(ALIAS_REGEX, errorMessage)

    companion object {
        const val ALIAS_REGEX = "^[a-z0-9\\.@_-]*$"
    }
}