/*
 * Created by Ershov Aleksandr on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.sdk.net

import com.wavesplatform.sdk.utils.Environment
import com.wavesplatform.sdk.utils.notNull
import okhttp3.HttpUrl
import okhttp3.Interceptor
import java.io.IOException

internal class HostSelectionInterceptor(initServers: Environment) : Interceptor {

    @Volatile
    private var nodeHost: String? = null
    @Volatile
    private var dataHost: String? = null
    @Volatile
    private var matcherHost: String? = null

    private val initNodeHost = HttpUrl.parse(initServers.nodeUrl)?.host()
    private val initDataHost = HttpUrl.parse(initServers.dataUrl)?.host()
    private val initMatcherHost = HttpUrl.parse(initServers.matcherUrl)?.host()

    fun setHosts(servers: Environment) {
        nodeHost = HttpUrl.parse(servers.nodeUrl)?.host()
        dataHost = HttpUrl.parse(servers.dataUrl)?.host()
        matcherHost = HttpUrl.parse(servers.matcherUrl)?.host()
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        var request = chain.request()
        if (this.nodeHost != null || this.dataHost != null || this.matcherHost != null) {

            var host = chain.request().url().host()
            when (host) {
                initNodeHost -> nodeHost.notNull { host = it }
                initDataHost -> dataHost.notNull { host = it }
                initMatcherHost -> matcherHost.notNull { host = it }
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
}