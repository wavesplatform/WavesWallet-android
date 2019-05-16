/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.wallet.leasing.start.confirmation

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.sdk.utils.MoneyUtil
import com.wavesplatform.sdk.net.model.request.CreateLeasingRequest
import com.wavesplatform.sdk.utils.isSmartError
import com.wavesplatform.sdk.utils.makeAsAlias
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.sdk.utils.RxUtil
import com.wavesplatform.wallet.v2.util.errorBody
import javax.inject.Inject

@InjectViewState
class ConfirmationStartLeasingPresenter @Inject constructor() : BasePresenter<ConfirmationStartLeasingView>() {

    var createLeasingRequest: CreateLeasingRequest = CreateLeasingRequest()
    var recipientIsAlias = false
    var address: String = ""
    var amount: String = ""
    var fee = 0L

    var success = false

    fun startLeasing() {
        if (recipientIsAlias) {
            createLeasingRequest.recipient = address.makeAsAlias()
        } else {
            createLeasingRequest.recipient = address
        }
        createLeasingRequest.amount = MoneyUtil.getUnscaledValue(amount, 8)

        addSubscription(nodeDataManager.startLeasing(createLeasingRequest, recipientIsAlias, fee)
                .compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe({
                    success = true
                    viewState.successStartLeasing()
                    viewState.showProgressBar(false)
                }, {
                    it.printStackTrace()
                    viewState.showProgressBar(false)

                    it.errorBody()?.let { error ->
                        if (error.isSmartError()) {
                            viewState.failedStartLeasingCauseSmart()
                        } else {
                            viewState.failedStartLeasing(error.message)
                        }
                    }
                }))
    }
}
