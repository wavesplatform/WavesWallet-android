/*
 * Created by Eduard Zaydel on 25/6/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data.manager.gateway.manager

import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.manager.base.BaseDataManager
import com.wavesplatform.wallet.v2.data.model.local.gateway.base.GatewayMetadataModel
import com.wavesplatform.wallet.v2.data.model.local.gateway.coinomat.CoinomatMetadataArgs
import com.wavesplatform.wallet.v2.data.model.remote.response.gateway.GatewayMetadata
import com.wavesplatform.wallet.v2.data.model.remote.response.gateway.coinomat.CreateTunnel
import com.wavesplatform.wallet.v2.data.model.remote.response.gateway.coinomat.GetTunnel
import com.wavesplatform.wallet.v2.data.model.remote.response.gateway.coinomat.Limit
import com.wavesplatform.wallet.v2.data.model.remote.response.gateway.coinomat.XRate
import io.reactivex.Observable
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CoinomatDataManager @Inject constructor() : BaseDataManager(), BaseGateway {

    override fun loadGatewayMetadata(args: GatewayMetadataModel): Observable<GatewayMetadata> {
        if (args is CoinomatMetadataArgs) {
            val currencyTo = Constants.coinomatCryptoCurrencies()[args.assetId]
            val currencyFrom = "W$currencyTo"

            if (currencyTo.isNullOrEmpty()) {
                return Observable.error(Throwable("Empty or null 'currencyTo' field"))
            }

            return coinomatService.getXRate(currencyFrom, currencyTo, LANG)
                    .map { rate ->
                        val metadata = GatewayMetadata(
                                rate.inMin?.toBigDecimal() ?: BigDecimal.ZERO,
                                rate.inMax?.toBigDecimal() ?: BigDecimal.ZERO,
                                rate.feeOut?.toBigDecimal() ?: BigDecimal.ZERO)

                        return@map metadata
                    }
        } else {
            return Observable.error(Throwable("Args is not CoinomatMetadataArgs"))
        }

    }

    fun loadRate(crypto: String?, address: String?, fiat: String?, amount: String?): Observable<String> {
        return coinomatService.rate(crypto, address, fiat, amount)
    }

    fun loadLimits(crypto: String?, address: String?, fiat: String?): Observable<Limit> {
        return coinomatService.limits(crypto, address, fiat)
    }

    fun createTunnel(
            currencyFrom: String?,
            currencyTo: String?,
            address: String?,
            moneroPaymentId: String?
    ): Observable<CreateTunnel> {
        return coinomatService.createTunnel(currencyFrom, currencyTo, address, moneroPaymentId)
    }

    fun getTunnel(xtId: String?, k1: String?, k2: String?, lang: String): Observable<GetTunnel> {
        return coinomatService.getTunnel(xtId, k1, k2, lang)
    }

    fun getXRate(from: String?, to: String?, lang: String): Observable<XRate> {
        return coinomatService.getXRate(from, to, lang)
    }

    companion object {
        const val LANG: String = "ru_RU"
    }
}