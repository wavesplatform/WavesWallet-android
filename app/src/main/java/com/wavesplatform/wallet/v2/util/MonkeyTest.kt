package com.wavesplatform.wallet.v2.util

import android.text.TextUtils
import android.util.Log
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.BuildConfig
import com.wavesplatform.wallet.v2.data.local.PreferencesHelper
import java.io.File


class MonkeyTest {

    companion object {

        @JvmStatic
        fun isTurnedOn(): Boolean {
            return !TextUtils.isEmpty(BuildConfig.MONKEY_TEST_SEED)
        }

        @JvmStatic
        fun startIfNeed() {
            if (isTurnedOn()) {
                //clearApplicationData()
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

        private fun clearApplicationData() {
            val cache = App.getAppContext().cacheDir
            val appDir = File(cache.parent)
            if (appDir.exists()) {
                val children = appDir.list()
                for (s in children) {
                    if (s != "lib") {
                        deleteDir(File(appDir, s))
                        Log.i("EEEEEERRRRRROOOOOOORRRR", "**************** File /data/data/APP_PACKAGE/$s DELETED *******************")
                    }
                }
            }
        }

        private fun deleteDir(dir: File?): Boolean {
            if (dir != null && dir.isDirectory) {
                val children = dir.list()
                var i = 0
                while (i < children.size) {
                    val success = deleteDir(File(dir, children[i]))
                    if (!success) {
                        return false
                    }
                    i++
                }
            }

            assert(dir != null)
            return dir!!.delete()
        }
    }
}