package com.wavesplatform.wallet.v2.ui.home.profile.address_book.edit

import android.content.DialogInterface
import com.arellomobile.mvp.InjectViewState
import com.google.common.base.Predicates
import com.google.common.base.Predicates.equalTo
import com.vicpin.krealmextensions.delete
import com.vicpin.krealmextensions.queryFirst
import com.vicpin.krealmextensions.save
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.ui.home.profile.address_book.AddressBookUser
import javax.inject.Inject

@InjectViewState
class EditAddressPresenter @Inject constructor() : BasePresenter<EditAddressView>() {
    var nameFieldValid = false
    var addressFieldValid = false
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
        oldAddress?.address = address
        oldAddress?.name = name
        oldAddress?.save()
        viewState.successEditAddress(oldAddress)
    }

}
