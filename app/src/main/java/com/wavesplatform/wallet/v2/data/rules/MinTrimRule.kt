package com.wavesplatform.wallet.v2.data.rules

import android.support.annotation.StringRes

import java.util.Locale

import io.github.anderscheow.validator.rules.BaseRule

class MinTrimRule : BaseRule {

    private var minLength: Int = 0

    constructor(minLength: Int) : super(String.format(Locale.getDefault(), "Length must exceed at least %d characters", minLength)) {
        this.minLength = minLength
    }

    constructor(minLength: Int, @StringRes errorRes: Int) : super(errorRes) {
        this.minLength = minLength
    }

    constructor(minLength: Int, errorMessage: String) : super(errorMessage) {
        this.minLength = minLength
    }

    override fun validate(value: Any?): Boolean {
        if (value == null) {
            throw NullPointerException()
        }

        if (value is String) {
            return value.trim().length >= minLength
        }

        throw ClassCastException("Required String value")
    }
}
