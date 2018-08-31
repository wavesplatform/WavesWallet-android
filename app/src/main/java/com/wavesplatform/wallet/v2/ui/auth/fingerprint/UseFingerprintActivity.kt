package com.wavesplatform.wallet.v2.ui.auth.fingerprint

import android.app.Dialog
import android.os.Bundle
import android.support.v7.widget.AppCompatTextView
import android.util.Log
import android.widget.Toast
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.mtramin.rxfingerprint.EncryptionMethod
import com.mtramin.rxfingerprint.RxFingerprint
import com.mtramin.rxfingerprint.data.FingerprintResult
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v1.data.access.AccessState
import com.wavesplatform.wallet.v1.ui.customviews.ToastCustom
import com.wavesplatform.wallet.v2.ui.auth.choose_account.ChooseAccountActivity
import com.wavesplatform.wallet.v2.ui.auth.new_account.NewAccountActivity
import com.wavesplatform.wallet.v2.ui.auth.passcode.create.CreatePasscodeActivity
import com.wavesplatform.wallet.v2.ui.auth.passcode.enter.EnterPasscodeActivity
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.ui.home.MainActivity
import com.wavesplatform.wallet.v2.util.launchActivity
import kotlinx.android.synthetic.main.activity_use_fingerprint.*
import pers.victor.ext.click
import javax.inject.Inject
import io.reactivex.disposables.Disposables




class UseFingerprintActivity : BaseActivity(), UseFingerprintView {

    @Inject
    @InjectPresenter
    lateinit var presenter: UseFingerprintPresenter

    private var fingerprintDisposable = Disposables.empty()

    @ProvidePresenter
    fun providePresenter(): UseFingerprintPresenter = presenter

    override fun configLayoutRes() = R.layout.activity_use_fingerprint


    override fun onViewReady(savedInstanceState: Bundle?) {
        button_use_fingerprint.click { _ ->
            AccessState.getInstance().isUseFingerPrint = true
            val passCode = intent.extras.getString(CreatePasscodeActivity.KEY_PASS_CODE)

            val mFingerprintDialog = FingerprintAuthenticationDialogFragment
                    .newInstance(FingerprintAuthenticationDialogFragment.AUTH)

            mFingerprintDialog.isCancelable = false
            mFingerprintDialog.show(fragmentManager, "fingerprintDialog")
            mFingerprintDialog.setFingerPrintDialogListener(object : FingerprintAuthenticationDialogFragment.FingerPrintDialogListener {
                override fun onSuccessRecognizedFingerprint() {
                    super.onSuccessRecognizedFingerprint()
                    fingerprintDisposable = RxFingerprint.encrypt(
                            EncryptionMethod.RSA,
                            this@UseFingerprintActivity,
                            EnterPasscodeActivity.KEY_PIN_CODE, passCode)
                            .subscribe({ encryptionResult ->
                                when (encryptionResult?.result) {
                                    FingerprintResult.FAILED -> onError("Fingerprint not recognized, try again!")
                                    FingerprintResult.HELP -> onError(encryptionResult.message)
                                    FingerprintResult.AUTHENTICATED -> {
                                        AccessState.getInstance()
                                                .ecryptedPin = encryptionResult.encrypted
                                        launchActivity<MainActivity>(clear = true)}
                                }
                            }, { onError(it.localizedMessage) })
                }
            })
        }

        button_do_it_later.click {
            launchActivity<MainActivity>(clear = true) { }
        }
    }

    override fun onStop() {
        super.onStop()
        fingerprintDisposable.dispose()
    }

    private fun onError(message: String?) {
        Log.d("ERROR", "Fingerprint Authenticate Error: $message", null)
        ToastCustom.makeText(
                this,
                message,
                Toast.LENGTH_SHORT,
                ToastCustom.TYPE_GENERAL)
    }

    override fun onBackPressed() {

    }
}
