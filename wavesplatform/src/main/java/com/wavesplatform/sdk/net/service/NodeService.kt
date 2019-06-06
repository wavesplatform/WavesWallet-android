/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.sdk.net.service

import com.wavesplatform.sdk.model.response.*
import com.wavesplatform.sdk.model.transaction.node.*
import io.reactivex.Observable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Service for working with nodes.
 * For more information: [Nodes Swagger]({https://nodes.wavesnodes.com/api-docs/index.html#!/assets/balances)
 */
interface NodeService {

    /**
     * Account's balances for all assets by address
     * @param address Address
     */
    @GET("assets/balance/{address}")
    fun assetsBalance(@Path("address") address: String?): Observable<AssetBalancesResponse>

    /**
     * Account's Waves balance
     * @param address Address
     */
    @GET("addresses/balance/{address}")
    fun wavesBalance(@Path("address") address: String?): Observable<WavesBalanceResponse>

    /**
     * Get list of transactions where specified address has been involved
     * @param address Address
     * @param limit Number of transactions to be returned. Max is last 1000.
     */
    @GET("transactions/address/{address}/limit/{limit}")
    fun transactionList(@Path("address") address: String?,
                        @Path("limit") limit: Int): Observable<List<List<TransactionResponse>>>

    @GET("assets/balance/{address}/{assetId}")
    fun addressAssetBalance(
        @Path("address") address: String?,
        @Path("assetId") assetId: String?
    ): Observable<AddressAssetBalanceResponse>

    /**
     * Get a transaction info by it's id
     * @param id id of transaction
     */
    @GET("transactions/info/{id}")
    fun getTransactionsInfo(@Path("id") id: String): Observable<TransactionsInfoResponse>

    /**
     *
     */
    @POST("assets/broadcast/issue")
    fun broadcastIssue(@Body transaction: IssueTransaction): Observable<IssueTransaction>

    /**
     *
     */
    @POST("assets/broadcast/reissue")
    fun broadcastReissue(@Body transaction: ReissueTransaction): Observable<ReissueTransaction>

    /**
     * Create alias - short name for address
     * @param transaction AliasTransaction with signature by privateKey
     */
    @POST("transactions/broadcast")
    fun broadcastAlias(@Body transaction: AliasTransaction): Observable<AliasResponse>

    /**
     * Broadcast a signed create leasing transaction
     * @param transaction CreateLeasingTransaction with signature by privateKey
     */
    @POST("transactions/broadcast")
    fun broadcastCreateLeasing(@Body transaction: CreateLeasingTransaction): Observable<TransactionResponse>

    /**
     * Broadcast a signed cancel leasing transaction
     * @param transaction CancelLeasingTransaction with signature by privateKey
     */
    @POST("transactions/broadcast")
    fun broadcastCancelLeasing(@Body transaction: CancelLeasingTransaction): Observable<TransactionResponse>

    /**
     * Broadcast a signed burn transaction
     * @param transaction BurnTransaction with signature by privateKey
     */
    @POST("transactions/broadcast")
    fun broadcastBurn(@Body transaction: BurnTransaction): Observable<BurnTransaction>

    /**
     * Broadcast a signed transfer transaction
     * @param transaction TransferTransaction with signature by privateKey
     */
    @POST("transactions/broadcast")
    fun broadcastTransfer(@Body transaction: TransferTransaction): Observable<TransferTransaction>

    /**
     * Get current Waves block-chain height
     */
    @GET("blocks/height")
    fun currentBlocksHeight(): Observable<HeightResponse>

    /**
     * Active leasing transactions of account
     * @param address Address
     */
    @GET("leasing/active/{address}")
    fun activeLeasing(@Path("address") address: String?): Observable<List<TransactionResponse>>

    /**
     * Account's script
     * @param address Address
     */
    @GET("addresses/scriptInfo/{address}")
    fun scriptAddressInfo(@Path("address") address: String): Observable<ScriptInfoResponse>

    /**
     * Provides detailed information about given asset
     * @param assetId Asset Id
     */
    @GET("/assets/details/{assetId}")
    fun assetDetails(@Path("assetId") assetId: String): Observable<AssetsDetailsResponse>

    /**
     * Current Node time (UTC)
     */
    @GET("/utils/time")
    fun utilsTime(): Observable<UtilsTimeResponse>

    /**
     * Get list of unconfirmed transactions
     */
    @GET("transactions/unconfirmed")
    fun unconfirmedTransactions(): Observable<List<TransactionResponse>>
}
