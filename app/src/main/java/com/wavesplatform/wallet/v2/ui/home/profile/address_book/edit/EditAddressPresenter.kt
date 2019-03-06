package com.wavesplatform.wallet.v2.ui.home.profile.address_book.edit

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.ui.home.profile.address_book.AddressBookUser
import javax.inject.Inject

@InjectViewState
class EditAddressPresenter @Inject constructor() : BasePresenter<EditAddressView>() {
    var nameFieldValid = true
    var addressFieldValid = true
    var addressBookUser: AddressBookUser? = null

    fun isAllFieldsValid(): Boolean {
        return nameFieldValid && addressFieldValid
    }

    fun deleteAddress() {
        prefsUtil.deleteAddressBookUsers(addressBookUser?.address)
        viewState.successDeleteAddress()
    }

    fun editAddress(address: String, name: String) {
        val oldAddress = prefsUtil.getAddressBookUser(addressBookUser?.address)
        if (addressBookUser?.address == address) {
            oldAddress?.name = name
            prefsUtil.saveAddressBookUsers(oldAddress)
            viewState.successEditAddress(oldAddress)
        } else {
            prefsUtil.deleteAddressBookUsers(addressBookUser?.address)
            val addressBookUser = AddressBookUser(address, name)
            prefsUtil.saveAddressBookUsers(addressBookUser)
            viewState.successEditAddress(addressBookUser)
        }
    }
}
