package com.wavesplatform.wallet.v2.data.rules

import android.support.annotation.StringRes
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.v1.data.auth.WavesWallet

import io.github.anderscheow.validator.rules.BaseRule

class NotEqualsAccountPasswordRule : BaseRule {

    constructor() : super("Value must not be empty") {}

    constructor(@StringRes errorRes: Int) : super(errorRes) {}

    constructor(errorMessage: String) : super(errorMessage) {}

    override fun validate(value: Any?): Boolean {
        if (value == null) {
            throw NullPointerException()
        }

        if (value is String) {
            return try {
                val oldWallet = WavesWallet(
                        App.getAccessManager().getCurrentWavesWalletEncryptedData(),
                        value
                )
                false
            } catch (e: Exception) {
                true
            }
        }

        throw ClassCastException("Required String value")
    }
}