package com.wavesplatform.wallet.v2.data.rules

import android.support.annotation.StringRes
import io.github.anderscheow.validator.rules.common.RegexRule

class AliasRule : RegexRule {

    constructor() : super(ALIAS_REGEX, "Value does not match alphabet regex")

    constructor(@StringRes errorRes: Int) : super(ALIAS_REGEX, errorRes)

    constructor(errorMessage: String) : super(ALIAS_REGEX, errorMessage)

    companion object {
        private const val ALIAS_REGEX = "^[a-z0-9\\.@_-]*$"
    }
}