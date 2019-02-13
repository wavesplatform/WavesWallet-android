package com.wavesplatform.sdk.service

import android.content.Context
import android.util.Log
import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.ihsanbal.logging.Level
import com.ihsanbal.logging.LoggingInterceptor
import com.wavesplatform.sdk.BuildConfig
import com.wavesplatform.sdk.Constants
import com.wavesplatform.sdk.Wavesplatform
import com.wavesplatform.sdk.model.request.*
import com.wavesplatform.sdk.model.response.*
import io.reactivex.Observable
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import ren.yale.android.retrofitcachelibrx2.RetrofitCache
import ren.yale.android.retrofitcachelibrx2.intercept.CacheForceInterceptorNoNet
import ren.yale.android.retrofitcachelibrx2.intercept.CacheInterceptorOnNet
import retrofit2.CallAdapter
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import java.io.File
import java.util.concurrent.TimeUnit

interface NodeService {

    @GET("assets/balance/{address}")
    fun assetsBalance(@Path("address") address: String?): Observable<AssetBalances>

    @GET("addresses/balance/{address}")
    fun wavesBalance(@Path("address") address: String?): Observable<WavesBalance>

    @GET("transactions/address/{address}/limit/{limit}")
    fun transactionList(@Path("address") address: String?,
                        @Path("limit") limit: Int): Observable<List<List<Transaction>>>

    @GET("assets/balance/{address}/{assetId}")
    fun addressAssetBalance(@Path("address") address: String?,
                            @Path("assetId") assetId: String?): Observable<AddressAssetBalance>

    @GET("transactions/info/{asset}")
    fun getTransactionsInfo(@Path("asset") asset: String): Observable<TransactionsInfo>

    @POST("assets/broadcast/transfer")
    fun broadcastTransfer(@Body tx: TransferTransactionRequest): Observable<TransferTransactionRequest>

    @POST("assets/broadcast/issue")
    fun broadcastIssue(@Body tx: IssueTransactionRequest): Observable<IssueTransactionRequest>

    @POST("assets/broadcast/reissue")
    fun broadcastReissue(@Body tx: ReissueTransactionRequest): Observable<ReissueTransactionRequest>

    @POST("transactions/broadcast")
    fun createAlias(@Body createAliasRequest: AliasRequest): Observable<Alias>

    @GET("transactions/unconfirmed")
    fun unconfirmedTransactions(): Observable<List<Transaction>>

    @POST("transactions/broadcast")
    fun transactionsBroadcast(@Body tx: TransactionsBroadcastRequest): Observable<TransactionsBroadcastRequest>

    @GET("blocks/height")
    fun currentBlocksHeight(): Observable<Height>

    @GET("leasing/active/{address}")
    fun activeLeasing(@Path("address") address: String?): Observable<List<Transaction>>

    @POST("transactions/broadcast")
    fun createLeasing(@Body createLeasingRequest: CreateLeasingRequest): Observable<Transaction>

    @POST("transactions/broadcast")
    fun cancelLeasing(@Body cancelLeasingRequest: CancelLeasingRequest): Observable<Transaction>

    @POST("transactions/broadcast")
    fun burn(@Body burnRequest: BurnRequest): Observable<BurnRequest>

    @GET("addresses/scriptInfo/{address}")
    fun scriptAddressInfo(@Path("address") address: String): Observable<ScriptInfo>

    @GET("/assets/details/{assetId}")
    fun assetDetails(@Path("assetId") assetId: String): Observable<AssetsDetails>

    companion object Factory {

        // todo задать параметры и заменить везде ^
        fun create(context: Context,
                   factory: CallAdapter.Factory,
                   timeout: Long = 30L): NodeService {
            val retrofit = Retrofit.Builder()
                    .baseUrl(Constants.URL_NODE)
                    .client(createClient(context, timeout))
                    .addCallAdapterFactory(factory)
                    .addConverterFactory(GsonConverterFactory.create(createGson()))
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build()
            RetrofitCache.getInstance().addRetrofit(retrofit)
            return retrofit.create(NodeService::class.java)
        }

        private fun createClient(context: Context, timeout: Long = 30L) : OkHttpClient {
            return OkHttpClient.Builder()
                    .cache(createCache(context))
                    .readTimeout(timeout, TimeUnit.SECONDS)
                    .writeTimeout(timeout, TimeUnit.SECONDS)
                    .addInterceptor(receivedCookiesInterceptor())
                    .addInterceptor(addCookiesInterceptor())
                    .addInterceptor(CacheForceInterceptorNoNet())
                    .addNetworkInterceptor(CacheInterceptorOnNet())
                    .addInterceptor(LoggingInterceptor.Builder()
                            .loggable(BuildConfig.DEBUG)
                            .setLevel(Level.BASIC)
                            .log(Log.INFO)
                            .request("Request")
                            .response("Response")
                            .build())
                    .build()
        }

        private fun receivedCookiesInterceptor(): Interceptor {
            return Interceptor { chain ->
                val originalResponse = chain.proceed(chain.request())
                if (originalResponse.request().url().url().toString()
                                .contains(Constants.URL_NODE)
                        && originalResponse.headers("Set-Cookie").isNotEmpty()
                        && Wavesplatform.getCookies().isEmpty()) {
                    val cookies = originalResponse.headers("Set-Cookie")
                            .toHashSet()
                    Wavesplatform.setCookies(cookies)
                }
                originalResponse
            }
        }

        private fun addCookiesInterceptor(): Interceptor {
            return Interceptor { chain ->
                val cookies = Wavesplatform.getCookies()
                if (cookies.isNotEmpty() && chain.request().url().url().toString()
                                .contains(Constants.URL_NODE)) {
                    val builder = chain.request().newBuilder()
                    cookies.forEach {
                        builder.addHeader("Cookie", it)
                    }
                    chain.proceed(builder.build())
                } else {
                    chain.proceed(chain.request())
                }
            }
        }

        private fun createCache(context: Context) : Cache {
            val cacheSize = 200 * 1024 * 1024
            val cacheDirectory = File(context.cacheDir, "httpcache")
            return Cache(cacheDirectory, cacheSize.toLong())
        }

        private fun createGson() : Gson {
            return GsonBuilder()
                    .setLenient()
                    .setPrettyPrinting()
                    .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES) // if filed status_code need as statusCode
                    .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                    .create()
        }
    }
}
