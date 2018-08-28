package com.wavesplatform.wallet.v2.ui.auth.passcode.create

import android.os.Bundle
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v1.data.access.AccessState
import com.wavesplatform.wallet.v2.ui.auth.fingerprint.UseFingerprintActivity
import com.wavesplatform.wallet.v2.ui.auth.new_account.NewAccountActivity
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.ui.custom.PassCodeEntryKeypad
import com.wavesplatform.wallet.v2.ui.home.MainActivity
import com.wavesplatform.wallet.v2.util.launchActivity
import com.wei.android.lib.fingerprintidentify.FingerprintIdentify
import kotlinx.android.synthetic.main.activity_create_passcode.*
import javax.inject.Inject


class CreatePasscodeActivity : BaseActivity(), CreatePasscodeView {

    @Inject
    @InjectPresenter
    lateinit var presenter: CreatePasscodePresenter
    lateinit var fingerprintIdentify: FingerprintIdentify

    @ProvidePresenter
    fun providePresenter(): CreatePasscodePresenter = presenter

    override fun configLayoutRes() = R.layout.activity_create_passcode


    override fun onViewReady(savedInstanceState: Bundle?) {
        setupToolbar(toolbar_view, View.OnClickListener { onBackPressed() }, false, icon = R.drawable.ic_toolbar_back_black)

        presenter.step = CreatePassCodeStep.CREATE

        fingerprintIdentify = FingerprintIdentify(this)

        pass_keypad.attachDots(pdl_dots)
        pass_keypad.setPadClickedListener(object : PassCodeEntryKeypad.OnPinEntryPadClickedListener {
            override fun onPassCodeEntered(passCode: String) {
                if (presenter.step == CreatePassCodeStep.CREATE) {
                    presenter.passCode = passCode
                    moveToVerifyStep()
                } else if (presenter.step == CreatePassCodeStep.VERIFY) {
                    if (presenter.passCode == passCode) {
                        if (fingerprintIdentify.isFingerprintEnable) {
                            launchActivity<UseFingerprintActivity> { }
                        } else {
                            if (intent.extras != null) {
                                AccessState.getInstance().storeWavesWallet(
                                        intent.extras!!.getString(NewAccountActivity.KEY_INTENT_SEED),
                                        intent.extras!!.getString(NewAccountActivity.KEY_INTENT_PASSWORD),
                                        intent.extras!!.getString(NewAccountActivity.KEY_INTENT_ACCOUNT))
                            }
                            launchActivity<MainActivity>(clear = true) { }
                        }
                    } else {
                        pass_keypad.passCodesNotMatches()
                    }
                }
            }
        })
    }

    private fun moveToCreateStep() {
        text_title.setText(R.string.create_passcode_create_title)
        pass_keypad.clearPasscode()
        presenter.step = CreatePassCodeStep.CREATE
        supportActionBar?.setHomeButtonEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        toolbar.setNavigationOnClickListener(null)
    }

    private fun moveToVerifyStep() {
        text_title.setText(R.string.create_passcode_verify_title)
        pass_keypad.clearPasscode()
        presenter.step = CreatePassCodeStep.VERIFY
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener({
            moveToCreateStep()
        })
    }

    enum class CreatePassCodeStep(step: Int) {
        CREATE(0),
        VERIFY(1)
    }

    override fun onBackPressed() {
        if (presenter.step == CreatePassCodeStep.VERIFY) {
            moveToCreateStep()
        }
    }
}
