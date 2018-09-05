package com.wavesplatform.wallet.v2.ui.auth.passcode.enter

import com.wavesplatform.wallet.v2.ui.base.view.BaseMvpView

interface EnterPasscodeView : BaseMvpView {

    fun onSuccessValidatePassCode(password: String, passCode: String)
    fun onFailValidatePassCode(overMaxWrongPassCode: Boolean, message: String?)
}
