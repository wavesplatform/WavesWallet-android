package com.wavesplatform.wallet.v2.ui.auth.choose_account.edit

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.ui.home.profile.address_book.AddressTestObject
import javax.inject.Inject

@InjectViewState
class EditAccountNamePresenter @Inject constructor() : BasePresenter<EditAccountNameView>() {

    var account: AddressTestObject? = null
    var accountNameFieldValid = false
}
