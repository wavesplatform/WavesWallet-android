/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.wallet.leasing.cancel.confirmation

import com.wavesplatform.wallet.v2.ui.base.view.BaseMvpView

interface ConfirmationCancelLeasingView : BaseMvpView {
    fun successCancelLeasing()
    fun failedCancelLeasing(message: String?)
    fun failedCancelLeasingCauseSmart()
    fun showCommissionLoading()
    fun showCommissionSuccess(unscaledAmount: Long)
    fun showCommissionError()
}
