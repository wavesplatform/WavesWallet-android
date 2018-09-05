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
import com.wavesplatform.wallet.v2.ui.auth.passcode.create.CreatePasscodeActivity
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
        if (intent.hasExtra(EnterPasscodeActivity.KEY_GUID)) {
            guid = intent.extras.getString(EnterPasscodeActivity.KEY_GUID)
            if (!TextUtils.isEmpty(guid)) {
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
                    val passwordStr = edit_account_password.text.toString()
                    WavesWallet(AccessState.getInstance().getWalletData(guid), passwordStr)
                    launchActivity<CreatePasscodeActivity>(clear = true) {
                        putExtra(CreatePasscodeActivity.KEY_INTENT_PROCESS_RECREATE_PASS_CODE, true)
                        putExtra(EnterPasscodeActivity.KEY_GUID, guid)
                        putExtra(NewAccountActivity.KEY_INTENT_PASSWORD, passwordStr)
                    }
                    AccessState.getInstance().removePinFails()
                } catch (e: Exception) {
                    toast(getString(R.string.enter_passcode_error_wrong_password))
                }
            }
        }
    }
}
