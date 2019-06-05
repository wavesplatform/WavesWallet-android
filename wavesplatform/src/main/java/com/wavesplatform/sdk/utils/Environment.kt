package com.wavesplatform.sdk.utils

import com.google.gson.annotations.SerializedName
import pers.victor.ext.currentTimeMillis

class Environment(
        @SerializedName("server") var server: Server,
        @SerializedName("timestampServerDiff") var timestampServerDiff: Long) {

    @SerializedName("nodeUrl")
    var nodeUrl: String = ""
    @SerializedName("dataUrl")
    var dataUrl: String = ""
    @SerializedName("matcherUrl")
    var matcherUrl: String = ""
    @SerializedName("scheme")
    var scheme: Byte = 'W'.toByte()

    init {
        when (server) {
            Server.MainNet -> {
                this.scheme = 'W'.toByte()
                this.dataUrl = WavesConstants.URL_DATA
                this.nodeUrl = WavesConstants.URL_NODE
                this.matcherUrl = WavesConstants.URL_MATCHER
            }
            Server.TestNet -> {
                this.scheme = 'T'.toByte()
                this.dataUrl = WavesConstants.URL_DATA_TEST
                this.nodeUrl = WavesConstants.URL_NODE_TEST
                this.matcherUrl = WavesConstants.URL_MATCHER_TEST
            }
            is Server.Custom -> {
                val serverCustom = server as Server.Custom
                this.scheme = serverCustom.scheme
                this.dataUrl = serverCustom.data
                this.nodeUrl = serverCustom.node
                this.matcherUrl = serverCustom.matcher
            }
        }
    }

    fun getTime(): Long {
        return currentTimeMillis + timestampServerDiff
    }

    companion object {
        val DEFAULT = Environment(server = Server.MainNet, timestampServerDiff = 0L)
    }

    sealed class Server {
        object MainNet : Server()
        object TestNet : Server()
        class Custom(val node: String, val matcher: String, val data: String, val scheme: Byte)
            : Server()
    }
}