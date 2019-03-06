package com.wavesplatform.wallet.v2.ui.auth.passcode.create

import android.app.Dialog
import android.os.Bundle
import android.widget.Toast
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.auth.fingerprint.FingerprintAuthDialogFragment
import com.wavesplatform.wallet.v2.ui.auth.fingerprint.UseFingerprintActivity
import com.wavesplatform.wallet.v2.ui.auth.new_account.NewAccountActivity
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.ui.custom.PassCodeEntryKeypad
import com.wavesplatform.wallet.v2.ui.home.MainActivity
import com.wavesplatform.wallet.v2.util.launchActivity
import com.wavesplatform.wallet.v2.util.showError
import kotlinx.android.synthetic.main.activity_create_passcode.*
import javax.inject.Inject

open class CreatePassCodeActivity : BaseActivity(), CreatePasscodeView {

    @Inject
    @InjectPresenter
    lateinit var presenter: CreatePassCodePresenter

    @ProvidePresenter
    fun providePresenter(): CreatePassCodePresenter = presenter

    override fun configLayoutRes() = R.layout.activity_create_passcode

    override fun askPassCode() = false

    override fun onCreate(savedInstanceState: Bundle?) {
        overridePendingTransition(R.anim.slide_in_right, R.anim.null_animation)
        super.onCreate(savedInstanceState)
    }

    override fun onViewReady(savedInstanceState: Bundle?) {
        setupToolbar(toolbar_view, false,
                icon = R.drawable.ic_toolbar_back_black)

        presenter.step = CreatePassCodeStep.CREATE

        pass_keypad.attachDots(pdl_dots)
        pass_keypad.setPadClickedListener(object : PassCodeEntryKeypad.OnPinEntryPadClickedListener {
            override fun onPassCodeEntered(passCode: String) {
                if (presenter.step == CreatePassCodeStep.CREATE) {
                    val oldPassCode = intent.getStringExtra(KEY_INTENT_PASS_CODE)
                    if (oldPassCode.isNullOrEmpty()) {
                        presenter.passCode = passCode
                        moveToVerifyStep()
                    } else {
                        if (oldPassCode == passCode) {
                            pass_keypad.passCodesNotMatches()
                            showError(R.string.create_passcode_validation_already_use_error, R.id.content)
                        } else {
                            presenter.passCode = passCode
                            moveToVerifyStep()
                        }
                    }
                } else if (presenter.step == CreatePassCodeStep.VERIFY) {
                    if (presenter.passCode == passCode) {
                        saveAccount(passCode)
                    } else {
                        pass_keypad.passCodesNotMatches()
                    }
                }
            }
        })
        moveToCreateStep()
    }

    private fun saveAccount(passCode: String) {
        if (intent.extras == null) {
            Toast.makeText(this, R.string.create_pin_failed, Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        showProgressBar(true)
        presenter.saveAccount(passCode, intent.extras)
    }

    override fun onSuccessCreatePassCode(guid: String, passCode: String) {
        showProgressBar(false)
        if ((intent.hasExtra(NewAccountActivity.KEY_INTENT_PROCESS_ACCOUNT_CREATION) ||
                        intent.hasExtra(NewAccountActivity.KEY_INTENT_PROCESS_ACCOUNT_IMPORT)) &&
                FingerprintAuthDialogFragment.isAvailable(this)) {
            launchActivity<UseFingerprintActivity>(intent.extras) {
                putExtra(CreatePassCodeActivity.KEY_INTENT_GUID, guid)
                putExtra(CreatePassCodeActivity.KEY_INTENT_PASS_CODE, passCode)
            }
        } else if (App.getAccessManager().isUseFingerPrint(guid)) {
            val fingerprintDialog = FingerprintAuthDialogFragment.newInstance(guid, passCode)
            fingerprintDialog.isCancelable = false
            fingerprintDialog.show(supportFragmentManager, "fingerprintDialog")
            fingerprintDialog.setFingerPrintDialogListener(
                    object : FingerprintAuthDialogFragment.FingerPrintDialogListener {
                        override fun onSuccessRecognizedFingerprint() {
                            launchActivity<MainActivity>(clear = true)
                        }

                        override fun onCancelButtonClicked(dialog: Dialog) {
                            App.getAccessManager().setUseFingerPrint(guid, false)
                            launchActivity<MainActivity>(clear = true)
                        }

                        override fun onFingerprintLocked(message: String) {
                            launchActivity<MainActivity>(clear = true)
                        }

                        override fun onShowErrorMessage(message: String) {
                            Toast.makeText(this@CreatePassCodeActivity, message,
                                    Toast.LENGTH_SHORT).show()
                            launchActivity<MainActivity>(clear = true)
                        }
                    })
        } else if (intent.hasExtra(KEY_INTENT_GUID)) {
            setResult(RESULT_OK)
            finish()
        } else {
            launchActivity<MainActivity>(clear = true)
        }
    }

    override fun onFailCreatePassCode() {
        showProgressBar(false)
        showError(R.string.create_pin_failed, R.id.content)
    }

    private fun moveToCreateStep() {
        if (intent.hasExtra(NewAccountActivity.KEY_INTENT_PROCESS_ACCOUNT_CREATION)) {
            text_title.setText(R.string.create_passcode_create_title)
        } else {
            text_title.setText(R.string.create_passcode_create_new_title)
        }
        pass_keypad.clearPassCode()
        presenter.step = CreatePassCodeStep.CREATE
        supportActionBar?.setHomeButtonEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        toolbar.setNavigationOnClickListener(null)
    }

    private fun moveToVerifyStep() {
        text_title.setText(R.string.create_passcode_verify_title)
        pass_keypad.clearPassCode()
        presenter.step = CreatePassCodeStep.VERIFY
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { moveToCreateStep() }
    }

    override fun needToShowNetworkMessage(): Boolean = true

    override fun onNetworkConnectionChanged(networkConnected: Boolean) {
        super.onNetworkConnectionChanged(networkConnected)
        pass_keypad.setEnable(networkConnected)
    }

    override fun onBackPressed() {
        if (presenter.step == CreatePassCodeStep.VERIFY) {
            moveToCreateStep()
        }
    }

    enum class CreatePassCodeStep {
        CREATE,
        VERIFY
    }

    companion object {
        const val KEY_INTENT_PASS_CODE = "intent_pass_code"
        const val KEY_INTENT_GUID = "intent_guid"
        const val KEY_INTENT_PROCESS_CHANGE_PASS_CODE = "intent_process_change_pass_code"
    }
}