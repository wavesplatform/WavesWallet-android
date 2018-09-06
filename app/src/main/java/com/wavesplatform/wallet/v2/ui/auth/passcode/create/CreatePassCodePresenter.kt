package com.wavesplatform.wallet.v2.ui.auth.passcode.create

import android.os.Bundle
import android.util.Log
import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.wallet.BlockchainApplication
import com.wavesplatform.wallet.v1.data.access.AccessState
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
            extras.containsKey(CreatePassCodeActivity.KEY_INTENT_PROCESS_CHANGE_PASS_CODE) ->
                BlockchainApplication.getAccessManager().getCurrentGuid()
            extras.containsKey(CreatePassCodeActivity.KEY_INTENT_PROCESS_RECREATE_PASS_CODE) ->
                extras.getString(EnterPassCodeActivity.KEY_INTENT_GUID)
            else -> {
                val accountName = extras.getString(NewAccountActivity.KEY_INTENT_ACCOUNT_NAME)
                val seed = extras.getString(NewAccountActivity.KEY_INTENT_SEED)
                val skipBackup = extras.getBoolean(NewAccountActivity.KEY_INTENT_SKIP_BACKUP)
                BlockchainApplication
                        .getAccessManager()
                        .storeWalletData(seed, password, accountName, skipBackup)
            }
        }
        createPassCode(guid, password, passCode)
    }

    private fun createPassCode(guid: String, password: String, passCode: String) {
        BlockchainApplication
                .getAccessManager()
                .writePassCodeObservable(guid, password, passCode)
                .subscribe({
                    viewState.onSuccessCreatePassCode(passCode)
                }, { throwable ->
                    Log.e("CreatePassCodeActivity", throwable.message)
                    BlockchainApplication.getAccessManager().deleteCurrentWavesWallet()
                    viewState.onFailCreatePassCode()
                })
    }
}
