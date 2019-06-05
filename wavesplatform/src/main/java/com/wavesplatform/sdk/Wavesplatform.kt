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
import com.wavesplatform.sdk.net.WavesService
import com.wavesplatform.sdk.utils.Environment

class Wavesplatform {

    private lateinit var environment: Environment
    private lateinit var crypto: WavesCrypto
    private lateinit var service: WavesService

    companion object {

        private var instance: Wavesplatform? = null

        /**
         * Initialisation Wavesplatform method must be call first.
         * @param application Application context ot the app
         */
        @JvmStatic
        fun init(application: Application) {
            init(application, Environment.DEFAULT)
        }

        @JvmStatic
        fun init(application: Application, environment: Environment) {
            instance = Wavesplatform()
            instance!!.environment = environment
            instance!!.service = WavesService(application)
            instance!!.crypto = WavesCrypto.Companion
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
        fun service(): WavesService {
            return get().service
        }

        @JvmStatic
        fun getEnvironment(): Environment {
            return get().environment
        }

        @JvmStatic
        fun setEnvironment(environment: Environment) {
            get().environment = environment
            service().createServices()
        }

        private fun get(): Wavesplatform {
            if (instance == null) {
                throw NullPointerException("Wavesplatform must be init first!")
            }
            return instance!!
        }
    }
}