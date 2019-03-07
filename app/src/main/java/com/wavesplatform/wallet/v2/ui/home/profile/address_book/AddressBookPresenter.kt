package com.wavesplatform.wallet.v2.ui.home.profile.address_book

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.util.RxUtil
import io.reactivex.Observable
import pyxis.uzuki.live.richutilskt.utils.runAsync
import javax.inject.Inject

@InjectViewState
class AddressBookPresenter @Inject constructor() : BasePresenter<AddressBookView>() {
    fun getAddresses() {
        runAsync {
            addSubscription(Observable.fromArray(prefsUtil.allAddressBookUsers)
                    .compose(RxUtil.applyObservableDefaultSchedulers())
                    .subscribe({
                        val addresses = it.sortedBy { it.name }.toMutableList()
                        viewState.afterSuccessGetAddress(addresses)
                    }, {
                        viewState.afterFailedGetAddress()
                    }))
        }
    }
}
