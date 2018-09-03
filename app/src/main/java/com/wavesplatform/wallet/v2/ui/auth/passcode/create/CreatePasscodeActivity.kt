package com.wavesplatform.wallet.v2.ui.auth.passcode.create

import android.os.Bundle
import android.util.Log
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.mtramin.rxfingerprint.RxFingerprint
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v1.data.access.AccessState
import com.wavesplatform.wallet.v1.ui.customviews.ToastCustom
import com.wavesplatform.wallet.v2.ui.auth.fingerprint.UseFingerprintActivity
import com.wavesplatform.wallet.v2.ui.auth.new_account.NewAccountActivity
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.ui.custom.PassCodeEntryKeypad
import com.wavesplatform.wallet.v2.ui.home.MainActivity
import com.wavesplatform.wallet.v2.util.launchActivity
import com.wei.android.lib.fingerprintidentify.FingerprintIdentify
import kotlinx.android.synthetic.main.activity_create_passcode.*
import javax.inject.Inject


open class CreatePasscodeActivity : BaseActivity(), CreatePasscodeView {

    companion object {
        const val KEY_PASS_CODE = "pass_code"
        const val KEY_CHANGE_PASS_CODE = "change_pass_code"
    }

    @Inject
    @InjectPresenter
    lateinit var presenter: CreatePasscodePresenter

    @ProvidePresenter
    fun providePresenter(): CreatePasscodePresenter = presenter

    override fun configLayoutRes() = R.layout.activity_create_passcode


    override fun onViewReady(savedInstanceState: Bundle?) {
        setupToolbar(toolbar_view, View.OnClickListener { onBackPressed() }, false, icon = R.drawable.ic_toolbar_back_black)

        presenter.step = CreatePassCodeStep.CREATE

        pass_keypad.attachDots(pdl_dots)
        pass_keypad.setPadClickedListener(object : PassCodeEntryKeypad.OnPinEntryPadClickedListener {
            override fun onPassCodeEntered(passCode: String) {
                if (presenter.step == CreatePassCodeStep.CREATE) {
                    presenter.passCode = passCode
                    moveToVerifyStep()
                } else if (presenter.step == CreatePassCodeStep.VERIFY) {
                    if (presenter.passCode == passCode) {
                        trySaveAccount(passCode)
                    } else {
                        pass_keypad.passCodesNotMatches()
                    }
                }
            }
        })
    }

    fun trySaveAccount(passCode: String) {
        if (intent.extras == null) {
            ToastCustom.makeText(this@CreatePasscodeActivity,
                    getString(R.string.create_pin_failed),
                    ToastCustom.LENGTH_SHORT,
                    ToastCustom.TYPE_ERROR)
            return
        }

        showProgressBar(true)

        val walletGuid: String
        val password = intent.extras!!.getString(
                NewAccountActivity.KEY_INTENT_PASSWORD)
        walletGuid = if (intent.hasExtra(CreatePasscodeActivity.KEY_CHANGE_PASS_CODE)) {
            AccessState.getInstance().currentGuid
        } else {
            val skipBackup = intent.extras!!.getBoolean(
                    NewAccountActivity.KEY_INTENT_SKIP_BACKUP)
            AccessState.getInstance().storeWavesWallet(
                    intent.extras!!.getString(NewAccountActivity.KEY_INTENT_SEED),
                    password,
                    intent.extras!!.getString(NewAccountActivity.KEY_INTENT_ACCOUNT),
                    skipBackup)
        }

        AccessState.getInstance().createPin(walletGuid, password, passCode)
                .subscribe( {
                    showProgressBar(false)
                    if (RxFingerprint.isAvailable(this)) {
                        launchActivity<UseFingerprintActivity>(intent.extras) {
                            putExtra(KEY_PASS_CODE, passCode)
                        }
                    } else {
                        launchActivity<MainActivity>(clear = true)
                    }
                }, { throwable ->
                    // AppUtil.restartApp()
                    showProgressBar(false)
                    ToastCustom.makeText(this@CreatePasscodeActivity,
                            getString(R.string.create_pin_failed),
                            ToastCustom.LENGTH_SHORT,
                            ToastCustom.TYPE_ERROR)
                    AccessState.getInstance().deleteCurrentWavesWallet()
                    finish()
                    Log.e("CreatePasscodeActivity_TAG", throwable.message)
                })
    }

    private fun moveToCreateStep() {
        text_title.setText(R.string.create_passcode_create_title)
        pass_keypad.clearPasscode()
        presenter.step = CreatePassCodeStep.CREATE
        supportActionBar?.setHomeButtonEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        toolbar.setNavigationOnClickListener(null)
    }

    private fun moveToVerifyStep() {
        text_title.setText(R.string.create_passcode_verify_title)
        pass_keypad.clearPasscode()
        presenter.step = CreatePassCodeStep.VERIFY
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { moveToCreateStep() }
    }

    enum class CreatePassCodeStep(step: Int) {
        CREATE(0),
        VERIFY(1)
    }

    override fun onBackPressed() {
        if (presenter.step == CreatePassCodeStep.VERIFY) {
            moveToCreateStep()
        }
    }
}
