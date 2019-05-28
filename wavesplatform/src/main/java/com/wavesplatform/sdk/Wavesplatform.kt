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
import com.wavesplatform.sdk.crypto.WavesCrypto
import com.wavesplatform.sdk.crypto.WavesCryptoImpl
import com.wavesplatform.sdk.net.DataManager
import com.wavesplatform.sdk.utils.Servers
import pers.victor.ext.currentTimeMillis

class Wavesplatform private constructor(var context: Application) {

    private val crypto = WavesCryptoImpl()
    private var service: DataManager = DataManager(context)
    private var netCode: Byte = 'W'.toByte()
    private var timeCorrection = 0L

    companion object {

        private var instance: Wavesplatform? = null

        /**
         * Initialisation Wavesplatform method must be call first.
         * @param application Application context ot the app
         */
        @JvmStatic
        fun init(application: Application) {
            instance = Wavesplatform(application)
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
        fun service(): DataManager {
            return get().service
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
            Wavesplatform.service().servers = servers
            Wavesplatform.get().netCode = servers.netCode
            Wavesplatform.service().createServices()
        }
    }
}