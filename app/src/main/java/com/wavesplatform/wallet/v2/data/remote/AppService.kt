package com.wavesplatform.wallet.v2.data.remote

import com.wavesplatform.wallet.v2.data.model.remote.request.Posts
import io.reactivex.Observable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AppService{

    @GET("users")
    fun getCompanies() : Observable<List<Any>>

    @POST("posts")
    fun addPost(@Body posts: Posts) : Observable<Any>

}
