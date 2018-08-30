package com.wavesplatform.wallet.v2.ui.auth.fingerprint

import android.os.Bundle
import android.util.Log
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v1.data.access.AccessState
import com.wavesplatform.wallet.v1.ui.customviews.ToastCustom
import com.wavesplatform.wallet.v2.ui.auth.new_account.NewAccountActivity
import com.wavesplatform.wallet.v2.ui.auth.passcode.create.CreatePasscodeActivity
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.ui.home.MainActivity
import com.wavesplatform.wallet.v2.util.launchActivity
import kotlinx.android.synthetic.main.activity_use_fingerprint.*
import pers.victor.ext.click
import javax.inject.Inject


class UseFingerprintActivity : BaseActivity(), UseFingerprintView {

    @Inject
    @InjectPresenter
    lateinit var presenter: UseFingerprintPresenter

    @ProvidePresenter
    fun providePresenter(): UseFingerprintPresenter = presenter

    override fun configLayoutRes() = R.layout.activity_use_fingerprint


    override fun onViewReady(savedInstanceState: Bundle?) {
        button_use_fingerprint.click{
            launchActivity<MainActivity>(clear = true) {  }
        }

        button_do_it_later.click{
            // todo copy - should refactoring
            val skipBackup = intent.extras!!.getBoolean(
                    NewAccountActivity.KEY_INTENT_SKIP_BACKUP)

            val passCode = intent.extras!!.getString(
                    CreatePasscodeActivity.KEY_PASS_CODE)
            val password = intent.extras!!.getString(
                    NewAccountActivity.KEY_INTENT_PASSWORD)
            val walletGuid = AccessState.getInstance().storeWavesWallet(
                    intent.extras!!.getString(NewAccountActivity.KEY_INTENT_SEED),
                    password,
                    intent.extras!!.getString(NewAccountActivity.KEY_INTENT_ACCOUNT),
                    skipBackup)

            AccessState.getInstance().createPin(walletGuid, password, passCode)
                    .subscribe({
                        showProgressBar(false)
                        /*mDataListener.dismissProgressDialog()
                        mFingerprintHelper.clearEncryptedData(PrefsUtil.KEY_ENCRYPTED_PIN_CODE)
                        mFingerprintHelper.setFingerprintUnlockEnabled(false)
                        mPrefsUtil.removeValue(PrefsUtil.KEY_PIN_FAILS)
                        if (mValidatingPinForResult) {
                            mDataListener.finishWithResultOk(mPassword.toString())
                        } else {
                            mAppUtil.restartAppWithVerifiedPin()
                        }*/
                        launchActivity<MainActivity>(clear = true) { }
                    }, { throwable ->
                        /*
                        mAppUtil.restartApp()*/
                        showProgressBar(false)
                        ToastCustom.makeText(this@UseFingerprintActivity,
                                getString(R.string.create_pin_failed),
                                ToastCustom.LENGTH_SHORT,
                                ToastCustom.TYPE_ERROR)
                        AccessState.getInstance().deleteCurrentWavesWallet()
                        finish()
                        Log.e("CreatePasscodeActivity_TAG", throwable.message)
                    })
        }
    }

    override fun onBackPressed() {

    }

}
