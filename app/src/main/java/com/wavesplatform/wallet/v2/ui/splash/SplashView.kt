package com.wavesplatform.wallet.v2.ui.splash

import com.wavesplatform.wallet.v2.ui.base.view.BaseMvpView

interface SplashView : BaseMvpView {

    fun onNotLoggedIn()

    fun onStartMainActivity(publicKey: String)
}
