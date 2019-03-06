package com.wavesplatform.wallet.v2.ui.home.profile.network

import com.wavesplatform.wallet.v2.ui.base.view.BaseMvpView

interface NetworkView : BaseMvpView {
    fun afterSuccessCheckSpamUrl(isValid: Boolean)
}
