/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.wallet.leasing.start.confirmation

import com.wavesplatform.wallet.v2.ui.base.view.BaseMvpView

interface ConfirmationStartLeasingView : BaseMvpView {
    fun successStartLeasing()
    fun failedStartLeasing(message: String?)
    fun failedStartLeasingCauseSmart()
}
