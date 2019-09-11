/*
 * Created by Eduard Zaydel on 30/8/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data.model.local

import android.support.annotation.StringRes
import com.wavesplatform.wallet.R

enum class NewAccountDialogItem(@StringRes var title: Int) : OptionsDialogModel {
    CREATE_ACCOUNT(R.string.welcome_create_new_acc),
    IMPORT_ACCOUNT(R.string.welcome_create_import_title);

    override fun itemTitle(): Int {
        return title
    }
}