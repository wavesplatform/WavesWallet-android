package com.wavesplatform.wallet.v2.ui.home.profile.address_book.edit

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.wallet.v2.data.model.userdb.AddressBookUser
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.util.deleteUserData
import com.wavesplatform.wallet.v2.util.queryFirstUserData
import com.wavesplatform.wallet.v2.util.saveUserData
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
        deleteUserData<AddressBookUser> { equalTo("address", addressBookUser?.address) }
        viewState.successDeleteAddress()
    }

    fun editAddress(address: String, name: String) {
        val oldAddress = queryFirstUserData<AddressBookUser> { equalTo("address", addressBookUser?.address) }
        if (addressBookUser?.address == address) {
            oldAddress?.name = name
            oldAddress?.saveUserData()
            viewState.successEditAddress(oldAddress)
        } else {
            deleteUserData<AddressBookUser> { equalTo("address", addressBookUser?.address) }
            val addressBookUser = AddressBookUser(address, name)
            addressBookUser.saveUserData()
            viewState.successEditAddress(addressBookUser)
        }
    }
}
