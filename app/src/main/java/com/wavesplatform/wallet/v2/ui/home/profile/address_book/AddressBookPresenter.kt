package com.wavesplatform.wallet.v2.ui.home.profile.address_book

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import javax.inject.Inject

@InjectViewState
class AddressBookPresenter @Inject constructor() : BasePresenter<AddressBookView>() {
    fun getAddresses(){
        var list = listOf<AddressTestObject>(AddressTestObject("MkSuckMydickmMak1593x1GrfYmFdsf83skS11", "Ed"),
                AddressTestObject("MkSuckMydickmMak1593x1GrfYmFdsf83skS11", "Droid"),
                AddressTestObject("MkSuckMydickmMak1593x1GrfYmFdsf83skS11", "Expert"),
                AddressTestObject("MkSuckMydickmMak1593x1GrfYmFdsf83skS11", "Fuaaaarrkkkk"))
        viewState.afterSuccessGetAddress(list)
    }
}
