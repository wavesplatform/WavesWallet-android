/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.auth.passcode.enter

import com.wavesplatform.wallet.v2.ui.base.view.BaseMvpView

interface EnterPasscodeView : BaseMvpView {

    fun onSuccessValidatePassCode(password: String, passCode: String)
    fun onFailValidatePassCode(overMaxWrongPassCode: Boolean, errorMessage: String?)
}
