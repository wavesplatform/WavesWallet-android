/*
 * Created by Eduard Zaydel on 25/6/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data.model.local.gateway.gateway

import com.wavesplatform.wallet.v2.data.model.local.gateway.base.GatewayWithdrawModel
import com.wavesplatform.wallet.v2.data.model.remote.request.TransactionsBroadcastRequest

data class GatewayWithdrawArgs(var transaction: TransactionsBroadcastRequest) : GatewayWithdrawModel