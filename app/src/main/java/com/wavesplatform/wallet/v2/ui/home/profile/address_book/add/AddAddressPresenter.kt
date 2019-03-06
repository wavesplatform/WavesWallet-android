package com.wavesplatform.wallet.v2.ui.home.profile.address_book.add

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.ui.home.profile.address_book.AddressBookUser
import javax.inject.Inject

@InjectViewState
class AddAddressPresenter @Inject constructor() : BasePresenter<AddAddressView>() {
    var nameFieldValid = false
    var addressFieldValid = false

    fun isAllFieldsValid(): Boolean {
        return nameFieldValid && addressFieldValid
    }

    fun saveAddress(address: String, name: String) {
        val addressBookUser = AddressBookUser(address, name)
        prefsUtil.saveAddressBookUsers(addressBookUser)
        viewState.successSaveAddress(addressBookUser)
    }
}
