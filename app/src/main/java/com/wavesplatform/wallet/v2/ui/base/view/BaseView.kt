package com.wavesplatform.wallet.v2.ui.base.view

import com.arellomobile.mvp.MvpView

interface BaseView : MvpView {
    fun configLayoutRes(): Int
    fun onBackPressed()
    fun onNetworkConnectionChanged(networkConnected: Boolean)
}
