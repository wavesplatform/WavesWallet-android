package com.wavesplatform.wallet.v2.ui.home.quick_action.send.confirmation

import com.wavesplatform.wallet.v1.request.TransferTransactionRequest
import com.wavesplatform.wallet.v2.ui.base.view.BaseMvpView

interface SendConfirmationView : BaseMvpView {

    fun requestPassCode()
    fun onShowTransactionSuccess(signed: TransferTransactionRequest)
    fun onShowError(res: Int)
    fun showAddressBookUser(name: String)
    fun hideAddressBookUser()

}
