/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.wallet

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.sdk.utils.EnvironmentManager
import com.wavesplatform.wallet.BuildConfig
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.util.PrefsUtil
import javax.inject.Inject

@InjectViewState
class WalletPresenter @Inject constructor() : BasePresenter<WalletView>() {
    fun showTopBannerIfNeed() {
        if (!prefsUtil.getValue(PrefsUtil.KEY_IS_CLEARED_ALERT_ALREADY_SHOWN, false) &&
                prefsUtil.getValue(PrefsUtil.KEY_IS_NEED_TO_SHOW_CLEARED_ALERT, false)) {
            viewState.afterCheckClearedWallet()
        } else {
            checkNewAppUpdates()
        }
    }

    private fun checkNewAppUpdates() {
        val needUpdate = compareVersions()
        viewState.afterCheckNewAppUpdates(needUpdate)
    }

    private fun compareVersions(): Boolean {
        var needUpdate = false

        val currentVersion = BuildConfig.VERSION_NAME.split(".")
        // todo check
        val lastAppVersion = EnvironmentManager.getLastAppVersion().split(".")

        for (index in 0 until currentVersion.size) {
            if (currentVersion[index].toInt() < lastAppVersion[index].toInt()) {
                needUpdate = true
                break
            }
        }

        return needUpdate
    }
}
