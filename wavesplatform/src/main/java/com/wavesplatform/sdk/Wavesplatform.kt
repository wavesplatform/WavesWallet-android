/**
 *
 * ██╗    ██╗ █████╗ ██╗   ██╗███████╗███████╗
 * ██║    ██║██╔══██╗██║   ██║██╔════╝██╔════╝
 * ██║ █╗ ██║███████║██║   ██║█████╗  ███████╗
 * ██║███╗██║██╔══██║╚██╗ ██╔╝██╔══╝  ╚════██║
 * ╚███╔███╔╝██║  ██║ ╚████╔╝ ███████╗███████║
 * ╚══╝╚══╝ ╚═╝  ╚═╝  ╚═══╝  ╚══════╝╚══════╝
 *
 * ██████╗ ██╗      █████╗ ████████╗███████╗ ██████╗ ██████╗ ███╗   ███╗
 * ██╔══██╗██║     ██╔══██╗╚══██╔══╝██╔════╝██╔═══██╗██╔══██╗████╗ ████║
 * ██████╔╝██║     ███████║   ██║   █████╗  ██║   ██║██████╔╝██╔████╔██║
 * ██╔═══╝ ██║     ██╔══██║   ██║   ██╔══╝  ██║   ██║██╔══██╗██║╚██╔╝██║
 * ██║     ███████╗██║  ██║   ██║   ██║     ╚██████╔╝██║  ██║██║ ╚═╝ ██║
 * ╚═╝     ╚══════╝╚═╝  ╚═╝   ╚═╝   ╚═╝      ╚═════╝ ╚═╝  ╚═╝╚═╝     ╚═╝
 *
 */

package com.wavesplatform.sdk

import android.app.Application
import com.wavesplatform.sdk.crypto.*
import com.wavesplatform.sdk.net.CallAdapterFactory
import com.wavesplatform.sdk.net.DataManager
import com.wavesplatform.sdk.utils.Servers
import pers.victor.ext.currentTimeMillis
import retrofit2.CallAdapter

class Wavesplatform private constructor(var context: Application, factory: CallAdapter.Factory?) {

    private val crypto = WavesCryptoImpl()
    private var net: DataManager = DataManager(context, factory)
    private var netCode: Byte = 'W'.toByte()
    private var timeCorrection = 0L

    companion object {

        private var instance: Wavesplatform? = null

        /**
         * Initialisation Wavesplatform method must be call first.
         * @param application Application context ot the app
         * @param mainNet Optional parameter. Default true. Define net to use.
         * Default true means use MainNet. False - TestNet
         * @param factory Optional parameter. Add a call adapter factory
         * for supporting service method return types other than Call
         */
        @JvmStatic
        fun init(application: Application, mainNet: Boolean = true, factory: CallAdapterFactory? = null) {
            instance = Wavesplatform(application, factory)
        }

        /**
         * Initialisation Wavesplatform method must be call first.
         * @param application Application context ot the app
         */
        @JvmStatic
        fun init(application: Application) {
            init(application, true, null)
        }

        @JvmStatic
        @Throws(NullPointerException::class)
        private fun get(): Wavesplatform {
            if (instance == null) {
                throw NullPointerException("Wavesplatform must be init first!")
            }
            return instance!!
        }

        /**
         * Access to crypto-methods
         */
        @JvmStatic
        fun crypto(): WavesCrypto {
            return get().crypto
        }

        /**
         * Access to net
         */
        @JvmStatic
        fun net(): DataManager {
            return get().net
        }

        fun getNetCode(): Byte {
            return Wavesplatform.get().netCode
        }

        fun getTime(): Long {
            return currentTimeMillis + Wavesplatform.get().timeCorrection
        }

        fun setTimeCorrection(timeCorrection: Long) {
            Wavesplatform.get().timeCorrection = timeCorrection
        }

        fun setServers(servers: Servers) {
            Wavesplatform.net().servers = servers
            Wavesplatform.get().netCode = servers.netCode
            Wavesplatform.net().createServices()
        }
    }
}