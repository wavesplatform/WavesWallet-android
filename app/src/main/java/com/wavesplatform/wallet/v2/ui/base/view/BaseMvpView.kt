package com.wavesplatform.wallet.v2.ui.base.view

import com.arellomobile.mvp.MvpView

interface BaseMvpView : MvpView {
    fun showNetworkError()

    fun showProgressBar(isShowProgress: Boolean)
}