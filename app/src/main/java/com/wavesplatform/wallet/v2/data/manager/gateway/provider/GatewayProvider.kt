/*
 * Created by Eduard Zaydel on 25/6/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data.manager.gateway.provider

import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.manager.gateway.manager.BaseGateway
import com.wavesplatform.wallet.v2.data.manager.gateway.manager.CoinomatDataManager
import com.wavesplatform.wallet.v2.data.manager.gateway.manager.GatewayDataManager
import com.wavesplatform.wallet.v2.util.EnvironmentManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GatewayProvider @Inject constructor() {

    @Inject
    lateinit var coinomatManager: CoinomatDataManager
    @Inject
    lateinit var gatewayDataManager: GatewayDataManager

    fun getGatewayDataManager(assetId: String): BaseGateway {
        val gatewayGeneralAsset = EnvironmentManager.findAssetIdByAssetId(assetId)

        return when (gatewayGeneralAsset?.gatewayType) {
            Constants.GatewayType.GATEWAY -> gatewayDataManager
            Constants.GatewayType.COINOMAT -> coinomatManager
            else -> coinomatManager
        }
    }
}