package com.wavesplatform.wallet.v2.util

import android.text.TextUtils
import android.util.Log
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.BuildConfig
import com.wavesplatform.wallet.v2.data.local.PreferencesHelper
import java.io.File


class MonkeyTest {

    companion object {

        fun isTurnedOn(): Boolean {
            return !TextUtils.isEmpty(BuildConfig.MONKEY_TEST_SEED)
        }

        fun startIfNeed() {
            if (isTurnedOn()) {
                val guid = App.getAccessManager().getLoggedInGuid()
                if (TextUtils.isEmpty(guid)) {
                    App.getAccessManager().storeWalletData(
                            BuildConfig.MONKEY_TEST_SEED,
                            "11111111",
                            "monkey0",
                            true)
                    App.getAccessManager().setLastLoggedInGuid(App.getAccessManager().getLoggedInGuid())
                    PreferencesHelper(App.getAppContext()).setTutorialPassed(true)
                } else {
                    App.getAccessManager().setWallet(guid, "11111111")
                }
            }
        }
    }
}