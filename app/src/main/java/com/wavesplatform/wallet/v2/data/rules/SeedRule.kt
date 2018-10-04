package com.wavesplatform.wallet.v2.data.rules

import android.support.annotation.StringRes
import com.wavesplatform.wallet.App

import io.github.anderscheow.validator.rules.BaseRule

class SeedRule : BaseRule {

    constructor() : super("Value must not be empty") {}

    constructor(@StringRes errorRes: Int) : super(errorRes) {}

    constructor(errorMessage: String) : super(errorMessage) {}

    override fun validate(value: Any?): Boolean {
        if (value == null) {
            throw NullPointerException()
        }

        if (value is String) {
            return !App.getAccessManager().isAccountWithSeedExist(value)
        }

        throw ClassCastException("Required String value")
    }
}