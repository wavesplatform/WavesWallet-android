package com.wavesplatform.sdk.service

import com.wavesplatform.sdk.Constants
import io.reactivex.Observable
import ren.yale.android.retrofitcachelibrx2.RetrofitCache
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Url

interface SpamService {

    @GET
    fun spamAssets(@Url url: String): Observable<String>

    companion object Factory {
        fun create(): SpamService {
            val retrofit = Retrofit.Builder()
                    .baseUrl(Constants.URL_SPAM_FILE)
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            RetrofitCache.getInstance().addRetrofit(retrofit)
            return retrofit.create(SpamService::class.java)
        }
    }

}
