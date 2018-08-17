package com.wavesplatform.wallet.v2.data.helpers

import android.app.Activity
import com.wavesplatform.wallet.v1.db.DBHelper
import com.wavesplatform.wallet.v2.data.manager.NodeDataManager
import io.realm.Realm
import io.realm.RealmConfiguration
import javax.inject.Inject

class AuthHelper @Inject constructor(private val publicKeyAccountHelper: PublicKeyAccountHelper, val nodeDataManager: NodeDataManager) {

    fun startMainActivityAndCreateNewDBIfKeyValid(parent: Activity, publicKey: String) {
        if (publicKeyAccountHelper.isPublicKeyAccountAvailable(publicKey)) {

            nodeDataManager.generatePublicKeyFrom(publicKey)

            val config = RealmConfiguration.Builder()
                    .name(String.format("%s.realm", publicKey))
                    .deleteRealmIfMigrationNeeded()
                    .build()

            DBHelper.getInstance().setRealmConfig(config)
            Realm.getInstance(config).isAutoRefresh = false


            // TODO: uncomment
//            val intent = Intent(parent, MainActivity::class.java)
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
//            parent.startActivity(intent)
        }
    }
}
