package com.wavesplatform.wallet.v2.ui.auth.passcode.enter.use_account_password

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v1.data.access.AccessState
import com.wavesplatform.wallet.v1.data.auth.WavesWallet
import com.wavesplatform.wallet.v2.ui.auth.new_account.NewAccountActivity
import com.wavesplatform.wallet.v2.ui.auth.passcode.create.CreatePassCodeActivity
import com.wavesplatform.wallet.v2.ui.auth.passcode.enter.EnterPasscodeActivity
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.ui.custom.Identicon
import com.wavesplatform.wallet.v2.util.launchActivity
import io.github.anderscheow.validator.Validation
import io.github.anderscheow.validator.Validator
import io.github.anderscheow.validator.constant.Mode
import io.github.anderscheow.validator.rules.common.MinRule
import kotlinx.android.synthetic.main.activity_use_account_password.*
import pers.victor.ext.addTextChangedListener
import pers.victor.ext.click
import pers.victor.ext.toast
import javax.inject.Inject


class UseAccountPasswordActivity : BaseActivity(), UseAccountPasswordView {

    @Inject
    @InjectPresenter
    lateinit var presenter: UseAccountPasswordPresenter
    lateinit var validator: Validator
    private var guid: String? = ""

    @ProvidePresenter
    fun providePresenter(): UseAccountPasswordPresenter = presenter

    override fun configLayoutRes() = R.layout.activity_use_account_password


    override fun onViewReady(savedInstanceState: Bundle?) {
        setupToolbar(toolbar_view, View.OnClickListener { onBackPressed() }, true,
                icon = R.drawable.ic_toolbar_back_black)
        if (intent.hasExtra(EnterPasscodeActivity.KEY_INTENT_GUID)) {
            guid = intent.extras.getString(EnterPasscodeActivity.KEY_INTENT_GUID)
            if (!TextUtils.isEmpty(guid)) {
                setAccountData(guid!!)
            }
        }

        validator = Validator.with(applicationContext).setMode(Mode.CONTINUOUS)

        val accountPasswordValidation = Validation(til_account_password)
                .and(MinRule(8, R.string.new_account_create_password_validation_length_error))

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

        button_sign_in.click {
            if (!TextUtils.isEmpty(guid)) {
                try {
                    WavesWallet(AccessState.getInstance().getWalletData(guid),
                            edit_account_password.text.toString())
                    launchActivity<CreatePassCodeActivity>(options = createDataBundle())
                    AccessState.getInstance().removePinFails()
                } catch (e: Exception) {
                    toast(getString(R.string.enter_passcode_error_wrong_password))
                }
            }
        }
    }

    private fun createDataBundle(): Bundle {
        val options = Bundle()
        options.putBoolean(CreatePassCodeActivity.KEY_INTENT_PROCESS_RECREATE_PASS_CODE, true)
        options.putString(EnterPasscodeActivity.KEY_INTENT_GUID, guid)
        options.putString(NewAccountActivity.KEY_INTENT_PASSWORD, edit_account_password.text.toString())
        return options
    }

    private fun setAccountData(guid: String) {
        account_name.text = AccessState.getInstance().getWalletName(guid)
        val address = AccessState.getInstance().getWalletAddress(guid)
        account_address.text = address
        val bitmap = Identicon.create(address,
                Identicon.Options.Builder()
                        .setRandomBlankColor()
                        .create())
        Glide.with(applicationContext)
                .load(bitmap)
                .apply(RequestOptions().circleCrop())
                .into(image_asset)
    }
}
