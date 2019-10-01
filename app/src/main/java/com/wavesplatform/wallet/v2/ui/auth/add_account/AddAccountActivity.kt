/*
 * Created by Eduard Zaydel on 1/10/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.auth.add_account

import android.os.Bundle
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.auth.import_account.ImportAccountActivity
import com.wavesplatform.wallet.v2.ui.auth.new_account.NewAccountActivity
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.ui.welcome.WelcomeActivity.Companion.REQUEST_IMPORT_ACC
import com.wavesplatform.wallet.v2.util.launchActivity
import kotlinx.android.synthetic.main.activity_add_account.*
import pers.victor.ext.click
import javax.inject.Inject

class AddAccountActivity : BaseActivity(), AddAccountView {

    @Inject
    @InjectPresenter
    lateinit var presenter: AddAccountPresenter

    @ProvidePresenter
    fun providePresenter(): AddAccountPresenter = presenter

    override fun configLayoutRes() = R.layout.activity_add_account

    override fun askPassCode() = false

    override fun onCreate(savedInstanceState: Bundle?) {
        overridePendingTransition(R.anim.slide_in_right, R.anim.null_animation)
        super.onCreate(savedInstanceState)
    }

    override fun onViewReady(savedInstanceState: Bundle?) {
        setStatusBarColor(R.color.basic50)
        setNavigationBarColor(R.color.basic50)
        setupToolbar(toolbar_view, homeEnable = true, icon = R.drawable.ic_toolbar_back_black)


        card_create_account.click {
            launchActivity<NewAccountActivity>()
            overridePendingTransition(R.anim.slide_in_right, R.anim.null_animation)
        }

        card_import_account.click {
            launchActivity<ImportAccountActivity>(REQUEST_IMPORT_ACC)
            overridePendingTransition(R.anim.slide_in_right, R.anim.null_animation)
        }
    }

    override fun onBackPressed() {
        finish()
        overridePendingTransition(R.anim.null_animation, R.anim.slide_out_right)
    }
}
