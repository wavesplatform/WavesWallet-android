package com.wavesplatform.wallet.v2.ui.home.wallet.leasing.start

import android.util.Log
import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.wallet.R.color.i
import com.wavesplatform.wallet.v1.util.MoneyUtil
import com.wavesplatform.wallet.v2.data.model.remote.request.CancelLeasingRequest
import com.wavesplatform.wallet.v2.data.model.remote.request.CreateLeasingRequest
import com.wavesplatform.wallet.v2.data.model.remote.response.Alias
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.ui.home.wallet.leasing.start.StartLeasingActivity.Companion.BUNDLE_AVAILABLE
import com.wavesplatform.wallet.v2.util.RxUtil
import com.wavesplatform.wallet.v2.util.makeAsAlias
import javax.inject.Inject

@InjectViewState
class StartLeasingPresenter @Inject constructor() : BasePresenter<StartLeasingView>() {
    var nodeAddressValidation = false
    var amountValidation = false

    var recipientIsAlias = false

    var wavesAsset: AssetBalance? = null
    var availableBalance: Long = 0L


    fun isAllFieldsValid(): Boolean {
        return nodeAddressValidation && amountValidation
    }

}
