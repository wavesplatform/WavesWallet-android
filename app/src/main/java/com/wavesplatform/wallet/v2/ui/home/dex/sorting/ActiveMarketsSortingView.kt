package com.wavesplatform.wallet.v2.ui.home.dex.sorting

import com.wavesplatform.wallet.v2.ui.base.view.BaseMvpView
import com.wavesplatform.wallet.v2.ui.home.wallet.assets.TestObject

interface ActiveMarketsSortingView : BaseMvpView {
    fun afterSuccessLoadMarkets(list: ArrayList<TestObject>)

}
