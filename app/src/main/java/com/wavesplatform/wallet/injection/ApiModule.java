package com.wavesplatform.wallet.injection;

import com.wavesplatform.wallet.data.api.ApiInterceptor;
import com.wavesplatform.wallet.data.stores.TransactionListStore;

import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import retrofit2.converter.jackson.JacksonConverterFactory;

@Module
public class ApiModule {

    private static final int API_TIMEOUT = 15;

    @Provides
    @Singleton
    protected TransactionListStore provideTransactionListStore() {
        return new TransactionListStore();
    }

    @Provides
    @Singleton
    protected OkHttpClient provideOkHttpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(API_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(API_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(API_TIMEOUT, TimeUnit.SECONDS)
                .retryOnConnectionFailure(false)
                .addInterceptor(new ApiInterceptor())
                .build();
    }

    @Provides
    @Singleton
    protected JacksonConverterFactory provideJacksonConverterFactory() {
        return JacksonConverterFactory.create();
    }

}
