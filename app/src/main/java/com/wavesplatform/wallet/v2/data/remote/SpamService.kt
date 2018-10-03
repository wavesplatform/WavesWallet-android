package com.wavesplatform.wallet.v2.data.remote

import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Url

interface SpamService {

    @GET
    fun spamAssets(@Url url: String): Observable<String>

}
