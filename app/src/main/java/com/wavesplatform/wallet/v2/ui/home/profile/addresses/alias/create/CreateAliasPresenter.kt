package com.wavesplatform.wallet.v2.ui.home.profile.addresses.alias.create

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.wallet.v2.data.model.remote.request.AliasRequest
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.util.RxUtil
import com.wavesplatform.wallet.v2.util.errorBody
import com.wavesplatform.wallet.v2.util.isSmartError
import javax.inject.Inject

@InjectViewState
class CreateAliasPresenter @Inject constructor() : BasePresenter<CreateAliasView>() {

    var aliasRequest: AliasRequest = AliasRequest()
    var wavesBalance: AssetBalance = AssetBalance()
    var aliasValidation = false
    var fee = 0L

    fun loadAlias(alias: String) {
        addSubscription(apiDataManager.loadAlias(alias)
                .compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe({
                    viewState.aliasIsNotAvailable()
                }, {
                    viewState.aliasIsAvailable()
                }))
    }

    fun loadWavesBalance() {
        addSubscription(nodeDataManager.loadWavesBalance()
                .compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe({
                    wavesBalance = it
                }, {
                    it.printStackTrace()
                }))
    }

    fun createAlias(alias: String?) {
        aliasRequest.alias = alias
        aliasRequest.fee = fee

        viewState.showProgressBar(true)

        addSubscription(nodeDataManager.createAlias(aliasRequest)
                .compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe({
                    viewState.showProgressBar(false)
                    viewState.successCreateAlias(it)
                }, {
                    it.printStackTrace()
                    viewState.showProgressBar(false)

                    if (it.errorBody()?.isSmartError() == true) {
                        viewState.failedCreateAliasCauseSmart()
                    } else {
                        it.errorBody()?.let {
                            viewState.failedCreateAlias(it.message)
                        }
                    }
                }))
    }
}
