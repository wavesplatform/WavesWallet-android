package com.wavesplatform.wallet.v2.data.helpers

import android.app.Activity
import android.content.Intent
import com.wavesplatform.wallet.v1.api.datafeed.DataFeedManager
import com.wavesplatform.wallet.v1.api.mather.MatherManager
import com.wavesplatform.wallet.v1.db.DBHelper
import com.wavesplatform.wallet.v2.ui.home.MainActivity
import io.realm.RealmConfiguration
import javax.inject.Inject

class AuthHelper @Inject constructor(private val publicKeyAccountHelper: PublicKeyAccountHelper) {

    fun startMainActivityAndCreateNewDBIfKeyValid(parent: Activity, publicKey: String) {
        if (publicKeyAccountHelper.isPublicKeyAccountAvailable(publicKey)) {

//            TODO Need to change the logic  -------->
//            DataFeedManager.createInstance()
//            MatherManager.createInstance(publicKey)

//            <----------------------------------

            val config = RealmConfiguration.Builder()
                    .name(String.format("%s.realm", publicKey))
                    .deleteRealmIfMigrationNeeded()
                    .build()

            DBHelper.getInstance().setRealmConfig(config)

            val intent = Intent(parent, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            parent.startActivity(intent)
        }
    }
}
