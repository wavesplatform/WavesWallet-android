package com.wavesplatform.sdk.net

interface OnErrorListener {
    fun onError(exception: NetworkException)
}