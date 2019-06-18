/*
 * Created by Eduard Zaydel on 18/6/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data.manager.gateway

import com.wavesplatform.wallet.v2.data.manager.base.BaseDataManager
import com.wavesplatform.wallet.v2.data.model.remote.response.coinomat.CreateTunnel
import com.wavesplatform.wallet.v2.data.model.remote.response.coinomat.GetTunnel
import com.wavesplatform.wallet.v2.data.model.remote.response.coinomat.Limit
import com.wavesplatform.wallet.v2.data.model.remote.response.coinomat.XRate
import io.reactivex.Observable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CoinomatDataManager @Inject constructor() : BaseDataManager() {

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
}