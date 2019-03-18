package com.wavesplatform.wallet.v2.ui.home.wallet.leasing

import com.wavesplatform.sdk.net.model.response.AssetBalance
import com.wavesplatform.sdk.net.model.response.Transaction
import com.wavesplatform.wallet.v2.ui.base.view.BaseMvpView

interface LeasingView : BaseMvpView {
    fun showBalances(wavesAsset: AssetBalance)
    fun showActiveLeasingTransaction(transactions: List<Transaction>)
    fun afterFailedLoadLeasing()
}
