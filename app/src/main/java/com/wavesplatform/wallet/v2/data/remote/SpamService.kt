package com.wavesplatform.wallet.v2.data.remote

import io.reactivex.Observable
import retrofit2.http.GET

interface SpamService {

    @GET("Scam%20tokens%20according%20to%20the%20opinion%20of%20Waves%20Community.csv")
    fun spamAssets(): Observable<String>

}
