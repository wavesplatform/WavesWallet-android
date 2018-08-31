package com.wavesplatform.wallet.v2.ui.auth.import_account.manually

import android.os.Bundle
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.auth.import_account.protect_account.ProtectAccountActivity
import com.wavesplatform.wallet.v2.ui.base.view.BaseFragment
import com.wavesplatform.wallet.v2.util.launchActivity
import io.github.anderscheow.validator.Validation
import io.github.anderscheow.validator.Validator
import io.github.anderscheow.validator.constant.Mode
import io.github.anderscheow.validator.rules.common.NotEmptyRule
import kotlinx.android.synthetic.main.fragment_enter_seed_manually.*
import pers.victor.ext.addTextChangedListener
import pers.victor.ext.click
import javax.inject.Inject


class EnterSeedManuallyFragment : BaseFragment(), EnterSeedManuallyView {

    @Inject
    @InjectPresenter
    lateinit var presenter: EnterSeedManuallyPresenter
    lateinit var validator: Validator


    @ProvidePresenter
    fun providePresenter(): EnterSeedManuallyPresenter = presenter

    override fun configLayoutRes() = R.layout.fragment_enter_seed_manually


    override fun onViewReady(savedInstanceState: Bundle?) {
        validator = Validator.with(baseActivity).setMode(Mode.CONTINUOUS)
        val seedValidation = Validation(til_seed)
                .and(NotEmptyRule(" "))

        edit_seed.addTextChangedListener {
            on({ s, start, before, count ->
                validator
                        .validate(object : Validator.OnValidateListener {
                            override fun onValidateSuccess(values: List<String>) {
                                button_continue.isEnabled = true
                            }

                            override fun onValidateFailed() {
                                button_continue.isEnabled = false
                            }
                        }, seedValidation)
            })
        }

        button_continue.click {
            launchActivity<ProtectAccountActivity> {
                putExtra(ProtectAccountActivity.BUNDLE_ACCOUNT_ADDRESS, "MkSuckMydickmMak1593x1GrfYmFdsf83skS11")
            }
        }
    }

}
