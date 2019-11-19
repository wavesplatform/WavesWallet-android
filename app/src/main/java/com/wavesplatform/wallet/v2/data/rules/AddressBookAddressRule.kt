/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data.rules

import androidx.annotation.StringRes
import com.vicpin.krealmextensions.queryFirst
import com.wavesplatform.wallet.v2.util.PrefsUtil
import com.wavesplatform.wallet.v2.data.model.db.userdb.AddressBookUserDb

import io.github.anderscheow.validator.rules.BaseRule

class AddressBookAddressRule(var prefsUtil: PrefsUtil, @StringRes errorRes: Int) : BaseRule(errorRes) {

    override fun validate(value: Any?): Boolean {
        if (value == null) {
            throw NullPointerException()
        }

        if (value is String) {
            val user = queryFirst<AddressBookUserDb> { equalTo("address", value) }
            return user == null
        }

        throw ClassCastException("Required String value")
    }
}