/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.profile.network

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.sdk.utils.RxUtil
import javax.inject.Inject

@InjectViewState
class NetworkPresenter @Inject constructor() : BasePresenter<NetworkView>() {
    var spamUrlFieldValid: Boolean = false
    var spamFilterEnableValid: Boolean = false

    fun isAllFieldsValid(): Boolean {
        return spamUrlFieldValid || spamFilterEnableValid
    }

    fun checkValidUrl(url: String) {
        viewState.showProgressBar(true)
        addSubscription(githubServiceManager.isValidNewSpamUrl(url)
                .compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe({ isValid ->
                    viewState.showProgressBar(false)
                    viewState.afterSuccessCheckSpamUrl(isValid)
                }, {
                    viewState.showProgressBar(false)
                    viewState.afterSuccessCheckSpamUrl(false)
                    it.printStackTrace()
                }))
    }
}
