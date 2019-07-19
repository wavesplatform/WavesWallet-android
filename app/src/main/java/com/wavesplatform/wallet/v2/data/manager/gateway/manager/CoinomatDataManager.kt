/*
 * Created by Eduard Zaydel on 25/6/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data.manager.gateway.manager

import com.wavesplatform.sdk.WavesSdk
import com.wavesplatform.sdk.crypto.WavesCrypto
import com.wavesplatform.sdk.model.response.node.transaction.TransferTransactionResponse
import com.wavesplatform.sdk.net.NetworkException
import com.wavesplatform.sdk.net.OnErrorListener
import com.wavesplatform.sdk.utils.SignUtil
import com.wavesplatform.sdk.utils.parseAlias
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.manager.NodeServiceManager
import com.wavesplatform.wallet.v2.data.manager.base.BaseServiceManager
import com.wavesplatform.wallet.v2.data.model.local.gateway.GatewayDepositArgs
import com.wavesplatform.wallet.v2.data.model.local.gateway.GatewayMetadataArgs
import com.wavesplatform.wallet.v2.data.model.local.gateway.GatewayWithdrawArgs
import com.wavesplatform.wallet.v2.data.model.remote.response.gateway.GatewayDeposit
import com.wavesplatform.wallet.v2.data.model.remote.response.gateway.GatewayMetadata
import com.wavesplatform.wallet.v2.data.model.remote.response.gateway.coinomat.Limit
import com.wavesplatform.wallet.v2.data.remote.CoinomatService
import com.wavesplatform.wallet.v2.util.EnvironmentManager
import com.wavesplatform.wallet.v2.util.PrefsUtil
import io.reactivex.Observable
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CoinomatDataManager @Inject constructor() : BaseServiceManager(), BaseGateway {

    @Inject
    lateinit var nodeDataManager: NodeServiceManager

    override fun loadGatewayMetadata(args: GatewayMetadataArgs): Observable<GatewayMetadata> {
        val currencyTo = Constants.coinomatCryptoCurrencies()[args.asset?.assetId]
        val currencyFrom = "${EnvironmentManager.netCode.toChar()}$currencyTo"

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
    }

    override fun makeWithdraw(args: GatewayWithdrawArgs): Observable<TransferTransactionResponse> {
        val currencyTo = Constants.coinomatCryptoCurrencies()[args.transaction.assetId]
        val currencyFrom = "${EnvironmentManager.netCode.toChar()}$currencyTo"

        if (currencyTo.isNullOrEmpty()) {
            return Observable.error(Throwable("Empty or null 'currencyTo' field"))
        }

        return coinomatService.createTunnel(currencyFrom, currencyTo, args.transaction.recipient, args.coinomatMoneroPaymentId)
                .flatMap { createTunnel ->
                    coinomatService.getTunnel(
                            createTunnel.tunnelId,
                            createTunnel.k1,
                            createTunnel.k2,
                            LANG)
                }
                .flatMap {
                    args.transaction.recipient = it.tunnel?.walletFrom ?: args.transaction.recipient
                    args.transaction.attachment = SignUtil.textToBase58(
                            it.tunnel?.attachment ?: "")

                    args.transaction.sign(App.getAccessManager().getWallet().seedStr)

                    nodeDataManager.transactionsBroadcast(args.transaction)
                }
                .map { tx ->
                    tx.recipient = tx.recipient.parseAlias()
                    saveLastSentAddress(tx.recipient)
                    return@map tx
                }
    }

    override fun makeDeposit(args: GatewayDepositArgs): Observable<GatewayDeposit> {
        val currencyFrom = Constants.coinomatCryptoCurrencies()[args.asset?.assetId]
        val currencyTo = "${EnvironmentManager.netCode.toChar()}$currencyFrom"

        if (currencyFrom.isNullOrEmpty()) {
            return Observable.error(Throwable("Empty or null 'currencyFrom' field"))
        }

        return coinomatService.createTunnel(currencyFrom, currencyTo, getAddress(), null)
                .flatMap { createTunnel ->
                    coinomatService.getTunnel(
                            createTunnel.tunnelId,
                            createTunnel.k1,
                            createTunnel.k2,
                            LANG)
                }.map { tunnel ->
                    val address = tunnel.tunnel?.walletFrom ?: ""
                    val min = tunnel.tunnel?.inMin?.toBigDecimal() ?: BigDecimal.ZERO

                    return@map GatewayDeposit(min, address, currencyFrom)
                }
    }

    fun loadRate(crypto: String?, address: String?, fiat: String?, amount: String?): Observable<String> {
        return coinomatService.rate(crypto, address, fiat, amount)
    }

    fun loadLimits(crypto: String?, address: String?, fiat: String?): Observable<Limit> {
        return coinomatService.limits(crypto, address, fiat)
    }

    private fun saveLastSentAddress(newAddress: String) {
        val addresses = prefsUtil.getGlobalValueList(PrefsUtil.KEY_LAST_SENT_ADDRESSES)
        var needAdd = true
        for (address in addresses) {
            if (newAddress == address) {
                needAdd = false
            }
        }
        if (needAdd) {
            val addressesList = addresses.toMutableList()
            if (addresses.size < 5) {
                addressesList.add(newAddress)
            } else {
                addressesList.removeAt(0)
                addressesList.add(newAddress)
            }
            prefsUtil.setGlobalValue(PrefsUtil.KEY_LAST_SENT_ADDRESSES, addressesList.toTypedArray())
        }
    }

    companion object {

        const val LANG: String = "ru_RU"

        private var onErrorListener: OnErrorListener? = null

        fun create(onErrorListener: OnErrorListener? = null): CoinomatService {
            this.onErrorListener = onErrorListener
            return WavesSdk.service().createService(Constants.URL_COINOMAT,
                    object : OnErrorListener {
                        override fun onError(exception: NetworkException) {
                            CoinomatDataManager.onErrorListener?.onError(exception)
                        }
                    })
                    .create(CoinomatService::class.java)
        }

        fun removeOnErrorListener() {
            onErrorListener = null
        }
    }
}