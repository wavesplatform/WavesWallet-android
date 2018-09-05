package com.wavesplatform.wallet.v2.ui.auth.passcode.create

import android.os.Bundle
import android.util.Log
import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.wallet.v1.data.access.AccessState
import com.wavesplatform.wallet.v2.data.manager.AccessManager
import com.wavesplatform.wallet.v2.ui.auth.new_account.NewAccountActivity
import com.wavesplatform.wallet.v2.ui.auth.passcode.enter.EnterPasscodeActivity
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import javax.inject.Inject

@InjectViewState
class CreatePasscodePresenter @Inject constructor() : BasePresenter<CreatePasscodeView>() {

    var passCode: String = ""
    lateinit var step: CreatePasscodeActivity.CreatePassCodeStep

    fun saveAccount(passCode: String, extras: Bundle) {

        val password = extras.getString(NewAccountActivity.KEY_INTENT_PASSWORD)
        val guid = when {
            extras.containsKey(CreatePasscodeActivity.KEY_INTENT_PROCESS_CHANGE_PASS_CODE) ->
                AccessState.getInstance().currentGuid
            extras.containsKey(CreatePasscodeActivity.KEY_INTENT_PROCESS_RECREATE_PASS_CODE) ->
                extras.getString(EnterPasscodeActivity.KEY_INTENT_GUID)
            else -> {
                val accountName = extras.getString(NewAccountActivity.KEY_INTENT_ACCOUNT_NAME)
                val seed = extras.getString(NewAccountActivity.KEY_INTENT_SEED)
                val skipBackup = extras.getBoolean(NewAccountActivity.KEY_INTENT_SKIP_BACKUP)
                AccessState.getInstance().storeWavesWallet(seed, password, accountName, skipBackup)
            }
        }
        createPassCode(guid, password, passCode)
    }

    private fun createPassCode(guid: String, password: String, passCode: String) {
        AccessManager.instance.createPassCodeObservable(guid, password, passCode)
                .subscribe({
                    viewState.onSuccessCreatePassCodeFailed(passCode)
                }, { throwable ->
                    Log.e("CreatePassCodeActivity", throwable.message)
                    AccessState.getInstance().deleteCurrentWavesWallet()
                    viewState.onFailCreatePassCode()
                })
    }
}
