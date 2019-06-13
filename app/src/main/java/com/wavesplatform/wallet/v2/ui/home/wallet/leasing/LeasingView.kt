/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.wallet.leasing

import com.wavesplatform.sdk.model.response.AssetBalanceResponse
import com.wavesplatform.sdk.model.response.TransactionResponse
import com.wavesplatform.wallet.v2.ui.base.view.BaseMvpView

interface LeasingView : BaseMvpView {
    fun showBalances(wavesAsset: AssetBalanceResponse)
    fun showActiveLeasingTransaction(transactions: List<TransactionResponse>)
    fun afterFailedLoadLeasing()
}
