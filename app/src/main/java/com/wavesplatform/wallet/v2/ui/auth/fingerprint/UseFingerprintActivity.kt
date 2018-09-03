package com.wavesplatform.wallet.v2.ui.auth.fingerprint

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v1.data.access.AccessState
import com.wavesplatform.wallet.v1.ui.customviews.ToastCustom
import com.wavesplatform.wallet.v2.ui.auth.passcode.create.CreatePasscodeActivity
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
            val passCode = intent.extras.getString(CreatePasscodeActivity.KEY_PASS_CODE)
            val mFingerprintDialog = FingerprintAuthDialogFragment.newInstance(passCode)

            mFingerprintDialog.isCancelable = false
            mFingerprintDialog.show(fragmentManager, "fingerprintDialog")
            mFingerprintDialog.setFingerPrintDialogListener(
                    object : FingerprintAuthDialogFragment.FingerPrintDialogListener {
                        override fun onSuccessRecognizedFingerprint() {
                            AccessState.getInstance().isUseFingerPrint = true
                            launchActivity<MainActivity>(clear = true) { }
                        }
                    })
        }

        button_do_it_later.click {
            AccessState.getInstance().isUseFingerPrint = false
            launchActivity<MainActivity>(clear = true) { }
        }
    }

    override fun onStop() {
        super.onStop()
        fingerprintDisposable.dispose()
    }

    override fun onBackPressed() {

    }
}
