/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.splash

import com.wavesplatform.wallet.v2.ui.base.view.BaseMvpView

interface SplashView : BaseMvpView {

    fun onNotLoggedIn()

    fun onStartMainActivity(publicKey: String)
}
