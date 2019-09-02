package com.wavesplatform.wallet.v2.ui.keeper

import com.wavesplatform.sdk.keeper.interfaces.KeeperTransaction
import com.wavesplatform.sdk.model.request.node.BaseTransaction
import com.wavesplatform.sdk.model.response.node.AssetsDetailsResponse
import com.wavesplatform.sdk.model.response.node.transaction.BaseTransactionResponse
import com.wavesplatform.wallet.v2.ui.base.view.BaseMvpView

interface KeeperTransactionView : BaseMvpView {
    fun onError(error: Throwable)
    fun onSuccessSign(transaction: BaseTransaction)
    fun onReceiveTransactionData(type: Byte, transaction: KeeperTransaction, fee: Long,
                                 dAppAddress: String,
                                 assetDetails: HashMap<String, AssetsDetailsResponse>)
}
