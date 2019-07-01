/*
 * Created by Eduard Zaydel on 25/6/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data.manager.gateway.manager

import com.wavesplatform.wallet.v2.data.model.local.gateway.GatewayMetadataArgs
import com.wavesplatform.wallet.v2.data.model.local.gateway.GatewayWithdrawArgs
import com.wavesplatform.wallet.v2.data.model.remote.request.TransactionsBroadcastRequest
import com.wavesplatform.wallet.v2.data.model.remote.response.gateway.GatewayMetadata
import io.reactivex.Observable

interface BaseGateway {
    fun loadGatewayMetadata(args: GatewayMetadataArgs): Observable<GatewayMetadata>
    fun makeWithdraw(args: GatewayWithdrawArgs): Observable<TransactionsBroadcastRequest>
//    fun makeDeposit(args: GatewayDepositModel): Observable<DR>
}