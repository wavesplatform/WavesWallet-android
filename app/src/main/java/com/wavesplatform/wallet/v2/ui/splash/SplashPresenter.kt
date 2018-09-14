package com.wavesplatform.wallet.v2.ui.splash

import android.content.Intent
import android.text.TextUtils
import com.arellomobile.mvp.InjectViewState
import com.vicpin.krealmextensions.saveAll
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.v1.util.PrefsUtil
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.helpers.PublicKeyAccountHelper
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import pyxis.uzuki.live.richutilskt.utils.runAsync
import javax.inject.Inject

@InjectViewState
class SplashPresenter @Inject constructor(val keyAccountHelper: PublicKeyAccountHelper) : BasePresenter<SplashView>() {

    fun storeIncomingURI(intent: Intent) {
        val action = intent.action
        val scheme = intent.scheme
        if (action != null && Intent.ACTION_VIEW == action && scheme != null && scheme == "waves") {
            prefsUtil.setGlobalValue(PrefsUtil.GLOBAL_SCHEME_URL, intent.data!!.toString())
        }
    }

    fun resolveNextAction() {
        if (!preferenceHelper.isDefaultAssetsAlreadyExist()){
            runAsync {
                Constants.defaultAssets.saveAll()
                preferenceHelper.setDefaultAssetsAlreadyExist(true)
            }
        }

        if (TextUtils.isEmpty(App.getAccessManager().getLoggedInGuid())) {
            viewState.onNotLoggedIn()
        } else {
            val pubKey = prefsUtil.getValue(PrefsUtil.KEY_PUB_KEY, "")
            viewState.onStartMainActivity(pubKey)
        }
    }
}
