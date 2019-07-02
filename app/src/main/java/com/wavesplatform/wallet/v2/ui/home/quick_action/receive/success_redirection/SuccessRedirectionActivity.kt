/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.quick_action.receive.success_redirection

import android.os.Bundle
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.ui.home.MainActivity
import com.wavesplatform.wallet.v2.util.launchActivity
import com.wavesplatform.wallet.v2.util.notNull
import kotlinx.android.synthetic.main.activity_success.*
import pers.victor.ext.click
import javax.inject.Inject

class SuccessRedirectionActivity : BaseActivity(), SuccessRedirectionView {

    @Inject
    @InjectPresenter
    lateinit var presenter: SuccessRedirectionPresenter

    @ProvidePresenter
    fun providePresenter(): SuccessRedirectionPresenter = presenter

    override fun configLayoutRes() = R.layout.activity_success

    override fun onCreate(savedInstanceState: Bundle?) {
        translucentStatusBar = true
        super.onCreate(savedInstanceState)
    }

    override fun onViewReady(savedInstanceState: Bundle?) {
        intent.extras.notNull {
            text_title.text = it.getString(KEY_INTENT_TITLE, "")
            text_subtitle.text = it.getString(KEY_INTENT_SUBTITLE, "")
            button_ok.click {
                launchActivity<MainActivity>(clear = true)
            }
        }
    }

    companion object {
        const val KEY_INTENT_TITLE = "intent_title"
        const val KEY_INTENT_SUBTITLE = "intent_subtitle"
    }
}