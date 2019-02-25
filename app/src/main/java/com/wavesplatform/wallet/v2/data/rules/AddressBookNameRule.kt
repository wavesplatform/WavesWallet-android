package com.wavesplatform.wallet.v2.data.rules

import android.support.annotation.StringRes
import com.vicpin.krealmextensions.queryFirst
import com.wavesplatform.wallet.v2.ui.home.profile.address_book.AddressBookUser

import io.github.anderscheow.validator.rules.BaseRule

class AddressBookNameRule : BaseRule {

    constructor() : super("Value must not be empty") {}

    constructor(@StringRes errorRes: Int) : super(errorRes) {}

    constructor(errorMessage: String) : super(errorMessage) {}

    override fun validate(value: Any?): Boolean {
        if (value == null) {
            throw NullPointerException()
        }

        if (value is String) {
            val user = queryFirst<AddressBookUser> { equalTo("name", value) }
            return user == null
        }

        throw ClassCastException("Required String value")
    }
}