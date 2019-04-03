/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.auth.passcode.create

import android.os.Bundle
import android.util.Log
import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.v2.ui.auth.new_account.NewAccountActivity
import com.wavesplatform.wallet.v2.ui.auth.passcode.enter.EnterPassCodeActivity
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import javax.inject.Inject

@InjectViewState
class CreatePassCodePresenter @Inject constructor() : BasePresenter<CreatePasscodeView>() {

    var passCode: String = ""
    lateinit var step: CreatePassCodeActivity.CreatePassCodeStep

    fun saveAccount(passCode: String, extras: Bundle) {
        val password = extras.getString(NewAccountActivity.KEY_INTENT_PASSWORD)
        val guid = when {
            extras.containsKey(CreatePassCodeActivity.KEY_INTENT_PROCESS_CHANGE_PASS_CODE) -> {
                val guid = extras.getString(EnterPassCodeActivity.KEY_INTENT_GUID)
                App.getAccessManager().setWallet(guid, password)
                guid
            }
            else -> {
                val accountName = extras.getString(NewAccountActivity.KEY_INTENT_ACCOUNT_NAME)
                val seed = extras.getString(NewAccountActivity.KEY_INTENT_SEED)
                val skipBackup = extras.getBoolean(NewAccountActivity.KEY_INTENT_SKIP_BACKUP)
                val guid = App.getAccessManager()
                        .storeWalletData(seed, password, accountName, skipBackup)
                if (extras.containsKey(NewAccountActivity.KEY_INTENT_PROCESS_ACCOUNT_IMPORT)) {
                    App.getAccessManager().setCurrentAccountBackupDone()
                }
                guid
            }
        }
        writePassCodeToRemoteDb(guid, password, passCode)
    }

    private fun writePassCodeToRemoteDb(guid: String, password: String, passCode: String) {
        addSubscription(App.getAccessManager()
                .writePassCodeObservable(guid, password, passCode)
                .subscribe({
                    App.getAccessManager().resetPassCodeInputFails(guid)
                    viewState.onSuccessCreatePassCode(guid, passCode)
                }, { throwable ->
                    Log.e("CreatePassCodeActivity", throwable.message)
                    App.getAccessManager().deleteCurrentWavesWallet()
                    viewState.onFailCreatePassCode()
                }))
    }
}
