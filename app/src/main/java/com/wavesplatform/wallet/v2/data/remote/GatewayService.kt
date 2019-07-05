/*
 * Created by Eduard Zaydel on 17/6/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data.remote

import com.wavesplatform.wallet.v2.data.model.remote.request.TransactionsBroadcastRequest
import com.wavesplatform.wallet.v2.data.model.remote.request.gateway.InitGatewayRequest
import com.wavesplatform.wallet.v2.data.model.remote.response.gateway.gateway.InitDepositResponse
import com.wavesplatform.wallet.v2.data.model.remote.response.gateway.gateway.InitWithdrawResponse
import com.wavesplatform.wallet.v2.data.model.remote.response.gateway.gateway.SendTransactionResponse
import io.reactivex.Observable
import retrofit2.http.Body
import retrofit2.http.POST

interface GatewayService {

    @POST("v1/external/withdraw")
    fun initWithdraw(@Body request: InitGatewayRequest): Observable<InitWithdrawResponse>

    @POST("v1/external/send")
    fun sendWithdrawTransaction(@Body request: TransactionsBroadcastRequest): Observable<SendTransactionResponse>

    @POST("v1/external/deposit")
    fun initDeposit(@Body request: InitGatewayRequest): Observable<InitDepositResponse>

}