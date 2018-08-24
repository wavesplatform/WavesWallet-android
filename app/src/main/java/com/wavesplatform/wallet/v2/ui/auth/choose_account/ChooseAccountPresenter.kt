package com.wavesplatform.wallet.v2.ui.auth.choose_account

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.ui.home.profile.address_book.AddressBookUser
import javax.inject.Inject

@InjectViewState
class ChooseAccountPresenter @Inject constructor():BasePresenter<ChooseAccountView>(){
    fun getAddresses(){
        var list = arrayListOf<AddressBookUser>(AddressBookUser("MkSuckMydickmMak1593x1GrfYmFdsf83skS11", "Ed"),
                AddressBookUser("MkSuckMydickmMak1593x1GrfYmFdsf83skS11", "Droid"),
                AddressBookUser("MkSuckMydickmMak1593x1GrfYmFdsf83skS11", "Expert"),
                AddressBookUser("MkSuckMydickmMak1593x1GrfYmFdsf83skS11", "Fuaaaarrkkkk"))
        viewState.afterSuccessGetAddress(list)
    }
}
