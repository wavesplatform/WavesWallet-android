/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.base.view

import com.arellomobile.mvp.MvpView

interface BaseView : MvpView {
    fun configLayoutRes(): Int
    fun onBackPressed()
    fun onNetworkConnectionChanged(networkConnected: Boolean)
}
