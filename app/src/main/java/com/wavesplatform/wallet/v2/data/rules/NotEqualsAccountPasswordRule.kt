/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data.rules

import androidx.annotation.StringRes
import com.wavesplatform.wallet.v2.util.WavesWallet
import com.wavesplatform.wallet.App

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
                        App.accessManager.getCurrentWavesWalletEncryptedData(),
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