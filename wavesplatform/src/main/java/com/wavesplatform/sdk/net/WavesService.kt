package com.wavesplatform.sdk.net

import android.content.Context
import android.util.Log
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.ihsanbal.logging.Level
import com.ihsanbal.logging.LoggingInterceptor
import com.wavesplatform.sdk.BuildConfig
import com.wavesplatform.sdk.WavesSdk
import com.wavesplatform.sdk.net.service.DataService
import com.wavesplatform.sdk.net.service.MatcherService
import com.wavesplatform.sdk.net.service.NodeService
import com.wavesplatform.sdk.utils.WavesConstants
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.CallAdapter
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Net-services for sending transactions and getting data from blockchain
 * and other Waves services
 */
class WavesService(private var context: Context) {

    private lateinit var nodeService: NodeService
    private lateinit var dataService: DataService
    private lateinit var matcherService: MatcherService
    private var cookies: HashSet<String> = hashSetOf()
    private var adapterFactory: CallAdapter.Factory
    private val onErrorListeners = mutableListOf<OnErrorListener>()

    init {
        adapterFactory = CallAdapterFactory(object : OnErrorListener {
            override fun onError(exception: NetworkException) {
                onErrorListeners.forEach { it.onError(exception) }
            }
        })
        createServices()
    }

    /**
     * Main Waves blockchain service, it helps to sends transactions in blockchain
     * and also takes some data for address
     */
    fun getNode(): NodeService {
        return nodeService
    }

    /**
     * Matcher Service for DEX, decentralized exchange of Waves.
     * It collects and matches all orders from users by Exchange transactions
     * and sends it to blockchain
     */
    fun getMatcher(): MatcherService {
        return matcherService
    }

    /**
     * Special Waves-service for simple and convenient way to get data from Waves blockchain
     */
    fun getDataService(): DataService {
        return dataService
    }

    /**
     * Add Error Listener to retrofit. Add it if you need know about net errors
     */
    fun addOnErrorListener(errorListener: OnErrorListener) {
        onErrorListeners.add(errorListener)
    }

    /**
     * Remove all Retrofit listeners
     */
    fun removeOnErrorListener(errorListener: OnErrorListener) {
        onErrorListeners.remove(errorListener)
    }

    fun createService(baseUrl: String, errorListener: OnErrorListener): Retrofit {
        return createService(baseUrl, CallAdapterFactory(errorListener))
    }

    private fun createService(
        baseUrl: String,
        adapterFactory: CallAdapter.Factory = RxJava2CallAdapterFactory.create()
    )
            : Retrofit {
        return Retrofit.Builder()
            .baseUrl(addSlash(baseUrl))
            .client(createClient())
            .addConverterFactory(ScalarsConverterFactory.create())
            .addCallAdapterFactory(adapterFactory)
            .addConverterFactory(createGsonFactory())
            .build()
    }

    internal fun createServices() {
        nodeService = createService(addSlash(WavesSdk.getEnvironment().nodeUrl), adapterFactory)
            .create(NodeService::class.java)

        dataService = createService(addSlash(WavesSdk.getEnvironment().dataUrl), adapterFactory)
            .create(DataService::class.java)

        matcherService = createService(addSlash(WavesSdk.getEnvironment().matcherUrl), adapterFactory)
            .create(MatcherService::class.java)
    }

    private fun addSlash(url: String): String {
        return if (url.endsWith("/")) {
            url
        } else {
            "$url/"
        }
    }

    private fun createClient(timeout: Long = 30L): OkHttpClient {
        val okHttpClientBuilder = OkHttpClient.Builder()
            .cache(createCache())
            .readTimeout(timeout, TimeUnit.SECONDS)
            .writeTimeout(timeout, TimeUnit.SECONDS)
            .addInterceptor(receivedCookiesInterceptor())
            .addInterceptor(addCookiesInterceptor())
            .addInterceptor(createHostInterceptor())
            .addInterceptor(
                LoggingInterceptor.Builder()
                    .loggable(BuildConfig.DEBUG)
                    .setLevel(Level.BASIC)
                    .log(Log.INFO)
                    .request("Request")
                    .response("Response")
                    .build()
            )

        return okHttpClientBuilder.build()
    }

    private fun receivedCookiesInterceptor(): Interceptor {
        return Interceptor { chain ->
            val originalResponse = chain.proceed(chain.request())
            if (originalResponse.request().url().url().toString()
                    .contains(WavesConstants.URL_NODE)
                && originalResponse.headers("Set-Cookie").isNotEmpty()
                && this.cookies.isEmpty()
            ) {
                val cookies = originalResponse.headers("Set-Cookie")
                    .toHashSet()
                this.cookies = cookies
            }
            originalResponse
        }
    }

    private fun addCookiesInterceptor(): Interceptor {
        return Interceptor { chain ->
            if (this.cookies.isNotEmpty() && chain.request().url().url().toString()
                    .contains(WavesConstants.URL_NODE)
            ) {
                val builder = chain.request().newBuilder()
                this.cookies.forEach {
                    builder.addHeader("Cookie", it)
                }
                chain.proceed(builder.build())
            } else {
                chain.proceed(chain.request())
            }
        }
    }

    private fun createHostInterceptor(): HostSelectionInterceptor {
        return HostSelectionInterceptor(WavesSdk.getEnvironment())
    }

    private fun createCache(): Cache {
        val cacheSize = 200 * 1024 * 1024
        val cacheDirectory = File(context.cacheDir, "httpcache")
        return Cache(cacheDirectory, cacheSize.toLong())
    }

    private fun createGsonFactory(): GsonConverterFactory {
        return GsonConverterFactory.create(
            GsonBuilder()
                .serializeNulls()
                .setLenient()
                .setPrettyPrinting()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                .create()
        )
    }
}
