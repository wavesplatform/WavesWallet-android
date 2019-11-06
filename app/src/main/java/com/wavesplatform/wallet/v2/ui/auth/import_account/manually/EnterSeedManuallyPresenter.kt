/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.auth.import_account.manually

import moxy.InjectViewState
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import javax.inject.Inject

@InjectViewState
class EnterSeedManuallyPresenter @Inject constructor() : BasePresenter<EnterSeedManuallyView>() {
    var nextStepValidation = false
    var simpleValidationAlertShown: Boolean = false
}
