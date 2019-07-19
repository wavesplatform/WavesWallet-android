/*
 * Created by Ershov Aleksandr on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.util

import okhttp3.HttpUrl
import okhttp3.Interceptor
import java.io.IOException

class HostInterceptor(initUrl: String) : Interceptor {

    @Volatile
    private var currentHost: String? = null
    private val initHost = HttpUrl.parse(initUrl)?.host()

    fun setHost(hostUrl: String) {
        val newHost = HttpUrl.parse(hostUrl)?.host()
        if (!initHost.equals(newHost)) {
            currentHost = HttpUrl.parse(hostUrl)?.host()
        }
    }

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        var request = chain.request()
        var host = chain.request().url().host()

        if (currentHost != null && host == initHost) {
            host = currentHost!!

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