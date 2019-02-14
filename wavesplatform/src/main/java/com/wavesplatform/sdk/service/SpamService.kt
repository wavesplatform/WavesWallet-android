package com.wavesplatform.sdk.service

import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Url

interface SpamService {

    @GET
    fun spamAssets(@Url url: String): Observable<String>

}
