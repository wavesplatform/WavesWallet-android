package com.wavesplatform.wallet.v2.ui.home.profile.address_book

import com.arellomobile.mvp.InjectViewState
import com.vicpin.krealmextensions.queryAllAsync
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import javax.inject.Inject

@InjectViewState
class AddressBookPresenter @Inject constructor() : BasePresenter<AddressBookView>() {
    fun getAddresses(){
        queryAllAsync<AddressBookUser> {
            viewState.afterSuccessGetAddress(it)
        }
    }
}
