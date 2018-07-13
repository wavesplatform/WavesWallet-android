package com.wavesplatform.wallet.v2.ui.import_account.enter_seed

import android.os.Bundle
import android.view.View
import javax.inject.Inject

import com.arellomobile.mvp.presenter.InjectPresenter

import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity;

import com.arellomobile.mvp.presenter.ProvidePresenter;
import com.wavesplatform.wallet.R
import io.github.anderscheow.validator.Validation
import io.github.anderscheow.validator.Validator
import io.github.anderscheow.validator.constant.Mode
import io.github.anderscheow.validator.rules.common.MinRule
import io.github.anderscheow.validator.rules.common.NotEmptyRule
import kotlinx.android.synthetic.main.activity_enter_seed_manually.*
import pers.victor.ext.addTextChangedListener


class EnterSeedManuallyActivity : BaseActivity(), EnterSeedManuallyView {

    @Inject
    @InjectPresenter
    lateinit var presenter: EnterSeedManuallyPresenter

    @ProvidePresenter
    fun providePresenter(): EnterSeedManuallyPresenter = presenter

    override fun configLayoutRes() = R.layout.activity_enter_seed_manually


    override fun onViewReady(savedInstanceState: Bundle?) {
        setupToolbar(toolbar_view, View.OnClickListener { onBackPressed() }, true, title = getString(R.string.enter_seed_manually_toolbar_title), icon = R.drawable.ic_toolbar_back_black)


        val seedValidation = Validation(til_seed)
                .and(NotEmptyRule(" "))

        edit_seed.addTextChangedListener {
            on({ s, start, before, count ->
                Validator.with(applicationContext)
                        .setMode(Mode.CONTINUOUS)
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

    }

}
