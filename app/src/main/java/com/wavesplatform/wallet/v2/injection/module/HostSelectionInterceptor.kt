package com.wavesplatform.wallet.v2.injection.module

import com.wavesplatform.wallet.v2.data.model.remote.response.GlobalConfiguration
import com.wavesplatform.wallet.v2.util.notNull
import okhttp3.HttpUrl
import okhttp3.Interceptor
import java.io.IOException

class HostSelectionInterceptor(initServers: GlobalConfiguration.Servers) : Interceptor {

    @Volatile
    private var nodeHost: String? = null
    @Volatile
    private var dataHost: String? = null
    @Volatile
    private var matcherHost: String? = null
    @Volatile
    private var initServers: GlobalConfiguration.Servers = GlobalConfiguration.Servers(
            initServers.nodeUrl,
            initServers.dataUrl,
            initServers.spamUrl,
            initServers.matcherUrl)

    fun setHosts(servers: GlobalConfiguration.Servers) {
        HttpUrl.parse(servers.nodeUrl).notNull { httpUrl ->
            httpUrl.host().notNull {
                nodeHost = it
            }
        }

        HttpUrl.parse(servers.dataUrl).notNull { httpUrl ->
            httpUrl.host().notNull {
                dataHost = it
            }
        }

        HttpUrl.parse(servers.matcherUrl).notNull { httpUrl ->
            httpUrl.host().notNull {
                matcherHost = it
            }
        }
    }


    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        var request = chain.request()
        if (this.nodeHost != null || this.dataHost != null || this.matcherHost != null) {

            var host = chain.request().url().host()
            val nodeHost = getHost(this.nodeHost)
            val dataHost = getHost(this.dataHost)
            val matcherHost = getHost(this.matcherHost)

            when (host) {
                nodeHost -> {
                    nodeHost.notNull {
                        host = it
                    }
                }
                dataHost -> {
                    dataHost.notNull {
                        host = it
                    }
                }
                matcherHost -> {
                    matcherHost.notNull {
                        host = it
                    }
                }
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
        var result: String? = null
        url.notNull {
            HttpUrl.parse(it).notNull { httpUrl ->
                httpUrl.host().notNull { host ->
                    result = host
                }
            }
        }
        return result
    }
}