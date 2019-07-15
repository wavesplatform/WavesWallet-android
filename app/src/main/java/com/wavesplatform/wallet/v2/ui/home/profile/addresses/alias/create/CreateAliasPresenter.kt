/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.profile.addresses.alias.create

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.sdk.model.request.node.AliasTransaction
import com.wavesplatform.sdk.model.response.node.AssetBalanceResponse
import com.wavesplatform.sdk.utils.isSmartError
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.sdk.utils.RxUtil
import com.wavesplatform.wallet.v2.util.errorBody
import javax.inject.Inject

@InjectViewState
class CreateAliasPresenter @Inject constructor() : BasePresenter<CreateAliasView>() {

    var wavesBalance: AssetBalanceResponse = AssetBalanceResponse()
    var aliasValidation = false
    var fee = 0L

    fun loadAlias(alias: String) {
        addSubscription(dataServiceManager.loadAlias(alias)
                .compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe({
                    viewState.aliasIsNotAvailable()
                }, {
                    viewState.aliasIsAvailable()
                }))
    }

    fun loadWavesBalance() {
        addSubscription(nodeServiceManager.loadWavesBalance()
                .compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe({
                    wavesBalance = it
                }, {
                    it.printStackTrace()
                }))
    }

    fun createAlias(alias: String) {
        val aliasRequest = AliasTransaction(alias)
        aliasRequest.fee = fee
        viewState.showProgressBar(true)
        addSubscription(nodeServiceManager.createAlias(aliasRequest)
                .compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe({ alias ->
                    viewState.showProgressBar(false)
                    viewState.successCreateAlias(alias)
                }, {
                    it.printStackTrace()
                    viewState.showProgressBar(false)

                    it.errorBody()?.let { error ->
                        if (error.isSmartError()) {
                            viewState.failedCreateAliasCauseSmart()
                        } else {
                            viewState.failedCreateAlias(error.message)
                        }
                    }
                }))
    }
}
