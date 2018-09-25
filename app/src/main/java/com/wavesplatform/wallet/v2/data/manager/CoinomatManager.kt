package com.wavesplatform.wallet.v2.data.manager

import com.wavesplatform.wallet.v2.data.model.remote.response.CoinomatLimit
import io.reactivex.Observable
import javax.inject.Inject

class CoinomatManager @Inject constructor() : DataManager() {

    fun loadRate(crypto: String?, address: String?, fiat: String?, amount: String?): Observable<String> {
        return coinomatService.rate(crypto, address, fiat, amount)
    }

    fun loadLimits(crypto: String?, address: String?, fiat: String?): Observable<CoinomatLimit> {
        return coinomatService.limits(crypto, address, fiat)
    }

}