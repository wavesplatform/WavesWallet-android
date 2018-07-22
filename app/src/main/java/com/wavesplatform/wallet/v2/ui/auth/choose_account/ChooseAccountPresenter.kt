package com.wavesplatform.wallet.v2.ui.auth.choose_account

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.ui.home.profile.address_book.AddressTestObject
import javax.inject.Inject

@InjectViewState
class ChooseAccountPresenter @Inject constructor():BasePresenter<ChooseAccountView>(){
    fun getAddresses(){
        var list = arrayListOf<AddressTestObject>(AddressTestObject("MkSuckMydickmMak1593x1GrfYmFdsf83skS11", "Ed"),
                AddressTestObject("MkSuckMydickmMak1593x1GrfYmFdsf83skS11", "Droid"),
                AddressTestObject("MkSuckMydickmMak1593x1GrfYmFdsf83skS11", "Expert"),
                AddressTestObject("MkSuckMydickmMak1593x1GrfYmFdsf83skS11", "Fuaaaarrkkkk"))
        viewState.afterSuccessGetAddress(list)
    }
}
