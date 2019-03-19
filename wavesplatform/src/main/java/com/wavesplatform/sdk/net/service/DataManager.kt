package com.wavesplatform.sdk.net.service

import android.content.Context
import android.util.Log
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.ihsanbal.logging.Level
import com.ihsanbal.logging.LoggingInterceptor
import com.wavesplatform.sdk.BuildConfig
import com.wavesplatform.sdk.utils.Constants
import com.wavesplatform.sdk.utils.EnvironmentManager
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
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Singleton
open class DataManager(var context: Context,
                       private var adapterFactory: CallAdapter.Factory? = RxJava2CallAdapterFactory.create()) {

    lateinit var nodeService: NodeService
    lateinit var apiService: ApiService
    lateinit var matcherService: MatcherService
    lateinit var githubService: GithubService
    lateinit var coinomatService: CoinomatService
    private var cookies: HashSet<String> = hashSetOf()

    init {
        createServices()
    }

    private fun createServices() {
        nodeService = createRetrofit(
                Constants.URL_NODE,
                createClient(),
                adapterFactory ?: RxJava2CallAdapterFactory.create(),
                createGsonFactory()).create(NodeService::class.java)

        apiService = createRetrofit(
                Constants.URL_DATA,
                createClient(),
                adapterFactory ?: RxJava2CallAdapterFactory.create(),
                createGsonFactory()).create(ApiService::class.java)

        matcherService = createRetrofit(
                Constants.URL_MATCHER,
                createClient(),
                adapterFactory ?: RxJava2CallAdapterFactory.create(),
                createGsonFactory()).create(MatcherService::class.java)

        githubService = createRetrofit(
                Constants.URL_SPAM_FILE,
                createClient(),
                adapterFactory ?: RxJava2CallAdapterFactory.create(),
                createGsonFactory()).create(GithubService::class.java)

        coinomatService = createRetrofit(
                Constants.URL_COINOMAT,
                createClient(),
                adapterFactory ?: RxJava2CallAdapterFactory.create(),
                createGsonFactory()).create(CoinomatService::class.java)
    }

    fun setCallAdapterFactory(adapterFactory: CallAdapter.Factory) {
        this.adapterFactory = adapterFactory
        createServices()
    }

    private fun createRetrofit(
            baseUrl: String,
            client: OkHttpClient,
            adapterFactory: CallAdapter.Factory = RxJava2CallAdapterFactory.create(),
            gsonFactory: GsonConverterFactory): Retrofit {
        val retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addCallAdapterFactory(adapterFactory)
                .addConverterFactory(gsonFactory)
                .build()
        RetrofitCache.getInstance().addRetrofit(retrofit)
        return retrofit
    }

    private fun createClient(timeout: Long = 30L): OkHttpClient {
        return OkHttpClient.Builder()
                .cache(createCache())
                .readTimeout(timeout, TimeUnit.SECONDS)
                .writeTimeout(timeout, TimeUnit.SECONDS)
                .addInterceptor(receivedCookiesInterceptor())
                .addInterceptor(addCookiesInterceptor())
                .addInterceptor(CacheForceInterceptorNoNet())
                .addInterceptor(EnvironmentManager.createHostInterceptor())
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
                    && this.cookies.isEmpty()) {
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
                            .contains(Constants.URL_NODE)) {
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

    private fun createCache(): Cache {
        val cacheSize = 200 * 1024 * 1024
        val cacheDirectory = File(context.cacheDir, "httpcache")
        return Cache(cacheDirectory, cacheSize.toLong())
    }

    private fun createGsonFactory(): GsonConverterFactory {
        return GsonConverterFactory.create(GsonBuilder()
                .setLenient()
                .setPrettyPrinting()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                .create())
    }
}
