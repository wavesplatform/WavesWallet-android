package com.wavesplatform.wallet.v2.ui.auth.fingerprint

import android.os.Bundle
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.auth.passcode.create.CreatePassCodeActivity
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.ui.home.MainActivity
import com.wavesplatform.wallet.v2.util.launchActivity
import kotlinx.android.synthetic.main.activity_use_fingerprint.*
import pers.victor.ext.click
import javax.inject.Inject


class UseFingerprintActivity : BaseActivity(), UseFingerprintView {

    @Inject
    @InjectPresenter
    lateinit var presenter: UseFingerprintPresenter

    @ProvidePresenter
    fun providePresenter(): UseFingerprintPresenter = presenter

    override fun configLayoutRes() = R.layout.activity_use_fingerprint

    override fun onViewReady(savedInstanceState: Bundle?) {
        button_use_fingerprint.click { _ ->
            val passCode = intent.extras.getString(CreatePassCodeActivity.KEY_INTENT_PASS_CODE)
            val guid = intent.extras.getString(CreatePassCodeActivity.KEY_INTENT_GUID)

            val fingerprintDialog = FingerprintAuthDialogFragment.newInstance(guid, passCode)
            fingerprintDialog.isCancelable = false
            fingerprintDialog.show(supportFragmentManager, "fingerprintDialog")
            fingerprintDialog.setFingerPrintDialogListener(
                    object : FingerprintAuthDialogFragment.FingerPrintDialogListener {
                        override fun onSuccessRecognizedFingerprint() {
                            App.getAccessManager().setUseFingerPrint(true)
                            launchActivity<MainActivity>(clear = true)
                        }
                    })
        }

        button_do_it_later.click {
            onBackPressed()
        }
    }

    override fun onBackPressed() {
        App.getAccessManager().setUseFingerPrint(false)
        launchActivity<MainActivity>(clear = true)
    }
}
