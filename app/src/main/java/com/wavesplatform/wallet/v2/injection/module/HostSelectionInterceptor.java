package com.wavesplatform.wallet.v2.injection.module;

import com.wavesplatform.wallet.v2.data.model.remote.response.GlobalConfiguration;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;

public final class HostSelectionInterceptor implements Interceptor {

    private volatile String nodeHost;
    private volatile String dataHost;
    private volatile String matcherHost;
    private volatile GlobalConfiguration.Servers initServers;

    public HostSelectionInterceptor(GlobalConfiguration.Servers initServers) {
        this.initServers = new GlobalConfiguration.Servers(
                initServers.getNodeUrl(),
                initServers.getDataUrl(),
                initServers.getSpamUrl(),
                initServers.getMatcherUrl());
    }

    public void setHosts(GlobalConfiguration.Servers servers) {
        this.nodeHost = HttpUrl.parse(servers.getNodeUrl()).host();
        this.dataHost = HttpUrl.parse(servers.getDataUrl()).host();
        this.matcherHost = HttpUrl.parse(servers.getMatcherUrl()).host();
    }

    @Override
    public okhttp3.Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        if ((this.nodeHost != null || this.dataHost != null || this.matcherHost != null)
                && !request.url().host().equals("github-proxy.wvservices.com")) {

            String host = chain.request().url().host();
            String nodeHost = HttpUrl.parse(initServers.getNodeUrl()).host();
            String dataHost = HttpUrl.parse(initServers.getDataUrl()).host();
            String matcherHost = HttpUrl.parse(initServers.getMatcherUrl()).host();

            if (host.equals(nodeHost)) {
                host = nodeHost;
            } else if (host.equals(dataHost)) {
                host = dataHost;
            } else if (host.equals(matcherHost)) {
                host = matcherHost;
            }

            HttpUrl newUrl = request.url().newBuilder()
                    .host(host)
                    .build();
            request = request.newBuilder()
                    .url(newUrl)
                    .build();
        }
        return chain.proceed(request);
    }
}