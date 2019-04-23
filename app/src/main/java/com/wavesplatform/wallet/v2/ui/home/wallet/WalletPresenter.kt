/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.wallet

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.wallet.BuildConfig
import com.wavesplatform.wallet.v1.util.PrefsUtil
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import javax.inject.Inject

@InjectViewState
class WalletPresenter @Inject constructor() : BasePresenter<WalletView>() {
    var hideShadow: Boolean = true

    fun showTopBannerIfNeed() {
        if (!prefsUtil.getValue(PrefsUtil.KEY_IS_CLEARED_ALERT_ALREADY_SHOWN, false) &&
                prefsUtil.getValue(PrefsUtil.KEY_IS_NEED_TO_SHOW_CLEARED_ALERT, false)) {
            viewState.afterCheckClearedWallet()
        } else {
            checkNewAppUpdates()
        }
    }

    private fun checkNewAppUpdates() {
        val needUpdate = preferenceHelper.lastAppVersion != BuildConfig.VERSION_NAME
        viewState.afterCheckNewAppUpdates(needUpdate)
    }
}
