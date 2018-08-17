package com.wavesplatform.wallet.v2.ui.home.history.item

import com.wavesplatform.wallet.v2.ui.base.view.BaseMvpView
import com.wavesplatform.wallet.v2.ui.home.history.HistoryItem

interface HistoryDateItemView :BaseMvpView{
    fun showData(data: ArrayList<HistoryItem>, type: String?)

}
