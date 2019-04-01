/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.wallet.leasing

import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.data.model.remote.response.Transaction
import com.wavesplatform.wallet.v2.ui.base.view.BaseMvpView

interface LeasingView : BaseMvpView {
    fun showBalances(wavesAsset: AssetBalance)
    fun showActiveLeasingTransaction(transactions: List<Transaction>)
    fun afterFailedLoadLeasing()
}
