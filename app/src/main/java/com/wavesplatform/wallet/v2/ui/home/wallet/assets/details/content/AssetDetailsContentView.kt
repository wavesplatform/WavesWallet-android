package com.wavesplatform.wallet.v2.ui.home.wallet.assets.details.content

import com.wavesplatform.wallet.v2.ui.base.view.BaseMvpView
import com.wavesplatform.wallet.v2.ui.home.history.HistoryItem

interface AssetDetailsContentView : BaseMvpView {
    fun showLastTransactions(data: MutableList<HistoryItem>)
}
