package com.wavesplatform.wallet.v2.ui.keeper.send

import com.wavesplatform.sdk.keeper.interfaces.KeeperTransactionResponse
import com.wavesplatform.sdk.model.response.node.AssetsDetailsResponse
import com.wavesplatform.wallet.v2.ui.base.view.BaseMvpView

interface KeeperSendTransactionView : BaseMvpView {
    fun onError(error: Throwable)
    fun onSuccessSend(transaction: KeeperTransactionResponse)
    fun onReceiveAssetDetails(assetDetails: AssetsDetailsResponse)
}
