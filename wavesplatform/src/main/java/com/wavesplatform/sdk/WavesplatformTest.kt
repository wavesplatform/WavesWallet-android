package com.wavesplatform.sdk

import android.app.Application
import android.util.Log
import com.wavesplatform.sdk.utils.RxUtil

class WavesplatformTest {

    companion object {

        @JvmStatic
        fun testLoadAliases(app: Application) {
            Wavesplatform.init(app)
            Wavesplatform.get().createWallet(
                    "",
                    "11111111",
                    "")

            Wavesplatform.get().loader.apiService
                    .aliases(Wavesplatform.get().getWallet().address)
                    .compose(RxUtil.applyObservableDefaultSchedulers())
                    .subscribe({ aliases ->
                        Log.d("Wavesplatform", "Success")
                    }, { error ->
                        Log.d("Wavesplatform", "Error")
                    })
        }
    }
}