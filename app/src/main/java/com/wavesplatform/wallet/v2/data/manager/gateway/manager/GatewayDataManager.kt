/*
 * Created by Eduard Zaydel on 25/6/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data.manager.gateway.manager

import com.wavesplatform.sdk.WavesSdk
import com.wavesplatform.sdk.model.response.node.transaction.TransferTransactionResponse
import com.wavesplatform.sdk.net.NetworkException
import com.wavesplatform.sdk.net.OnErrorListener
import com.wavesplatform.sdk.utils.MoneyUtil.Companion.getScaledText
import com.wavesplatform.sdk.utils.SignUtil
import com.wavesplatform.sdk.utils.clearBalance
import com.wavesplatform.sdk.utils.parseAlias
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.manager.base.BaseServiceManager
import com.wavesplatform.wallet.v2.data.model.local.gateway.GatewayDepositArgs
import com.wavesplatform.wallet.v2.data.model.local.gateway.GatewayMetadataArgs
import com.wavesplatform.wallet.v2.data.model.local.gateway.GatewayWithdrawArgs
import com.wavesplatform.wallet.v2.data.model.remote.request.gateway.InitGatewayRequest
import com.wavesplatform.wallet.v2.data.model.remote.response.gateway.GatewayDeposit
import com.wavesplatform.wallet.v2.data.model.remote.response.gateway.GatewayMetadata
import com.wavesplatform.wallet.v2.data.remote.GatewayService
import com.wavesplatform.wallet.v2.util.EnvironmentManager
import io.reactivex.Observable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GatewayDataManager @Inject constructor() : BaseServiceManager(), BaseGateway {

    override fun loadGatewayMetadata(args: GatewayMetadataArgs): Observable<GatewayMetadata> {
        return gatewayService.initWithdraw(InitGatewayRequest(args.address, args.asset?.assetId))
                .map { response ->
                    val gatewayFee = getScaledText(response.fee, args.asset).clearBalance().toBigDecimal()
                    val gatewayMin = getScaledText(response.minAmount, args.asset).clearBalance().toBigDecimal()
                    val gatewayMax = getScaledText(response.maxAmount, args.asset).clearBalance().toBigDecimal()

                    return@map GatewayMetadata(gatewayMin, gatewayMax, gatewayFee, response.processId, response.recipientAddress)
                }
    }

    override fun makeWithdraw(args: GatewayWithdrawArgs): Observable<TransferTransactionResponse> {
        return loadGatewayMetadata(GatewayMetadataArgs(args.asset, args.transaction.recipient))
                .flatMap { metadata ->
                    args.transaction.attachment = SignUtil.textToBase58(
                            metadata.gatewayProcessId ?: "")
                    args.transaction.recipient = metadata.gatewayRecipientAddress ?: args.transaction.recipient

                    args.transaction.sign(App.getAccessManager().getWallet()?.seedStr ?: "")

                    val gatewayTransaction = GatewayWithdrawArgs.Transaction(
                            args.transaction, App.getAccessManager().getWallet()?.address ?: "")

                    return@flatMap gatewayService.sendWithdrawTransaction(gatewayTransaction)
                            .map {
                                val txResponse = TransferTransactionResponse(
                                        args.transaction.assetId,
                                        args.transaction.recipient.parseAlias(),
                                        args.transaction.amount,
                                        args.transaction.attachment,
                                        args.transaction.feeAssetId)
                                txResponse.timestamp = args.transaction.timestamp
                                txResponse.fee = args.transaction.fee
                                txResponse.version = args.transaction.version
                                txResponse.proofs.addAll(args.transaction.proofs)
                                return@map txResponse
                            }
                }
    }

    override fun makeDeposit(args: GatewayDepositArgs): Observable<GatewayDeposit> {
        return gatewayService.initDeposit(InitGatewayRequest(getAddress(), args.asset?.assetId))
                .map { response ->
                    val currencyFrom = Constants.coinomatCryptoCurrencies()[args.asset?.assetId]
                    val address = response.address
                    val gatewayMin = getScaledText(response.minAmount, args.asset).clearBalance().toBigDecimal()

                    return@map GatewayDeposit(gatewayMin, address, currencyFrom)
                }
    }

    companion object {

        private var onErrorListener: OnErrorListener? = null

        fun create(onErrorListener: OnErrorListener? = null): GatewayService {
            this.onErrorListener = onErrorListener
            return WavesSdk.service().createService(EnvironmentManager.servers.gatewayUrl,
                    object : OnErrorListener {
                        override fun onError(exception: NetworkException) {
                            GatewayDataManager.onErrorListener?.onError(exception)
                        }
                    })
                    .create(GatewayService::class.java)
        }

        fun removeOnErrorListener() {
            onErrorListener = null
        }
    }
}