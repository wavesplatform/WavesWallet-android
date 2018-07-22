package com.wavesplatform.wallet.v2.ui.splash

import android.content.Intent
import com.arellomobile.mvp.InjectViewState
import com.vicpin.krealmextensions.saveAll
import com.wavesplatform.wallet.v1.util.PrefsUtil
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.helpers.PublicKeyAccountHelper
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
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
            Constants.defaultAssets.saveAll()
            preferenceHelper.setDefaultAssetsAlreadyExist(true)
        }

        val loggedInGuid = prefsUtil.getGlobalValue(PrefsUtil.GLOBAL_LOGGED_IN_GUID, "")
        val pubKey = prefsUtil.getValue(PrefsUtil.KEY_PUB_KEY, "")
        if (loggedInGuid.isEmpty() || pubKey.isEmpty()
                || !keyAccountHelper.isPublicKeyAccountAvailable(pubKey)) {
            viewState.onNotLoggedIn()
        } else {
            viewState.onStartMainActivity(pubKey)
        }
    }

}
