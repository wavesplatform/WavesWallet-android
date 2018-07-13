package com.wavesplatform.wallet.v2.ui.passcode.enter

import android.os.Bundle
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.ui.custom.PassCodeEntryKeypad
import com.wavesplatform.wallet.v2.ui.home.MainActivity
import com.wavesplatform.wallet.v2.util.launchActivity
import javax.inject.Inject
import com.wavesplatform.wallet.v2.ui.fingerprint.FingerprintAuthenticationDialogFragment
import com.wavesplatform.wallet.v2.ui.passcode.enter.use_account_password.UseAccountPasswordActivity
import com.wei.android.lib.fingerprintidentify.FingerprintIdentify
import com.wei.android.lib.fingerprintidentify.base.BaseFingerprint
import kotlinx.android.synthetic.main.activity_enter_passcode.*
import pers.victor.ext.click
import pyxis.uzuki.live.richutilskt.utils.runDelayed


class EnterPasscodeActivity : BaseActivity(), EnterPasscodeView, BaseFingerprint.FingerprintIdentifyListener {
    @Inject
    @InjectPresenter
    lateinit var presenter: EnterPasscodePresenter
    private lateinit var mFingerprintIdentify: FingerprintIdentify
    private lateinit var mFingerprintDialog: FingerprintAuthenticationDialogFragment

    @ProvidePresenter
    fun providePresenter(): EnterPasscodePresenter = presenter

    override fun configLayoutRes() = R.layout.activity_enter_passcode


    companion object {
        var MAX_AVAILABLE_TIMES = 5
    }

    override fun onViewReady(savedInstanceState: Bundle?) {
        setupToolbar(toolbar_view, View.OnClickListener { onBackPressed() }, true, icon = R.drawable.ic_toolbar_back_black)

        mFingerprintIdentify = FingerprintIdentify(this)
        mFingerprintDialog = FingerprintAuthenticationDialogFragment()

        text_use_acc_password.click {
            launchActivity<UseAccountPasswordActivity> {  }
        }

        pass_keypad.isFingerprintAvailable(mFingerprintIdentify.isFingerprintEnable)
        pass_keypad.attachDots(pdl_dots)
        pass_keypad.setPadClickedListener(
                object : PassCodeEntryKeypad.OnPinEntryPadClickedListener {
                    override fun onPassCodeEntered(passCode: String) {
                        launchActivity<MainActivity>(clear = true) { }
                    }

                    override fun onFingerprintClicked() {
                        if (mFingerprintIdentify.isFingerprintEnable) {
                            mFingerprintDialog.isCancelable = false;
                            mFingerprintDialog.show(fragmentManager, "fingerprintDialog");

                            mFingerprintIdentify.startIdentify(MAX_AVAILABLE_TIMES, this@EnterPasscodeActivity);
                        }
                    }
                })
    }

    override fun onSucceed() {
        mFingerprintDialog.onSuccessRecognizedFingerprint()
        runDelayed(1500, {
            mFingerprintDialog.dismiss()
            launchActivity<MainActivity>(clear = true) { }
        })
    }

    override fun onFailed(isDeviceLocked: Boolean) {
        mFingerprintDialog.onFingerprintLocked()
    }

    override fun onNotMatch(availableTimes: Int) {
        mFingerprintDialog.onFingerprintDoNotMatchTryAgain()
    }

    override fun onStartFailedByDeviceLocked() {
        mFingerprintDialog.onFingerprintLocked();
    }

    override fun onPause() {
        super.onPause()
        mFingerprintIdentify.cancelIdentify()
    }

    override fun onDestroy() {
        super.onDestroy()
        mFingerprintIdentify.cancelIdentify()
    }
}
