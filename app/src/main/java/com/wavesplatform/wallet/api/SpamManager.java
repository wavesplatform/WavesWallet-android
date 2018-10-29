package com.wavesplatform.wallet.api;

import android.support.annotation.VisibleForTesting;

import com.wavesplatform.wallet.data.factory.RxErrorHandlingCallAdapterFactory;

import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import io.reactivex.Observable;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import retrofit2.http.GET;

public class SpamManager {
    interface SpamApi {

        @GET("Scam%20tokens%20according%20to%20the%20opinion%20of%20Waves%20Community.csv")
        Observable<String> getSpamAssets();
    }

    private static SpamManager instance;
    private final SpamApi service;

    public static SpamManager get() {
        if (instance == null) {
            createInstance();
        }
        return instance;
    }

    public static SpamManager createInstance() {
        try {
            instance = new SpamManager();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return instance;
    }

    @VisibleForTesting
    public static SpamManager createTestInstance(SpamApi service) {
        try {
            instance = new SpamManager(service);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return instance;
    }

    private SpamManager() {
        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();;
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://github-proxy.wvservices.com/wavesplatform/waves-community/master/")
                .addConverterFactory(ScalarsConverterFactory.create())
                .addCallAdapterFactory(new RxErrorHandlingCallAdapterFactory())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(okHttpClient)
                .build();
        service = retrofit.create(SpamApi.class);
    }

    // Added for testing purposes only
    private SpamManager(SpamApi service) {
        this.service = service;
    }

    public Observable<Set<String>> getSpamAssets() {
        return service.getSpamAssets().map(str -> {
            Scanner scanner = new Scanner(str);
            Set<String> spam = new HashSet<>();
            while(scanner.hasNextLine()) {
                spam.add(scanner.nextLine().split(",")[0]);
            }
            scanner.close();
            return spam;
        });
    }
}
