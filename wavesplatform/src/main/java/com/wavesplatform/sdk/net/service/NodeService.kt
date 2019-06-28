/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.sdk.net.service

import com.wavesplatform.sdk.model.request.node.*
import com.wavesplatform.sdk.model.response.node.*
import com.wavesplatform.sdk.model.response.node.AssetBalancesResponse
import com.wavesplatform.sdk.model.response.node.IssueTransactionResponse
import com.wavesplatform.sdk.model.response.node.transaction.*
import io.reactivex.Observable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Service for working with nodes.
 * For more information: [Nodes Swagger]({https://nodes.wavesnodes.com/api-docs/index.html#!/assets/balances)
 * Any transactions you can check at [Waves Explorer]({https://wavesexplorer.com/)
 */
interface NodeService {

    /**
     * Account's Waves balance
     * @param address Address
     */
    @GET("addresses/balance/{address}")
    fun wavesBalance(@Path("address") address: String?): Observable<WavesBalanceResponse>

    /**
     * Account's script
     * @param address Address
     */
    @GET("addresses/scriptInfo/{address}")
    fun scriptInfo(@Path("address") address: String): Observable<ScriptInfoResponse>

    /**
     * Account's balances for all assets by address
     * @param address Address
     */
    @GET("assets/balance/{address}")
    fun assetsBalance(@Path("address") address: String?): Observable<AssetBalancesResponse>

    /**
     * Account's assetId balance by address
     * @param address Address
     * @param assetId AssetId
     */
    @GET("assets/balance/{address}/{assetId}")
    fun addressBalance(
            @Path("address") address: String?,
            @Path("assetId") assetId: String?
    ): Observable<AddressAssetBalanceResponse>

    /**
     * Provides detailed information about given asset
     * @param assetId Asset Id
     */
    @GET("/assets/details/{assetId}")
    fun assetDetails(@Path("assetId") assetId: String): Observable<AssetsDetailsResponse>

    /**
     * Get list of transactions where specified address has been involved
     * @param address Address
     * @param limit Number of transactions to be returned. Max is last 1000.
     */
    @GET("transactions/address/{address}/limit/{limit}")
    fun transactionsAddress(@Path("address") address: String?,
                            @Path("limit") limit: Int): Observable<List<List<HistoryTransactionResponse>>>
    /**
     * Get current Waves block-chain height
     */
    @GET("blocks/height")
    fun blockHeight(): Observable<HeightResponse>

    /**
     * Active leasing transactions of account
     * @param address Address
     */
    @GET("leasing/active/{address}")
    fun leasingActive(@Path("address") address: String?): Observable<List<HistoryTransactionResponse>>

    /**
     * Current Node time (UTC)
     */
    @GET("/utils/time")
    fun utilsTime(): Observable<UtilsTimeResponse>


    // Broadcast transactions //////////////////////////////////////

    /**
     * Broadcast issue-transaction (typeId = 3)
     * @param transaction IssueTransaction with signature by privateKey
     */
    @POST("transactions/broadcast")
    fun transactionsBroadcast(@Body transaction: IssueTransaction): Observable<IssueTransactionResponse>

    /**
     * Broadcast transfer-transaction (typeId = 4)
     * @param transaction TransferTransaction with signature by privateKey
     */
    @POST("transactions/broadcast")
    fun transactionsBroadcast(@Body transaction: TransferTransaction): Observable<TransferTransactionResponse>

    /**
     * Broadcast reissue-transaction (typeId = 5)
     * @param transaction ReissueTransaction with signature by privateKey
     */
    @POST("transactions/broadcast")
    fun transactionsBroadcast(@Body transaction: ReissueTransaction): Observable<ReissueTransactionResponse>

    /**
     * Broadcast burn-transaction (typeId = 6)
     * @param transaction BurnTransaction with signature by privateKey
     */
    @POST("transactions/broadcast")
    fun transactionsBroadcast(@Body transaction: BurnTransaction): Observable<BurnTransactionResponse>

    /**
     * Broadcast exchange-transaction (typeId = 7)
     * @param transaction ExchangeTransaction with signature by privateKey
     *//*
    @POST("transactions/broadcast")
    fun transactionsBroadcast(@Body transaction: ExchangeTransaction): Observable<ExchangeTransactionResponse>*/

    /**
     * Broadcast lease-transaction (typeId = 8)
     * @param transaction LeaseTransaction with signature by privateKey
     */
    @POST("transactions/broadcast")
    fun transactionsBroadcast(@Body transaction: LeaseTransaction): Observable<LeaseTransactionResponse>

    /**
     * Broadcast lease-cancel-transaction (typeId = 9)
     * @param transaction LeaseCancelTransaction with signature by privateKey
     */
    @POST("transactions/broadcast")
    fun transactionsBroadcast(@Body transaction: LeaseCancelTransaction): Observable<LeaseCancelTransactionResponse>

    /**
     * Create alias-transaction. Alias - short name for address  (typeId = 10)
     * @param transaction AliasTransaction with signature by privateKey
     */
    @POST("transactions/broadcast")
    fun transactionsBroadcast(@Body transaction: AliasTransaction): Observable<AliasTransactionResponse>

    /**
     * Broadcast mass-transfer-transaction (typeId = 11)
     * @param transaction MassTransferTransaction with signature by privateKey
     */
    @POST("transactions/broadcast")
    fun transactionsBroadcast(@Body transaction: MassTransferTransaction): Observable<MassTransferTransactionResponse>

    /**
     * Broadcast data-transaction (typeId = 12)
     * @param transaction DataTransaction with signature by privateKey
     */
    @POST("transactions/broadcast")
    fun transactionsBroadcast(@Body transaction: DataTransaction): Observable<DataTransactionResponse>

    /**
     * Broadcast set-script-transaction, also called address-script  (typeId = 13)
     * @param transaction SetScriptTransaction with signature by privateKey
     */
    @POST("transactions/broadcast")
    fun transactionsBroadcast(@Body transaction: SetScriptTransaction): Observable<SetScriptTransactionResponse>

    /**
     * Broadcast sponsorship-transaction  (typeId = 14)
     * @param transaction SponsorshipTransaction with signature by privateKey
     */
    @POST("transactions/broadcast")
    fun transactionsBroadcast(@Body transaction: SponsorshipTransaction): Observable<SponsorshipTransactionResponse>

    /**
     * Broadcast set-asset-script-transaction (typeId = 15)
     * @param transaction SetAssetScriptTransaction with signature by privateKey
     */
    @POST("transactions/broadcast")
    fun transactionsBroadcast(@Body transaction: SetAssetScriptTransaction): Observable<SetAssetScriptTransactionResponse>

    /**
     * Broadcast invoke-script-transaction (typeId = 16)
     * @param transaction InvokeScriptTransaction with signature by privateKey
     */
    @POST("transactions/broadcast")
    fun transactionsBroadcast(@Body transaction: InvokeScriptTransaction): Observable<InvokeScriptTransactionResponse>

}
