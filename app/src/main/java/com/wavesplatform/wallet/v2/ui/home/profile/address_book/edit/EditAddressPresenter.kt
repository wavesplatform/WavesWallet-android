package com.wavesplatform.wallet.v2.ui.home.profile.address_book.edit

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.ui.home.profile.address_book.AddressTestObject
import javax.inject.Inject

@InjectViewState
class EditAddressPresenter @Inject constructor() : BasePresenter<EditAddressView>() {
    var nameFieldValid = false
    var addressFieldValid = false
    var address: AddressTestObject? = null

    fun isAllFieldsValid(): Boolean {
        return nameFieldValid  && addressFieldValid
    }
}
