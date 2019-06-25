/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data.manager

import com.wavesplatform.sdk.WavesPlatform
import com.wavesplatform.sdk.net.NetworkException
import com.wavesplatform.sdk.net.OnErrorListener
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.manager.base.BaseServiceManager
import com.wavesplatform.wallet.v2.data.manager.service.CoinomatService
import com.wavesplatform.wallet.v2.data.model.service.coinomat.CreateTunnelResponse
import com.wavesplatform.wallet.v2.data.model.service.coinomat.GetTunnelResponse
import com.wavesplatform.wallet.v2.data.model.service.coinomat.LimitResponse
import com.wavesplatform.wallet.v2.data.model.service.coinomat.XRateResponse
import io.reactivex.Observable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CoinomatServiceManager @Inject constructor() : BaseServiceManager() {

    fun loadRate(crypto: String?, address: String?, fiat: String?, amount: String?): Observable<String> {
        return coinomatService.rate(crypto, address, fiat, amount)
    }

    fun loadLimits(crypto: String?, address: String?, fiat: String?): Observable<LimitResponse> {
        return coinomatService.limits(crypto, address, fiat)
    }

    fun createTunnel(
            currencyFrom: String?,
            currencyTo: String?,
            address: String?,
            moneroPaymentId: String?
    ): Observable<CreateTunnelResponse> {
        return coinomatService.createTunnel(currencyFrom, currencyTo, address, moneroPaymentId)
    }

    fun getTunnel(xtId: String?, k1: String?, k2: String?, lang: String): Observable<GetTunnelResponse> {
        return coinomatService.getTunnel(xtId, k1, k2, lang)
    }

    fun getXRate(from: String?, to: String?, lang: String): Observable<XRateResponse> {
        return coinomatService.getXRate(from, to, lang)
    }

    companion object {

        private var onErrorListener: OnErrorListener? = null

        fun create(onErrorListener: OnErrorListener? = null): CoinomatService {
            this.onErrorListener = onErrorListener
            return WavesPlatform.service().createService(Constants.URL_COINOMAT,
                    object : OnErrorListener {
                        override fun onError(exception: NetworkException) {
                            CoinomatServiceManager.onErrorListener?.onError(exception)
                        }
                    })
                    .create(CoinomatService::class.java)
        }

        fun removeOnErrorListener() {
            onErrorListener = null
        }
    }
}