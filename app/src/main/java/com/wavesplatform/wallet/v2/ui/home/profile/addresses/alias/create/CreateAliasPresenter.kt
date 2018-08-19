package com.wavesplatform.wallet.v2.ui.home.profile.addresses.alias.create

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.util.RxUtil
import pers.victor.ext.toast
import javax.inject.Inject

@InjectViewState
class CreateAliasPresenter @Inject constructor() : BasePresenter<CreateAliasView>(){

    fun loadAlias(alias: String) {
        addSubscription(apiDataManager.loadAlias(alias)
                .compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe({
                    viewState.aliasIsAvailable()
                },{
                    viewState.aliasIsNotAvailable()
                }))
    }
}
