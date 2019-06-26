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
import com.wavesplatform.sdk.net.WavesService
import com.wavesplatform.sdk.utils.Environment

/**
 * WavesSDK is library for easy and simple co-working Waves blockchain platform and app based on Rx.
 *
 * Library contains 3 parts:
 *
 * Waves Crypto – collection of functions to work with Waves basic types
 * and crypto primitives used by Waves.
 *
 * Waves Models – data transfer objects collection of transactions and other models
 * for work with Waves net services.
 * The models release signification with private key
 *
 * Waves Services – net-services for sending transactions and getting data from blockchain
 * and other Waves services
 */
class WavesSdk {

    internal lateinit var environment: Environment
    internal lateinit var service: WavesService

    companion object {

        private var instance: WavesSdk? = null

        /**
         * Initialisation WavesSdk method must be call first.
         * @param application Application context ot the app
         */
        @JvmStatic
        fun init(application: Application) {
            init(application, Environment.DEFAULT)
        }

        /**
         * Initialisation WavesSdk method must be call first.
         * @param application application-context ot the app
         * @param environment base urls and current time
         */
        @JvmStatic
        fun init(application: Application, environment: Environment) {
            instance = WavesSdk()
            instance!!.environment = environment
            instance!!.service = WavesService(application)
        }

        /**
         * @return Net-service for sending transactions and getting data from blockchain
         * and other Waves services
         */
        @JvmStatic
        fun service(): WavesService {
            return get().service
        }

        /**
         * @return current SDK net-settings
         */
        @JvmStatic
        fun getEnvironment(): Environment {
            return get().environment
        }

        /**
         * Updates current SDK net-settings
         */
        @JvmStatic
        fun setEnvironment(environment: Environment) {
            get().environment = environment
            service().createServices()
        }

        private fun get(): WavesSdk {
            if (instance == null) {
                throw NullPointerException("WavesSdk must be init first!")
            }
            return instance!!
        }
    }
}