package com.wavesplatform.wallet.v2.ui.auth.passcode.enter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.AppCompatEditText
import android.text.InputType
import android.text.TextUtils
import android.util.Log
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.mtramin.rxfingerprint.RxFingerprint
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v1.data.access.AccessState
import com.wavesplatform.wallet.v1.data.auth.IncorrectPinException
import com.wavesplatform.wallet.v1.data.auth.WavesWallet
import com.wavesplatform.wallet.v1.ui.customviews.ToastCustom
import com.wavesplatform.wallet.v1.util.RootUtil
import com.wavesplatform.wallet.v1.util.ViewUtils
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.ui.auth.fingerprint.FingerprintAuthDialogFragment
import com.wavesplatform.wallet.v2.ui.auth.new_account.NewAccountActivity
import com.wavesplatform.wallet.v2.ui.auth.passcode.create.CreatePasscodeActivity
import com.wavesplatform.wallet.v2.ui.auth.passcode.enter.use_account_password.UseAccountPasswordActivity
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.ui.custom.PassCodeEntryKeypad
import com.wavesplatform.wallet.v2.ui.splash.SplashActivity
import com.wavesplatform.wallet.v2.util.launchActivity
import kotlinx.android.synthetic.main.activity_enter_passcode.*
import pers.victor.ext.click
import pers.victor.ext.toast
import javax.inject.Inject


class EnterPasscodeActivity : BaseActivity(), EnterPasscodeView{
    @Inject
    @InjectPresenter
    lateinit var presenter: EnterPasscodePresenter
    private lateinit var fingerprintDialog: FingerprintAuthDialogFragment

    @ProvidePresenter
    fun providePresenter(): EnterPasscodePresenter = presenter

    override fun configLayoutRes() = R.layout.activity_enter_passcode


    companion object {
        const val MAX_AVAILABLE_TIMES = 5
        const val KEY_PASS_CODE = "pass_code"
        const val KEY_SHOW_FINGERPRINT = "show_fingerprint"
        const val KEY_GUID = "guid"
        const val REQUEST_ENTER_PASS_CODE = 555
    }

    override fun onViewReady(savedInstanceState: Bundle?) {
        setupToolbar(toolbar_view, View.OnClickListener { onBackPressed() }, true,
                icon = R.drawable.ic_toolbar_back_black)

        text_use_acc_password.click {
            val guid = getGuid()
            if (TextUtils.isEmpty(guid)) {
                restartApp(this)
            } else {
                launchActivity<UseAccountPasswordActivity> {
                    putExtra(KEY_GUID, guid)
                }
            }
        }

        val isShowFingerprint = intent.hasExtra(KEY_SHOW_FINGERPRINT)
        val isLoggedIn = !TextUtils.isEmpty(AccessState.getInstance().currentGuid)
        val useFingerprint = (!RootUtil.isDeviceRooted()
                && RxFingerprint.isAvailable(this)
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

    private fun getGuid(): String? {
        return if (intent.hasExtra(KEY_GUID)) {
            intent.extras.getString(KEY_GUID)
        } else if (!TextUtils.isEmpty(AccessState.getInstance().currentGuid)) {
            AccessState.getInstance().currentGuid
        } else {
            ""
        }
    }

    fun validate(passCode: String) {
        showProgressBar(true)
        val guid = getGuid()
        AccessState.getInstance().validatePin(guid, passCode).subscribe({ password ->
            AccessState.getInstance().removePinFails()
            showProgressBar(false)
            val data = Intent()
            data.putExtra(NewAccountActivity.KEY_INTENT_PASSWORD, password)
            data.putExtra(KEY_PASS_CODE, passCode)
            setResult(Constants.RESULT_OK, data)
            finish()
        }, { err ->
            if (err !is IncorrectPinException) {
                Log.e(javaClass.simpleName, "Failed to validate pin", err)
            } else {
                AccessState.getInstance().incrementPinFails()
                checkPinFails()
            }
            showProgressBar(false)
        })
    }

    private fun showFingerPrint() {
        fingerprintDialog.isCancelable = false;
        fingerprintDialog.show(fragmentManager, "fingerprintDialog")
    }

    private fun checkPinFails() {
        val fails = AccessState.getInstance().pinFails
        if (fails >= MAX_AVAILABLE_TIMES) {
            ToastCustom.makeText(this@EnterPasscodeActivity,
                    getString(R.string.pin_4_strikes),
                    ToastCustom.LENGTH_SHORT,
                    ToastCustom.TYPE_ERROR)
            showRequestPasswordDialog()
        }
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

    private fun tryLaunchRecreatePassCode(passwordStr: String) {
        val guid = getGuid()

        if (TextUtils.isEmpty(guid)) {
            restartApp(this)
        } else {
            if (!TextUtils.isEmpty(guid)) {
                try {
                    WavesWallet(AccessState.getInstance()
                            .getWalletData(guid), passwordStr)
                    launchActivity<CreatePasscodeActivity>(clear = true) {
                        putExtra(CreatePasscodeActivity.KEY_RECREATE_PASS_CODE, true)
                        putExtra(KEY_GUID, guid)
                        putExtra(NewAccountActivity.KEY_INTENT_PASSWORD, passwordStr)
                    }
                    AccessState.getInstance().removePinFails()
                } catch (e: Exception) {
                    toast(getString(R.string.enter_passcode_error_wrong_password))
                }
            }
        }
    }

    fun restartApp(context: Context) {
        val intent = Intent(context, SplashActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}
