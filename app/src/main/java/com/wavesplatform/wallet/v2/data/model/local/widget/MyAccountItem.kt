/*
 * Created by Eduard Zaydel on 2/10/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data.model.local.widget

import android.support.annotation.StringRes
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.wavesplatform.wallet.v2.data.model.db.userdb.AddressBookUserDb
import com.wavesplatform.wallet.v2.ui.auth.migration.migrate_account.MigrateAccountAdapter

class MyAccountItem : MultiItemEntity {
    var type: Int = MigrateAccountAdapter.TYPE_ACCOUNT
    var data: Any
    var locked: Boolean = false
    var active: Boolean = false

    constructor(account: AddressBookUserDb, locked: Boolean, active: Boolean) {
        data = account
        this.locked = locked
        this.active = active
        type = MigrateAccountAdapter.TYPE_ACCOUNT
    }

    constructor(@StringRes header: Int) {
        data = header
        type = MigrateAccountAdapter.TYPE_HEADER
    }

    override fun getItemType(): Int {
        return type
    }
}