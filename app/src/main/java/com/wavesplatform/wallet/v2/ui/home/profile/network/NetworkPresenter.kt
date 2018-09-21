package com.wavesplatform.wallet.v2.ui.home.profile.network

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import javax.inject.Inject

@InjectViewState
class NetworkPresenter @Inject constructor() : BasePresenter<NetworkView>() {
    var spamUrlFieldValid: Boolean = false
    var spamFilterEnableValid: Boolean = false


    fun isAllFieldsValid(): Boolean {
        return spamUrlFieldValid || spamFilterEnableValid
    }


}
