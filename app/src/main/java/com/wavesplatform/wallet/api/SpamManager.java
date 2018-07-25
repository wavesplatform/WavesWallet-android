package com.wavesplatform.wallet.api;

import com.wavesplatform.wallet.api.mather.MatherManager;
import com.wavesplatform.wallet.data.factory.RxErrorHandlingCallAdapterFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import io.reactivex.Observable;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
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
