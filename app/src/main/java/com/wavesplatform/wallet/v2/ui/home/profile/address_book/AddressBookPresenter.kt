package com.wavesplatform.wallet.v2.ui.home.profile.address_book

import android.util.Log
import com.arellomobile.mvp.InjectViewState
import com.vicpin.krealmextensions.queryAllAsSingle
import com.vicpin.krealmextensions.queryAllAsync
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.util.RxUtil
import pyxis.uzuki.live.richutilskt.utils.runAsync
import javax.inject.Inject

@InjectViewState
class AddressBookPresenter @Inject constructor() : BasePresenter<AddressBookView>() {
    fun getAddresses() {
        runAsync {
            addSubscription(queryAllAsSingle<AddressBookUser>().toObservable()
                    .compose(RxUtil.applyObservableDefaultSchedulers())
                    .subscribe({
                        viewState.afterSuccessGetAddress(it)
                    }, {
                        viewState.afterFailedGetAddress()
                    }))
        }
    }
}
