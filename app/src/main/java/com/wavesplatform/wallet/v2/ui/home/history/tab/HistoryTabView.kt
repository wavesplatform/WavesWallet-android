package com.wavesplatform.wallet.v2.ui.home.history.tab

import com.wavesplatform.wallet.v2.ui.base.view.BaseMvpView
import com.wavesplatform.wallet.v2.ui.home.history.HistoryItem

interface HistoryTabView :BaseMvpView{
    fun afterSuccessLoadTransaction(data: ArrayList<HistoryItem>, type: String?)
    fun afterSuccessLoadMoreTransaction(data: ArrayList<HistoryItem>, type: String?)
    fun goneLoadMoreView()
}
