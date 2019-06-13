/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.quick_action.send.confirmation

import com.wavesplatform.sdk.model.transaction.node.TransferTransaction
import com.wavesplatform.wallet.v2.ui.base.view.BaseMvpView

interface SendConfirmationView : BaseMvpView {

    fun onShowTransactionSuccess(signed: TransferTransaction)
    fun onShowError(res: Int)
    fun showAddressBookUser(name: String)
    fun hideAddressBookUser()
    fun failedSendCauseSmart()
}
