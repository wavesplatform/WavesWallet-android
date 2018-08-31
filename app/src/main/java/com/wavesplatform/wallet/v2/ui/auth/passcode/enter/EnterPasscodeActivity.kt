package com.wavesplatform.wallet.v2.ui.auth.passcode.enter

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.mtramin.rxfingerprint.EncryptionMethod
import com.mtramin.rxfingerprint.RxFingerprint
import com.mtramin.rxfingerprint.data.FingerprintResult
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v1.data.access.AccessState
import com.wavesplatform.wallet.v1.data.auth.IncorrectPinException
import com.wavesplatform.wallet.v1.ui.auth.PinEntryViewModel
import com.wavesplatform.wallet.v1.ui.customviews.ToastCustom
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.ui.auth.fingerprint.FingerprintAuthenticationDialogFragment
import com.wavesplatform.wallet.v2.ui.auth.new_account.NewAccountActivity
import com.wavesplatform.wallet.v2.ui.auth.passcode.enter.use_account_password.UseAccountPasswordActivity
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.ui.custom.PassCodeEntryKeypad
import com.wavesplatform.wallet.v2.ui.home.MainActivity
import com.wavesplatform.wallet.v2.util.launchActivity
import com.wei.android.lib.fingerprintidentify.FingerprintIdentify
import com.wei.android.lib.fingerprintidentify.base.BaseFingerprint
import kotlinx.android.synthetic.main.activity_enter_passcode.*
import pers.victor.ext.click
import pyxis.uzuki.live.richutilskt.utils.runDelayed
import javax.inject.Inject


class EnterPasscodeActivity : BaseActivity(), EnterPasscodeView, BaseFingerprint.FingerprintIdentifyListener {
    @Inject
    @InjectPresenter
    lateinit var presenter: EnterPasscodePresenter
    private lateinit var mFingerprintDialog: FingerprintAuthenticationDialogFragment

    @ProvidePresenter
    fun providePresenter(): EnterPasscodePresenter = presenter

    override fun configLayoutRes() = R.layout.activity_enter_passcode


    companion object {
        const val MAX_AVAILABLE_TIMES = 5
        const val KEY_PIN_CODE = "pin_code"
    }

    override fun onViewReady(savedInstanceState: Bundle?) {
        setupToolbar(toolbar_view, View.OnClickListener { onBackPressed() }, true,
                icon = R.drawable.ic_toolbar_back_black)

        text_use_acc_password.click {
            launchActivity<UseAccountPasswordActivity> {  }
        }

        pass_keypad.isFingerprintAvailable(
                RxFingerprint.isAvailable(this)
                        && AccessState.getInstance().isUseFingerPrint)

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

        mFingerprintDialog = FingerprintAuthenticationDialogFragment
                .newInstance(FingerprintAuthenticationDialogFragment.DECRYPT)
        mFingerprintDialog.setFingerPrintDialogListener(object : FingerprintAuthenticationDialogFragment.FingerPrintDialogListener {
            override fun onSuccessRecognizedFingerprint(decrypted: String) {
                super.onSuccessRecognizedFingerprint(decrypted)
                validate(decrypted)
            }
        })

        if (RxFingerprint.isAvailable(this)
                && AccessState.getInstance().isUseFingerPrint) {
            showFingerPrint()
        }
    }

    fun validate(passCode: String) {
        showProgressBar(true)
        AccessState.getInstance().validatePin(passCode).subscribe({ password ->
            AccessState.getInstance().removePinFails()
            showProgressBar(false)
            val data = Intent()
            data.putExtra(NewAccountActivity.KEY_INTENT_PASSWORD, password)
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
            finish()
        })
    }

    private fun showFingerPrint() {
        mFingerprintDialog.isCancelable = false;
        mFingerprintDialog.show(fragmentManager, "fingerprintDialog")
    }

    private fun checkPinFails() {
        val fails = AccessState.getInstance().pinFails
        if (fails >= PinEntryViewModel.MAX_ATTEMPTS) { // todo check const
            ToastCustom.makeText(this@EnterPasscodeActivity,
                    getString(R.string.pin_4_strikes),
                    ToastCustom.LENGTH_SHORT,
                    ToastCustom.TYPE_ERROR)
            // todo showRequestPasswordDialog()
        }
    }

    override fun onSucceed() {
        mFingerprintDialog.onSuccessRecognizedFingerprint()
        runDelayed(1500) {
            mFingerprintDialog.dismiss()
            setResult(Constants.RESULT_OK)
            finish()
        }
    }

    override fun onBackPressed() {
        setResult(Constants.RESULT_CANCELED)
        finish()
    }

    override fun onFailed(isDeviceLocked: Boolean) {
        if (isDeviceLocked) mFingerprintDialog.onFingerprintLocked()
    }

    override fun onNotMatch(availableTimes: Int) {
        mFingerprintDialog.onFingerprintDoNotMatchTryAgain()
    }

    override fun onStartFailedByDeviceLocked() {
        mFingerprintDialog.onFingerprintLocked()
    }
}
