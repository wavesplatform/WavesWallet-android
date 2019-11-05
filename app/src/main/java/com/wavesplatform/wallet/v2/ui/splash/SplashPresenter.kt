/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.splash

import android.content.Intent
import android.text.TextUtils
import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.v2.util.PrefsUtil
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import javax.inject.Inject

@InjectViewState
class SplashPresenter @Inject constructor() : BasePresenter<SplashView>() {

    fun storeIncomingURI(intent: Intent) {
        val action = intent.action
        val scheme = intent.scheme
        if (action != null && Intent.ACTION_VIEW == action && scheme != null && scheme == "waves") {
            prefsUtil.setGlobalValue(PrefsUtil.GLOBAL_SCHEME_URL, intent.data!!.toString())
        }
    }

    fun resolveNextAction() {
        if (TextUtils.isEmpty(App.accessManager.getLoggedInGuid())) {
            viewState.onNotLoggedIn()
        } else {
            val pubKey = prefsUtil.getValue(PrefsUtil.KEY_PUB_KEY, "")
            viewState.onStartMainActivity(pubKey)
        }
    }
}
