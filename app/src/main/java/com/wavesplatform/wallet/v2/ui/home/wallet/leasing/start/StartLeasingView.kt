package com.wavesplatform.wallet.v2.ui.home.wallet.leasing.start

import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.ui.base.view.BaseMvpView

interface StartLeasingView : BaseMvpView {
    fun afterSuccessLoadWavesBalance(waves: AssetBalance, availableBalance: Long)
}
