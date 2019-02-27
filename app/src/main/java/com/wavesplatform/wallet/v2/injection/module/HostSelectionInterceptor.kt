package com.wavesplatform.wallet.v2.injection.module

import com.wavesplatform.wallet.v2.data.model.remote.response.GlobalConfiguration
import com.wavesplatform.wallet.v2.util.notNull
import okhttp3.HttpUrl
import okhttp3.Interceptor
import java.io.IOException

class HostSelectionInterceptor : Interceptor {

    @Volatile
    private var nodeHost: String? = null
    @Volatile
    private var dataHost: String? = null
    @Volatile
    private var matcherHost: String? = null

    fun setHosts(servers: GlobalConfiguration.Servers) {
        nodeHost = HttpUrl.parse(servers.nodeUrl)?.host()
        dataHost = HttpUrl.parse(servers.dataUrl)?.host()
        matcherHost = HttpUrl.parse(servers.matcherUrl)?.host()
    }


    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        var request = chain.request()
        if (this.nodeHost != null || this.dataHost != null || this.matcherHost != null) {

            var host = chain.request().url().host()
            val nodeHost = HttpUrl.parse(nodeHost ?: "")?.host()
            val dataHost = HttpUrl.parse(dataHost ?: "")?.host()
            val matcherHost = HttpUrl.parse(matcherHost ?: "")?.host()

            when (host) {
                nodeHost -> nodeHost.notNull { host = it }
                dataHost -> dataHost.notNull { host = it }
                matcherHost -> matcherHost.notNull { host = it }
            }

            val newUrl = request.url().newBuilder()
                    .host(host)
                    .build()

            request = request.newBuilder()
                    .url(newUrl)
                    .build()
        }
        return chain.proceed(request)
    }

    private fun getHost(url: String?): String? {
        return HttpUrl.parse(url ?: "")?.host()
    }
}