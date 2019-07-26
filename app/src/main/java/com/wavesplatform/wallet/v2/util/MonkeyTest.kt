package com.wavesplatform.wallet.v2.util

import android.text.TextUtils
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.BuildConfig
import com.wavesplatform.wallet.v2.data.local.PreferencesHelper
import com.wavesplatform.wallet.v2.ui.home.MainActivity


class MonkeyTest {

    companion object {

        fun isTurnedOn(): Boolean {
            return !TextUtils.isEmpty(BuildConfig.MONKEY_TEST_SEED)
        }

        fun startIfNeed() {
            if (isTurnedOn()) {
                val guid = getGuid()
                if (TextUtils.isEmpty(guid)) {
                    App.getAccessManager().storeWalletData(
                            BuildConfig.MONKEY_TEST_SEED,
                            "11111111",
                            "monkey0",
                            true)
                    App.getAccessManager().setLastLoggedInGuid(App.getAccessManager().getLoggedInGuid())
                    PreferencesHelper(App.getAppContext()).setTutorialPassed(true)
                    App.getAccessManager().setCurrentAccountBackupDone()
                } else {
                    App.getAccessManager().setWallet(guid, "11111111")
                }
                App.getAppContext().launchActivity<MainActivity>(clear = true)
            }
        }

        private fun getGuid(): String {
            val lastGuid = App.getAccessManager().getLastLoggedInGuid()
            return if (TextUtils.isEmpty(lastGuid)) {
                lastGuid
            } else {
                val guids = PrefsUtil(App.getAppContext()).getGlobalValueList(
                        EnvironmentManager.name + PrefsUtil.LIST_WALLET_GUIDS)
                if (guids.isNotEmpty()) {
                    guids[0]
                } else {
                    ""
                }
            }
        }
    }
}