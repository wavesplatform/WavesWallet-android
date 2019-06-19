/*
 * Created by Eduard Zaydel on 18/6/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data.manager.gateway

import com.wavesplatform.wallet.v2.data.manager.base.BaseDataManager
import com.wavesplatform.wallet.v2.data.model.remote.request.TransactionsBroadcastRequest
import com.wavesplatform.wallet.v2.data.model.remote.request.gateway.InitGatewayRequest
import com.wavesplatform.wallet.v2.data.model.remote.response.coinomat.CreateTunnel
import com.wavesplatform.wallet.v2.data.model.remote.response.coinomat.GetTunnel
import com.wavesplatform.wallet.v2.data.model.remote.response.coinomat.Limit
import com.wavesplatform.wallet.v2.data.model.remote.response.coinomat.XRate
import com.wavesplatform.wallet.v2.data.model.remote.response.gateway.InitDepositResponse
import com.wavesplatform.wallet.v2.data.model.remote.response.gateway.InitWithdrawResponse
import com.wavesplatform.wallet.v2.data.model.remote.response.gateway.SendTransactionResponse
import io.reactivex.Observable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GatewayDataManager @Inject constructor() : BaseDataManager() {

    fun prepareSendTransaction(userAddress: String, assetId: String): Observable<InitWithdrawResponse> {
        return gatewayService.initWithdraw(InitGatewayRequest(userAddress, assetId))
    }

    fun receiveTransaction(userAddress: String, assetId: String): Observable<InitDepositResponse> {
        return gatewayService.initDeposit(InitGatewayRequest(userAddress, assetId))
    }

    fun sendTransaction(transaction: TransactionsBroadcastRequest): Observable<SendTransactionResponse> {
        return gatewayService.sendWithdrawTransaction(transaction)
    }

}