package com.wavesplatform.wallet.v2.data.rules

import android.support.annotation.StringRes
import com.wavesplatform.sdk.WavesWallet
import com.wavesplatform.wallet.App
import io.github.anderscheow.validator.rules.BaseRule

class EqualsAccountPasswordRule : BaseRule {
    var guid = ""

    constructor(guid: String = "") : super("Value must not be empty") {
        this.guid = guid
    }

    constructor(@StringRes errorRes: Int, guid: String = "") : super(errorRes) {
        this.guid = guid
    }

    constructor(errorMessage: String, guid: String = "") : super(errorMessage) {
        this.guid = guid
    }

    override fun validate(value: Any?): Boolean {
        if (value == null) {
            throw NullPointerException()
        }

        if (value is String) {
            return try {
                if (guid.isEmpty()) {
                    WavesWallet(
                            App.getAccessManager().getCurrentWavesWalletEncryptedData(),
                            value
                    )
                    true
                } else {
                    WavesWallet(App.getAccessManager().getWalletData(guid),
                            value.trim())
                    true
                }
            } catch (e: Exception) {
                false
            }
        }

        throw ClassCastException("Required String value")
    }
}