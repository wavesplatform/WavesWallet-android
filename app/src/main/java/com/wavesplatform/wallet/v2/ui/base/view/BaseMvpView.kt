/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.base.view

import moxy.MvpView

interface BaseMvpView : MvpView {
    fun showNetworkError()
    fun showProgressBar(isShowProgress: Boolean)
}