/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data.manager

import com.wavesplatform.sdk.Wavesplatform
import com.wavesplatform.sdk.net.CallAdapterFactory
import com.wavesplatform.sdk.net.OnErrorListener
import com.wavesplatform.sdk.net.RetrofitException
import com.wavesplatform.sdk.net.model.response.coinomat.CreateTunnelResponse
import com.wavesplatform.sdk.net.model.response.coinomat.GetTunnelResponse
import com.wavesplatform.sdk.net.model.response.coinomat.LimitResponse
import com.wavesplatform.sdk.net.model.response.coinomat.XRateResponse
import com.wavesplatform.wallet.v2.data.manager.service.CoinomatService
import com.wavesplatform.sdk.utils.Constants
import com.wavesplatform.wallet.v2.data.manager.base.BaseDataManager
import io.reactivex.Observable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CoinomatManager @Inject constructor() : BaseDataManager() {

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
            val adapterFactory = CallAdapterFactory(object : OnErrorListener{
                override fun onError(exception: RetrofitException) {
                    CoinomatManager.onErrorListener?.onError(exception)
                }
            })
            return Wavesplatform.service().createService(Constants.URL_COINOMAT, adapterFactory)
                    .create(CoinomatService::class.java)
        }

        fun removeOnErrorListener() {
            onErrorListener = null
        }
    }
}