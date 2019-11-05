/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.profile.address_book

import moxy.InjectViewState
import com.vicpin.krealmextensions.queryAllAsSingle
import com.wavesplatform.wallet.v2.data.model.db.userdb.AddressBookUserDb
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.sdk.utils.RxUtil
import pyxis.uzuki.live.richutilskt.utils.runAsync
import javax.inject.Inject

@InjectViewState
class AddressBookPresenter @Inject constructor() : BasePresenter<AddressBookView>() {
    fun getAddresses() {
        runAsync {
            addSubscription(queryAllAsSingle<AddressBookUserDb>().toObservable()
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
