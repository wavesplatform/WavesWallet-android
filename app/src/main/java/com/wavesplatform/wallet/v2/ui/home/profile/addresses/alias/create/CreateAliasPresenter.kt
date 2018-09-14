package com.wavesplatform.wallet.v2.ui.home.profile.addresses.alias.create

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.wallet.v2.data.model.remote.request.AliasRequest
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.util.RxUtil
import javax.inject.Inject

@InjectViewState
class CreateAliasPresenter @Inject constructor() : BasePresenter<CreateAliasView>() {

    var aliasRequest: AliasRequest = AliasRequest()

    fun loadAlias(alias: String) {
        addSubscription(apiDataManager.loadAlias(alias)
                .compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe({
                    viewState.aliasIsNotAvailable()
                }, {
                    viewState.aliasIsAvailable()
                }))
    }

    fun createAlias(alias: String, privateKey: ByteArray, publicKeyStr: String) {
        aliasRequest.alias = alias

        addSubscription(nodeDataManager.createAlias(aliasRequest, privateKey, publicKeyStr)
                .compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe({
                    viewState.successCreateAlias(it)
                }))
    }
}
