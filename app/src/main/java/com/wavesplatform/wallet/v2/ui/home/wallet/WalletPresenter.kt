/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.wallet

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.wallet.BuildConfig
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import javax.inject.Inject

@InjectViewState
class WalletPresenter @Inject constructor() : BasePresenter<WalletView>() {
    fun checkNewAppUpdates() {
        val needUpdate = preferenceHelper.lastAppVersion != BuildConfig.VERSION_NAME
        viewState.afterCheckNewAppUpdates(needUpdate)
    }
}
