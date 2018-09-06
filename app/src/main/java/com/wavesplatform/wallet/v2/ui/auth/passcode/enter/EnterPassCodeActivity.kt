package com.wavesplatform.wallet.v2.ui.auth.passcode.enter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.AppCompatEditText
import android.text.InputType
import android.text.TextUtils
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v1.data.access.AccessState
import com.wavesplatform.wallet.v1.data.auth.WavesWallet
import com.wavesplatform.wallet.v1.ui.customviews.ToastCustom
import com.wavesplatform.wallet.v1.util.RootUtil
import com.wavesplatform.wallet.v1.util.ViewUtils
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.ui.auth.fingerprint.FingerprintAuthDialogFragment
import com.wavesplatform.wallet.v2.ui.auth.new_account.NewAccountActivity
import com.wavesplatform.wallet.v2.ui.auth.passcode.create.CreatePassCodeActivity
import com.wavesplatform.wallet.v2.ui.auth.passcode.enter.use_account_password.UseAccountPasswordActivity
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.ui.custom.PassCodeEntryKeypad
import com.wavesplatform.wallet.v2.ui.splash.SplashActivity
import com.wavesplatform.wallet.v2.util.launchActivity
import kotlinx.android.synthetic.main.activity_enter_passcode.*
import pers.victor.ext.click
import pers.victor.ext.toast
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
        setupToolbar(toolbar_view, View.OnClickListener { onBackPressed() }, true,
                icon = R.drawable.ic_toolbar_back_black)

        text_use_acc_password.click {
            val guid = getGuid()
            if (TextUtils.isEmpty(guid)) {
                restartApp(this)
            } else {
                launchActivity<UseAccountPasswordActivity> {
                    putExtra(KEY_INTENT_GUID, guid)
                }
            }
        }

        val isShowFingerprint = intent.hasExtra(KEY_INTENT_SHOW_FINGERPRINT)
        val isLoggedIn = !TextUtils.isEmpty(AccessState.getInstance().currentGuid)
        val useFingerprint = (!RootUtil.isDeviceRooted()
                && FingerprintAuthDialogFragment.isAvailable(this)
                && ((isLoggedIn && AccessState.getInstance().isUseFingerPrint)
                || isShowFingerprint))

        pass_keypad.isFingerprintAvailable(useFingerprint)

        pass_keypad.attachDots(pdl_dots)
        pass_keypad.setPadClickedListener(
                object : PassCodeEntryKeypad.OnPinEntryPadClickedListener {
                    override fun onPassCodeEntered(passCode: String) {
                        validate(passCode)
                    }

                    override fun onFingerprintClicked() {
                        if (AccessState.getInstance().isUseFingerPrint) {
                            showFingerPrint()
                        }
                    }
                })

        if (useFingerprint) {
            fingerprintDialog = FingerprintAuthDialogFragment.newInstance()
            fingerprintDialog.setFingerPrintDialogListener(
                    object : FingerprintAuthDialogFragment.FingerPrintDialogListener {
                        override fun onSuccessRecognizedFingerprint(passCode: String) {
                            validate(passCode)
                        }
                    })
            showFingerPrint()
        }
    }

    private fun getGuid(): String {
        return if (intent.hasExtra(KEY_INTENT_GUID)) {
            intent.extras.getString(KEY_INTENT_GUID, "")
        } else if (!TextUtils.isEmpty(AccessState.getInstance().currentGuid)) {
            AccessState.getInstance().currentGuid
        } else {
            ""
        }
    }

    fun validate(passCode: String) {
        showProgressBar(true)
        presenter.validate(this, getGuid(), passCode)
    }

    override fun onSuccessValidatePassCode(password: String, passCode: String) {
        AccessState.getInstance().removePinFails()
        showProgressBar(false)
        val data = Intent()
        data.putExtra(NewAccountActivity.KEY_INTENT_PASSWORD, password)
        data.putExtra(KEY_INTENT_GUID, getGuid())
        data.putExtra(KEY_INTENT_PASS_CODE, passCode)
        setResult(Constants.RESULT_OK, data)
        AccessState.getInstance().setCurrentAccount(getGuid())
        finish()
    }

    override fun onFailValidatePassCode(overMaxWrongPassCode: Boolean, errorMessage: String?) {
        showProgressBar(false)
        if (overMaxWrongPassCode) {
            ToastCustom.makeText(this@EnterPassCodeActivity,
                    getString(R.string.pin_4_strikes),
                    ToastCustom.LENGTH_SHORT,
                    ToastCustom.TYPE_ERROR)
            showRequestPasswordDialog()
        } else {
            val message = if (TextUtils.isEmpty(errorMessage)) {
                getString(R.string.invalid_pin)
            } else {
                getString(R.string.unexpected_error) + " ($errorMessage)"
            }
            ToastCustom.makeText(this@EnterPassCodeActivity, message,
                    ToastCustom.LENGTH_SHORT, ToastCustom.TYPE_ERROR)
            finish()
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
                    restartApp(this)
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
            restartApp(this)
        } else {
            if (!TextUtils.isEmpty(guid)) {
                try {
                    WavesWallet(AccessState.getInstance()
                            .getWalletData(guid), password)
                    launchActivity<CreatePassCodeActivity> {
                        putExtra(CreatePassCodeActivity.KEY_INTENT_PROCESS_RECREATE_PASS_CODE, true)
                        putExtra(KEY_INTENT_GUID, guid)
                        putExtra(NewAccountActivity.KEY_INTENT_PASSWORD, password)
                    }
                    AccessState.getInstance().removePinFails()
                } catch (e: Exception) {
                    toast(getString(R.string.enter_passcode_error_wrong_password))
                    finish()
                }
            }
        }
    }

    fun restartApp(context: Context) {
        val intent = Intent(context, SplashActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    companion object {
        const val KEY_INTENT_PASS_CODE = "intent_pass_code"
        const val KEY_INTENT_SHOW_FINGERPRINT = "intent_show_fingerprint"
        const val KEY_INTENT_GUID = "intent_guid"
        const val REQUEST_ENTER_PASS_CODE = 555
    }
}
