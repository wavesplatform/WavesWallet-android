package com.wavesplatform.wallet.v2.ui.auth.passcode.enter.use_account_password

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.inputmethod.EditorInfo
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v1.data.auth.WavesWallet
import com.wavesplatform.wallet.v2.data.rules.EqualsAccountPasswordRule
import com.wavesplatform.wallet.v2.ui.auth.new_account.NewAccountActivity
import com.wavesplatform.wallet.v2.ui.auth.passcode.create.CreatePassCodeActivity
import com.wavesplatform.wallet.v2.ui.auth.passcode.enter.EnterPassCodeActivity
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.ui.custom.Identicon
import com.wavesplatform.wallet.v2.ui.home.MainActivity
import com.wavesplatform.wallet.v2.util.launchActivity
import com.wavesplatform.wallet.v2.util.showError
import io.github.anderscheow.validator.Validation
import io.github.anderscheow.validator.Validator
import io.github.anderscheow.validator.constant.Mode
import io.github.anderscheow.validator.rules.common.NotEmptyRule
import kotlinx.android.synthetic.main.activity_use_account_password.*
import pers.victor.ext.addTextChangedListener
import pers.victor.ext.click
import javax.inject.Inject

class UseAccountPasswordActivity : BaseActivity(), UseAccountPasswordView {

    @Inject
    @InjectPresenter
    lateinit var presenter: UseAccountPasswordPresenter
    private lateinit var validator: Validator
    private var guid: String = ""

    @ProvidePresenter
    fun providePresenter(): UseAccountPasswordPresenter = presenter

    override fun configLayoutRes() = R.layout.activity_use_account_password

    override fun askPassCode() = false

    override fun onCreate(savedInstanceState: Bundle?) {
        translucentStatusBar = true
        overridePendingTransition(R.anim.slide_in_right, R.anim.null_animation)
        super.onCreate(savedInstanceState)
    }

    override fun onViewReady(savedInstanceState: Bundle?) {
        setStatusBarColor(R.color.white)
        setNavigationBarColor(R.color.white)
        setupToolbar(toolbar_view, true,
                icon = R.drawable.ic_toolbar_back_black)
        if (intent.hasExtra(EnterPassCodeActivity.KEY_INTENT_GUID)) {
            guid = intent.extras.getString(EnterPassCodeActivity.KEY_INTENT_GUID)
            if (!TextUtils.isEmpty(guid)) {
                setAccountData(guid)
            }
        }

        validator = Validator.with(applicationContext).setMode(Mode.CONTINUOUS)

        val accountPasswordValidation = Validation(til_account_password)
                .and(NotEmptyRule(" "))
                .and(EqualsAccountPasswordRule(R.string.change_password_validation_old_password_wrong_error, guid))

        edit_account_password.addTextChangedListener {
            on { s, start, before, count ->
                validator
                        .validate(object : Validator.OnValidateListener {
                            override fun onValidateSuccess(values: List<String>) {
                                button_sign_in.isEnabled = true
                            }

                            override fun onValidateFailed() {
                                button_sign_in.isEnabled = false
                            }
                        }, accountPasswordValidation)
            }
        }

        edit_account_password.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE && button_sign_in.isEnabled) {
                goNext()
                true
            } else {
                false
            }
        }

        button_sign_in.click {
            goNext()
        }
    }

    private fun goNext() {
        if (!TextUtils.isEmpty(guid)) {
            try {
                WavesWallet(App.getAccessManager().getWalletData(guid),
                        edit_account_password.text.toString().trim())
                launchActivity<CreatePassCodeActivity>(options = createDataBundle(),
                        requestCode = REQUEST_CREATE_PASS_CODE)
            } catch (e: Exception) {
                showError(R.string.enter_passcode_error_wrong_password, R.id.content)
            }
        }
    }

    private fun createDataBundle(): Bundle {
        val options = Bundle()
        options.putBoolean(CreatePassCodeActivity.KEY_INTENT_PROCESS_CHANGE_PASS_CODE, true)
        options.putString(EnterPassCodeActivity.KEY_INTENT_GUID, guid)
        options.putString(NewAccountActivity.KEY_INTENT_PASSWORD,
                edit_account_password.text.toString().trim())
        return options
    }

    private fun setAccountData(guid: String) {
        account_name.text = App.getAccessManager().getWalletName(guid)
        val address = App.getAccessManager().getWalletAddress(guid)
        account_address.text = address
        Glide.with(applicationContext)
                .load(Identicon().create(address))
                .apply(RequestOptions().circleCrop())
                .into(image_asset)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CREATE_PASS_CODE) {
            if (resultCode == RESULT_OK) {
                launchActivity<MainActivity>(clear = true)
            }
        }
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_CANCELED, Intent().apply {
            putExtra(EnterPassCodeActivity.KEY_AFTER_OVER_ATTEMPTS, intent.extras.getBoolean(EnterPassCodeActivity.KEY_AFTER_OVER_ATTEMPTS))
        })
        finish()
        overridePendingTransition(R.anim.null_animation, R.anim.slide_out_right)
    }

    companion object {
        const val REQUEST_CREATE_PASS_CODE = 1000
    }
}
