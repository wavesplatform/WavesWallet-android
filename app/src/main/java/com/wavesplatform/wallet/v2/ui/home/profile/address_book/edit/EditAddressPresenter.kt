package com.wavesplatform.wallet.v2.ui.home.profile.address_book.edit

import com.arellomobile.mvp.InjectViewState
import com.vicpin.krealmextensions.delete
import com.vicpin.krealmextensions.queryFirst
import com.vicpin.krealmextensions.save
import com.wavesplatform.wallet.v2.data.model.userdb.AddressBookUser
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
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
        delete<AddressBookUser> { equalTo("address", addressBookUser?.address) }
        viewState.successDeleteAddress()
    }

    fun editAddress(address: String, name: String) {
        val oldAddress = queryFirst<AddressBookUser> { equalTo("address", addressBookUser?.address) }
        if (addressBookUser?.address == address) {
            oldAddress?.let {
                oldAddress.name = name
                oldAddress?.save()
                viewState.successEditAddress(oldAddress)
            }
        } else {
            delete<AddressBookUser> { equalTo("address", addressBookUser?.address) }
            val addressBookUser = AddressBookUser(address, name)
            addressBookUser.save()
            viewState.successEditAddress(addressBookUser)
        }
    }
}
