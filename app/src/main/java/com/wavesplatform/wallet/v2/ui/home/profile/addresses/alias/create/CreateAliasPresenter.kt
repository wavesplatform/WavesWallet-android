package com.wavesplatform.wallet.v2.ui.home.profile.addresses.alias.create

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.wallet.v2.data.exception.RetrofitException
import com.wavesplatform.wallet.v2.data.model.remote.request.AliasRequest
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalances
import com.wavesplatform.wallet.v2.data.model.remote.response.ErrorResponse
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.util.RxUtil
import javax.inject.Inject

@InjectViewState
class CreateAliasPresenter @Inject constructor() : BasePresenter<CreateAliasView>() {

    var aliasRequest: AliasRequest = AliasRequest()
    var wavesBalance: AssetBalance = AssetBalance()

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

        viewState.showProgressBar(true)

        addSubscription(nodeDataManager.createAlias(aliasRequest)
                .compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe({
                    viewState.showProgressBar(false)
                    viewState.successCreateAlias(it)
                }, {
                    it.printStackTrace()
                    viewState.showProgressBar(false)
                    if (it is RetrofitException) {
                        val response = it.getErrorBodyAs(ErrorResponse::class.java)
                        viewState.failedCreateAlias(response?.message)
                    }
                }))
    }
}
