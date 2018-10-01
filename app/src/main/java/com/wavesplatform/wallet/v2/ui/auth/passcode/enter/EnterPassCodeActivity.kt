package com.wavesplatform.wallet.v2.ui.auth.passcode.enter

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.AppCompatEditText
import android.text.InputType
import android.text.TextUtils
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v1.data.auth.WavesWallet
import com.wavesplatform.wallet.v1.util.ViewUtils
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.ui.auth.fingerprint.FingerprintAuthDialogFragment
import com.wavesplatform.wallet.v2.ui.auth.new_account.NewAccountActivity
import com.wavesplatform.wallet.v2.ui.auth.passcode.create.CreatePassCodeActivity
import com.wavesplatform.wallet.v2.ui.auth.passcode.enter.use_account_password.UseAccountPasswordActivity
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.ui.custom.PassCodeEntryKeypad
import com.wavesplatform.wallet.v2.ui.welcome.WelcomeActivity
import com.wavesplatform.wallet.v2.util.launchActivity
import com.wavesplatform.wallet.v2.util.showError
import kotlinx.android.synthetic.main.activity_enter_passcode.*
import pers.victor.ext.click
import pers.victor.ext.visiable
import javax.inject.Inject


class EnterPassCodeActivity : BaseActivity(), EnterPasscodeView {
    @Inject
    @InjectPresenter
    lateinit var presenter: EnterPassCodePresenter
    private lateinit var fingerprintDialog: FingerprintAuthDialogFragment

    @ProvidePresenter
    fun providePresenter(): EnterPassCodePresenter = presenter

    override fun configLayoutRes() = R.layout.activity_enter_passcode

    override fun onViewReady(savedInstanceState: Bundle?) {
        text_use_acc_password.click {
            val guid = getGuid()
            if (TextUtils.isEmpty(guid)) {
                restartApp()
            } else {
                launchActivity<UseAccountPasswordActivity> {
                    putExtra(KEY_INTENT_GUID, guid)
                }
            }
        }

        val isProcessSetFingerprint = intent.hasExtra(KEY_INTENT_PROCESS_SET_FINGERPRINT)
        val isAvailable = FingerprintAuthDialogFragment.isAvailable(this)
        val guid = getGuid()

        val isLoggedIn = !TextUtils.isEmpty(guid)
        val useFingerprint = (isAvailable && !isProcessSetFingerprint
                && ((isLoggedIn && App.getAccessManager().isGuidUseFingerPrint(guid))))

        pass_keypad.isFingerprintAvailable(useFingerprint)

        pass_keypad.attachDots(pdl_dots)
        pass_keypad.setPadClickedListener(
                object : PassCodeEntryKeypad.OnPinEntryPadClickedListener {
                    override fun onPassCodeEntered(passCode: String) {
                        validate(passCode)
                    }

                    override fun onFingerprintClicked() {
                        if (App.getAccessManager().isUseFingerPrint()) {
                            showFingerPrint()
                        }
                    }
                })

        if (useFingerprint) {
            fingerprintDialog = FingerprintAuthDialogFragment.newInstance(getGuid())
            fingerprintDialog.setFingerPrintDialogListener(
                    object : FingerprintAuthDialogFragment.FingerPrintDialogListener {
                        override fun onSuccessRecognizedFingerprint(passCode: String) {
                            validate(passCode)
                        }
                    })
            showFingerPrint()
        }

        if (TextUtils.isEmpty(guid)) {
            finish()
        } else {
            if (intent.hasExtra(KEY_INTENT_GUID)) {
                text_title.text = App.getAccessManager().getWalletName(guid)
                text_subtitle.text = App.getAccessManager().getWalletAddress(guid)
                text_subtitle.visiable()
                logout.visiable()
                logout.click {
                    App.getAccessManager().setLastLoggedInGuid("")
                    App.getAccessManager().resetWallet()
                    launchActivity<WelcomeActivity>()
                }
            } else {
                setupToolbar(toolbar_view, true,
                        icon = R.drawable.ic_toolbar_back_black)
            }
        }
    }

    private fun getGuid(): String {
        return if (intent.hasExtra(KEY_INTENT_GUID)) {
            intent.extras.getString(KEY_INTENT_GUID, "")
        } else if (!TextUtils.isEmpty(App.getAccessManager().getLastLoggedInGuid())) {
            App.getAccessManager().getLastLoggedInGuid()
        } else {
            ""
        }
    }

    fun validate(passCode: String) {
        showProgressBar(true)
        presenter.validate(getGuid(), passCode)
    }

    override fun onSuccessValidatePassCode(password: String, passCode: String) {
        App.getAccessManager().resetPassCodeInputFails()
        showProgressBar(false)

        val data = Intent()
        data.putExtra(NewAccountActivity.KEY_INTENT_PASSWORD, password)
        data.putExtra(KEY_INTENT_GUID, getGuid())
        data.putExtra(KEY_INTENT_PASS_CODE, passCode)
        setResult(Constants.RESULT_OK, data)
        App.getAccessManager().setWallet(getGuid(), password)
        finish()
    }

    override fun onFailValidatePassCode(overMaxWrongPassCode: Boolean, errorMessage: String?) {
        showProgressBar(false)
        if (overMaxWrongPassCode) {
            showError(R.string.pin_4_strikes, R.id.content)
            pass_keypad.passCodesNotMatches()
            showRequestPasswordDialog()
        } else {
            if (TextUtils.isEmpty(errorMessage)) {
                pass_keypad.passCodesNotMatches()
            } else {
                pass_keypad.passCodesNotMatches()
                showError(getString(R.string.unexpected_error) + " ($errorMessage)", R.id.content)
            }
        }
    }

    private fun showFingerPrint() {
        fingerprintDialog.isCancelable = false
        fingerprintDialog.show(fragmentManager, "fingerprintDialog")
    }

    override fun onBackPressed() {
        setResult(Constants.RESULT_CANCELED)
        finish()
    }

    override fun askPassCode() = false

    private fun showRequestPasswordDialog() {
        val password = AppCompatEditText(this)
        password.inputType = InputType.TYPE_CLASS_TEXT or
                InputType.TYPE_TEXT_VARIATION_PASSWORD or
                InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
        password.hint = getString(R.string.password_entry)

        AlertDialog.Builder(this, R.style.AlertDialogStyle)
                .setTitle(R.string.app_name)
                .setMessage(getString(R.string.pin_4_strikes))
                .setView(ViewUtils.getAlertDialogEditTextLayout(this, password))
                .setCancelable(false)
                .setNegativeButton(android.R.string.cancel) { _, _ ->
                    restartApp()
                }
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    val passwordStr = password.text.toString()
                    if (passwordStr.isEmpty()) {
                        password.error = getString(R.string.invalid_password_too_short)
                    } else {
                        tryLaunchRecreatePassCode(passwordStr)
                    }
                }.show()
    }

    private fun tryLaunchRecreatePassCode(password: String) {
        val guid = getGuid()

        if (TextUtils.isEmpty(guid)) {
            restartApp()
        } else {
            if (!TextUtils.isEmpty(guid)) {
                try {
                    WavesWallet(App.getAccessManager().getWalletData(guid), password)
                    launchActivity<CreatePassCodeActivity> {
                        putExtra(CreatePassCodeActivity.KEY_INTENT_PROCESS_CHANGE_PASS_CODE, true)
                        putExtra(KEY_INTENT_GUID, guid)
                        putExtra(NewAccountActivity.KEY_INTENT_PASSWORD, password)
                    }
                    App.getAccessManager().resetPassCodeInputFails()
                } catch (e: Exception) {
                    showError(R.string.enter_passcode_error_wrong_password, R.id.content)
                }
            }
        }
    }

    companion object {
        const val KEY_INTENT_PASS_CODE = "intent_pass_code"
        const val KEY_INTENT_GUID = "intent_guid"
        const val KEY_INTENT_PROCESS_SET_FINGERPRINT = "intent_process_set_fingerprint"
        const val REQUEST_ENTER_PASS_CODE = 555
    }
}
