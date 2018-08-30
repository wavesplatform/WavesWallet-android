package com.wavesplatform.wallet.v2.ui.auth.import_account.enter_seed

import android.os.Bundle
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v1.data.access.AccessState
import com.wavesplatform.wallet.v1.data.auth.WavesWallet
import com.wavesplatform.wallet.v2.ui.auth.import_account.protect_account.ProtectAccountActivity
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.util.launchActivity
import io.github.anderscheow.validator.Validation
import io.github.anderscheow.validator.Validator
import io.github.anderscheow.validator.constant.Mode
import io.github.anderscheow.validator.rules.common.NotEmptyRule
import kotlinx.android.synthetic.main.activity_enter_seed_manually.*
import org.apache.commons.io.Charsets
import pers.victor.ext.addTextChangedListener
import pers.victor.ext.click
import javax.inject.Inject


class EnterSeedManuallyActivity : BaseActivity(), EnterSeedManuallyView {

    @Inject
    @InjectPresenter
    lateinit var presenter: EnterSeedManuallyPresenter
    lateinit var validator: Validator

    @ProvidePresenter
    fun providePresenter(): EnterSeedManuallyPresenter = presenter

    override fun configLayoutRes() = R.layout.activity_enter_seed_manually


    override fun onViewReady(savedInstanceState: Bundle?) {
        setupToolbar(toolbar_view, View.OnClickListener { onBackPressed() }, true, title = getString(R.string.enter_seed_manually_toolbar_title), icon = R.drawable.ic_toolbar_back_black)

        validator = Validator.with(applicationContext).setMode(Mode.CONTINUOUS)
        val seedValidation = Validation(til_seed)
                .and(NotEmptyRule(" "))

        edit_seed.addTextChangedListener {
            on { s, start, before, count ->
                validator
                        .validate(object : Validator.OnValidateListener {
                            override fun onValidateSuccess(values: List<String>) {
                                button_continue.isEnabled = true
                            }

                            override fun onValidateFailed() {
                                button_continue.isEnabled = false
                            }
                        }, seedValidation)
            }
        }

        button_continue.click {
            launchActivity<ProtectAccountActivity> {
                putExtra(ProtectAccountActivity.BUNDLE_SEED, edit_seed.text.toString().trim())
            }
        }
    }

}
