package com.wavesplatform.wallet.v2.ui.home.profile.network

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.util.RxUtil
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
        addSubscription(githubDataManager.isValidNewSpamUrl(url)
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
