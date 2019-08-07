/*
 * Created by Eduard Zaydel on 7/8/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.widget.option

data class OptionsDialogItem<T>(
        var data: T,
        var checked: Boolean = false
)