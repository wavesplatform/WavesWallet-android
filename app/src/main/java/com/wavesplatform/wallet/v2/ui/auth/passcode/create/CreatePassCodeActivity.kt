package com.wavesplatform.wallet.v2.ui.auth.passcode.create

import android.app.Dialog
import android.os.Bundle
import android.support.v7.widget.AppCompatTextView
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v1.ui.customviews.ToastCustom
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

    override fun onViewReady(savedInstanceState: Bundle?) {
        setStatusBarColor(R.color.white)
        setupToolbar(toolbar_view, false,
                icon = R.drawable.ic_toolbar_back_black)

        presenter.step = CreatePassCodeStep.CREATE

        pass_keypad.attachDots(pdl_dots)
        pass_keypad.setPadClickedListener(object : PassCodeEntryKeypad.OnPinEntryPadClickedListener {
            override fun onPassCodeEntered(passCode: String) {
                if (presenter.step == CreatePassCodeStep.CREATE) {
                    presenter.passCode = passCode
                    moveToVerifyStep()
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

    override fun askPassCode() = false

    private fun saveAccount(passCode: String) {
        if (intent.extras == null) {
            ToastCustom.makeText(this@CreatePassCodeActivity,
                    getString(R.string.create_pin_failed),
                    ToastCustom.LENGTH_SHORT,
                    ToastCustom.TYPE_ERROR)
            finish()
            return
        }
        showProgressBar(true)
        presenter.saveAccount(passCode, intent.extras)
    }

    override fun onSuccessCreatePassCode(guid: String, passCode: String) {
        showProgressBar(false)
        App.getAccessManager().setUseFingerPrint(false)
        if (intent.hasExtra(NewAccountActivity.KEY_INTENT_PROCESS_ACCOUNT_CREATION)
                && FingerprintAuthDialogFragment.isAvailable(this)) {
            launchActivity<UseFingerprintActivity>(intent.extras) {
                putExtra(CreatePassCodeActivity.KEY_INTENT_GUID, guid)
                putExtra(CreatePassCodeActivity.KEY_INTENT_PASS_CODE, passCode)
            }
        } else if (App.getAccessManager().isUseFingerPrint()) {
            val fingerprintDialog = FingerprintAuthDialogFragment.newInstance(guid, passCode)
            fingerprintDialog.isCancelable = false
            fingerprintDialog.show(fragmentManager, "fingerprintDialog")
            fingerprintDialog.setFingerPrintDialogListener(
                    object : FingerprintAuthDialogFragment.FingerPrintDialogListener {
                        override fun onSuccessRecognizedFingerprint() {
                            launchActivity<MainActivity>(clear = true)
                        }

                        override fun onCancelButtonClicked(dialog: Dialog, button: AppCompatTextView) {
                            launchActivity<MainActivity>(clear = true)
                        }
                    })
        } else if (intent.hasExtra(KEY_INTENT_GUID)) {
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


    override fun onBackPressed() {
        if (presenter.step == CreatePassCodeStep.VERIFY) {
            moveToCreateStep()
        } else {
            super.onBackPressed()
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
