/*
 * Created by Eduard Zaydel on 30/9/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.auth.migration.new_security_level

import android.os.Bundle
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.auth.new_account.secret_phrase.SecretPhrasePresenter
import com.wavesplatform.wallet.v2.ui.auth.new_account.secret_phrase.SecretPhraseView
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import kotlinx.android.synthetic.main.activity_new_level_of_security.*
import pers.victor.ext.click
import javax.inject.Inject

class NewLevelOfSecurityActivity : BaseActivity(), NewLevelOfSecurityView {

    @Inject
    @InjectPresenter
    lateinit var presenter: NewLevelOfSecurityPresenter

    @ProvidePresenter
    fun providePresenter(): NewLevelOfSecurityPresenter = presenter

    override fun configLayoutRes() = R.layout.activity_new_level_of_security

    override fun askPassCode() = false

    override fun onCreate(savedInstanceState: Bundle?) {
        overridePendingTransition(R.anim.slide_in_right, R.anim.null_animation)
        super.onCreate(savedInstanceState)
    }

    override fun onViewReady(savedInstanceState: Bundle?) {
        setStatusBarColor(R.color.basic50)
        setNavigationBarColor(R.color.basic50)
        setupToolbar(toolbar_view)

        button_confirm.click {
            // TODO: Multi account logic here
        }
    }

    override fun onBackPressed() {
        finish()
        overridePendingTransition(R.anim.null_animation, R.anim.slide_out_right)
    }
}
