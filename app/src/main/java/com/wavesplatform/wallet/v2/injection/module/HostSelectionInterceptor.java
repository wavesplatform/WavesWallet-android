package com.wavesplatform.wallet.v2.injection.module;

import java.io.IOException;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public final class HostSelectionInterceptor implements Interceptor {

    private volatile String host;

    public void setHost(String host) {
        this.host = HttpUrl.parse(host).host();
    }

    @Override
    public okhttp3.Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        String reqUrl = request.url().host();

        String host = this.host;
        if (host != null) {
            HttpUrl newUrl = request.url().newBuilder()
                    .host(host)
                    .build();
            request = request.newBuilder()
                    .url(newUrl)
                    .build();
        }
        return chain.proceed(request);
    }

    public static void main(String[] args) throws Exception {
        HostSelectionInterceptor interceptor = new HostSelectionInterceptor();

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build();

        Request request = new Request.Builder()
                .url("http://www.coca-cola.com/robots.txt")
                .build();

        okhttp3.Call call1 = okHttpClient.newCall(request);
        okhttp3.Response response1 = call1.execute();
        System.out.println("RESPONSE FROM: " + response1.request().url());
        System.out.println(response1.body().string());

        interceptor.setHost("www.pepsi.com");

        okhttp3.Call call2 = okHttpClient.newCall(request);
        okhttp3.Response response2 = call2.execute();
        System.out.println("RESPONSE FROM: " + response2.request().url());
        System.out.println(response2.body().string());
    }
}