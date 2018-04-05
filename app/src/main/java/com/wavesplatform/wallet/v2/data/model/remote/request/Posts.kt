package com.wavesplatform.wallet.v2.data.model.remote.request
import com.google.gson.annotations.SerializedName


/**
 * Created by anonymous on 16.12.17.
 */

data class Posts(
		@SerializedName("title") var title: String, //foo
		@SerializedName("body") var body: String, //bar
		@SerializedName("userId") var userId: Int //1
)