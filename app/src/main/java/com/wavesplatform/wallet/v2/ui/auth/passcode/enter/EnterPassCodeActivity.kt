package com.wavesplatform.wallet.v2.ui.auth.passcode.enter

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.text.TextUtils
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v1.util.PrefsUtil
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.ui.auth.fingerprint.FingerprintAuthDialogFragment
import com.wavesplatform.wallet.v2.ui.auth.new_account.NewAccountActivity
import com.wavesplatform.wallet.v2.ui.auth.passcode.enter.use_account_password.UseAccountPasswordActivity
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.ui.custom.PassCodeEntryKeypad
import com.wavesplatform.wallet.v2.util.*
import de.adorsys.android.securestoragelibrary.SecurePreferences
import kotlinx.android.synthetic.main.activity_enter_passcode.*
import pers.victor.ext.click
import pers.victor.ext.inflate
import pers.victor.ext.visiable
import javax.inject.Inject

class EnterPassCodeActivity : BaseActivity(), EnterPasscodeView {
    @Inject
    @InjectPresenter
    lateinit var presenter: EnterPassCodePresenter
    private lateinit var fingerprintDialog: FingerprintAuthDialogFragment
    private lateinit var guid: String
    private var useFingerprint: Boolean = false

    @ProvidePresenter
    fun providePresenter(): EnterPassCodePresenter = presenter

    override fun configLayoutRes() = R.layout.activity_enter_passcode

    override fun askPassCode() = false

    override fun onCreate(savedInstanceState: Bundle?) {
        overridePendingTransition(R.anim.null_animation, R.anim.null_animation)
        super.onCreate(savedInstanceState)
    }

    override fun onViewReady(savedInstanceState: Bundle?) {
        setStatusBarColor(R.color.white)
        setNavigationBarColor(R.color.white)

        val isProcessSetFingerprint = intent.hasExtra(KEY_INTENT_PROCESS_SET_FINGERPRINT)
        val isAvailable = FingerprintAuthDialogFragment.isAvailable(this)
        guid = getGuid()

        useFingerprint = isAvailable &&
                !isProcessSetFingerprint &&
                !TextUtils.isEmpty(guid) &&
                App.getAccessManager().isGuidUseFingerPrint(guid)

        if (EnterPassCodePresenter.overMaxWrongPassCodes(guid)) {
            startUsePasswordScreen(true)
        }

        text_use_acc_password.click {
            startUsePasswordScreen(false)
        }

        pass_keypad.isFingerprintAvailable(useFingerprint)

        pass_keypad.attachDots(pdl_dots)
        pass_keypad.setPadClickedListener(
                object : PassCodeEntryKeypad.OnPinEntryPadClickedListener {
                    override fun onPassCodeEntered(passCode: String) {
                        validate(passCode)
                    }

                    override fun onFingerprintClicked() {
                        if (App.getAccessManager().isUseFingerPrint(guid)) {
                            showFingerPrint()
                        }
                    }
                })

        if (useFingerprint) {
            if (!SecurePreferences.contains(guid + EnterPassCodeActivity.KEY_INTENT_PASS_CODE)) {
                pass_keypad.isFingerprintAvailable(false)
            } else {
                fingerprintDialog = FingerprintAuthDialogFragment.newInstance(getGuid())
                fingerprintDialog.setFingerPrintDialogListener(
                        object : FingerprintAuthDialogFragment.FingerPrintDialogListener {
                            override fun onSuccessRecognizedFingerprint(passCode: String) {
                                validate(passCode)
                            }

                            override fun onFingerprintLocked(message: String) {
                                pass_keypad.isFingerprintAvailable(false)
                            }

                            override fun onShowErrorMessage(message: String) {
                                showError(message, R.id.content)
                                fingerprintDialog.dismiss()
                            }

                            override fun onShowMessage(message: String) {
                                showMessage(message, R.id.content)
                                fingerprintDialog.dismiss()
                            }
                        })
                showFingerPrint()
            }
        }

        if (TextUtils.isEmpty(guid)) {
            exitFromScreen()
        } else {
            if (intent.hasExtra(KEY_INTENT_GUID)) {
                text_title.text = App.getAccessManager().getWalletName(guid)
                text_subtitle.text = App.getAccessManager().getWalletAddress(guid)
                text_subtitle.visiable()
                logout.visiable()
                logout.click {
                    clearAndLogout()
                }
            } else {
                setupToolbar(toolbar_view, true,
                        icon = R.drawable.ic_toolbar_back_black)
            }
        }
    }

    private fun startUsePasswordScreen(afterOverAttempts: Boolean) {
        val guid = getGuid()
        if (TextUtils.isEmpty(guid)) {
            restartApp()
        } else {
            launchActivity<UseAccountPasswordActivity>(REQUEST_USE_PASSWORD_CODE) {
                putExtra(KEY_INTENT_GUID, guid)
                putExtra(KEY_AFTER_OVER_ATTEMPTS, afterOverAttempts)
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

    private fun validate(passCode: String) {
        if (isNetworkConnection()) {
            showProgressBar(true)
            presenter.validate(getGuid(), passCode)
        } else {
            pass_keypad.passCodesNotMatches()
        }
    }

    override fun onSuccessValidatePassCode(password: String, passCode: String) {
        if (useFingerprint && !SecurePreferences.contains(guid + EnterPassCodeActivity.KEY_INTENT_PASS_CODE)) {
            SecurePreferences.setValue(guid + EnterPassCodeActivity.KEY_INTENT_PASS_CODE, passCode)
            prefsUtil.removeValue(guid, PrefsUtil.KEY_ENCRYPTED_PIN)
        }

        showProgressBar(false)

        val data = Intent()
        data.putExtra(NewAccountActivity.KEY_INTENT_PASSWORD, password)
        data.putExtra(KEY_INTENT_GUID, getGuid())
        data.putExtra(KEY_INTENT_PASS_CODE, passCode)
        setResult(Constants.RESULT_OK, data)
        App.getAccessManager().setWallet(getGuid(), password)
        exitFromScreen()
    }

    override fun onFailValidatePassCode(overMaxWrongPassCode: Boolean, errorMessage: String?) {
        showProgressBar(false)
        if (overMaxWrongPassCode) {
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
        if (!fingerprintDialog.isAdded) {
            fingerprintDialog.show(supportFragmentManager, fingerprintDialog::class.java.simpleName)
        }
    }

    override fun onBackPressed() {
        if (intent.hasExtra(KEY_INTENT_GUID) && !intent.hasExtra(KEY_INTENT_USE_BACK_FOR_EXIT)) {
            exit()
        } else {
            setResult(Constants.RESULT_CANCELED)
            exitFromScreen()
        }
    }

    private fun exitFromScreen() {
        finish()
        overridePendingTransition(R.anim.null_animation, R.anim.slide_out_down)
    }

    private fun showRequestPasswordDialog() {
        val alertDialog = AlertDialog.Builder(this).create()
        alertDialog.setCancelable(false)
        alertDialog.setTitle(getString(R.string.enter_passcode_too_many_attempts_dialog_title))
        alertDialog.setView(getDescriptionView())
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE,
                getString(R.string.enter_passcode_too_many_attempts_dialog_positive_btn_txt)) { dialog, _ ->
            dialog.dismiss()
            startUsePasswordScreen(true)
        }
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.enter_passcode_too_many_attempts_dialog_negative_btn_txt)) { dialog, _ ->
            dialog.dismiss()
            clearAndLogout()
        }
        alertDialog.show()
        alertDialog.makeStyled()
    }

    override fun needToShowNetworkMessage(): Boolean = true

    override fun onNetworkConnectionChanged(networkConnected: Boolean) {
        super.onNetworkConnectionChanged(networkConnected)
        pass_keypad.setEnable(networkConnected)
    }

    private fun getDescriptionView(): View? {
        return inflate(R.layout.layout_many_attepmts)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_USE_PASSWORD_CODE -> {
                if (resultCode == Activity.RESULT_CANCELED) {
                    if (data?.getBooleanExtra(KEY_AFTER_OVER_ATTEMPTS, false) == true) {
                        clearAndLogout()
                    }
                }
            }
        }
    }

    companion object {
        const val KEY_INTENT_PASS_CODE = "intent_pass_code"
        const val KEY_INTENT_GUID = "intent_guid"
        const val KEY_AFTER_OVER_ATTEMPTS = "after_over_attempts"
        const val KEY_INTENT_PROCESS_SET_FINGERPRINT = "intent_process_set_fingerprint"
        const val KEY_INTENT_USE_BACK_FOR_EXIT = "intent_use_back_for_exit"
        const val REQUEST_ENTER_PASS_CODE = 555
        const val REQUEST_USE_PASSWORD_CODE = 777
    }
}