/*
 * Created by Eduard Zaydel on 30/9/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.auth.migration.migrate_account

import com.wavesplatform.wallet.v2.data.model.local.MigrateAccountItem
import com.wavesplatform.wallet.v2.ui.base.view.BaseMvpView

interface MigrateAccountView : BaseMvpView {
    fun afterSuccessGetAddress(accounts: MutableList<MigrateAccountItem>)
}
