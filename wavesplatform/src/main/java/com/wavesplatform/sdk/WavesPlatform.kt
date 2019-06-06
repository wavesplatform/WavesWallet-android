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

class WavesPlatform {

    internal lateinit var environment: Environment
    internal lateinit var service: WavesService

    companion object {

        private var instance: WavesPlatform? = null

        /**
         * Initialisation WavesPlatform method must be call first.
         * @param application Application context ot the app
         */
        @JvmStatic
        fun init(application: Application) {
            init(application, Environment.DEFAULT)
        }

        @JvmStatic
        fun init(application: Application, environment: Environment) {
            instance = WavesPlatform()
            instance!!.environment = environment
            instance!!.service = WavesService(application)
        }

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

        private fun get(): WavesPlatform {
            if (instance == null) {
                throw NullPointerException("WavesPlatform must be init first!")
            }
            return instance!!
        }
    }
}