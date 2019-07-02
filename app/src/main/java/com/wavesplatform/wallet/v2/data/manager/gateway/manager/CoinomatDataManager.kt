/*
 * Created by Eduard Zaydel on 25/6/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data.manager.gateway.manager

import com.wavesplatform.wallet.v1.ui.auth.EnvironmentManager
import com.wavesplatform.wallet.v1.util.PrefsUtil
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.manager.NodeDataManager
import com.wavesplatform.wallet.v2.data.manager.base.BaseDataManager
import com.wavesplatform.wallet.v2.data.model.local.gateway.GatewayDepositArgs
import com.wavesplatform.wallet.v2.data.model.local.gateway.GatewayMetadataArgs
import com.wavesplatform.wallet.v2.data.model.local.gateway.GatewayWithdrawArgs
import com.wavesplatform.wallet.v2.data.model.remote.request.TransactionsBroadcastRequest
import com.wavesplatform.wallet.v2.data.model.remote.response.gateway.GatewayDeposit
import com.wavesplatform.wallet.v2.data.model.remote.response.gateway.GatewayMetadata
import com.wavesplatform.wallet.v2.data.model.remote.response.gateway.coinomat.CreateTunnel
import com.wavesplatform.wallet.v2.data.model.remote.response.gateway.coinomat.GetTunnel
import com.wavesplatform.wallet.v2.data.model.remote.response.gateway.coinomat.Limit
import com.wavesplatform.wallet.v2.data.model.remote.response.gateway.coinomat.XRate
import com.wavesplatform.wallet.v2.util.parseAlias
import io.reactivex.Observable
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CoinomatDataManager @Inject constructor() : BaseDataManager(), BaseGateway {

    @Inject
    lateinit var nodeDataManager: NodeDataManager

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

    override fun makeWithdraw(args: GatewayWithdrawArgs): Observable<TransactionsBroadcastRequest> {
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
                    args.transaction.attachment = it.tunnel?.attachment ?: ""

                    args.transaction.sign(getPrivateKey())

                    nodeDataManager.transactionsBroadcast(args.transaction)
                }
                .map { tx ->
                    tx.recipient = tx.recipient.parseAlias()
                    saveLastSentAddress(tx.recipient)
                    return@map tx
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
    }
}