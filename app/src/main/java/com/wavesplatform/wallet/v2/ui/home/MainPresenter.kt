package com.wavesplatform.wallet.v2.ui.home

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.util.RxUtil
import javax.inject.Inject

@InjectViewState
class MainPresenter @Inject constructor() : BasePresenter<MainView>() {

    fun loadBalancesAndTransactions() {
        addSubscription(dataManager.loadBalancesAndTransactions()
                .compose(RxUtil.applyDefaultSchedulers())
                .subscribe({
                    val test =""
                }, {
                    it.printStackTrace()
                }))
    }
}
