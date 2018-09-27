package com.wavesplatform.wallet.v2.ui.home.wallet.assets.token_burn.confirmation

import android.os.Bundle
import android.view.animation.AnimationUtils
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import kotlinx.android.synthetic.main.activity_token_burn_confirmation.*
import pers.victor.ext.click
import pers.victor.ext.gone
import pers.victor.ext.invisiable
import pers.victor.ext.visiable
import pyxis.uzuki.live.richutilskt.utils.runDelayed
import javax.inject.Inject


class TokenBurnConfirmationActivity : BaseActivity(), TokenBurnConfirmationView {

    @Inject
    @InjectPresenter
    lateinit var presenter: TokenBurnConfirmationPresenter

    @ProvidePresenter
    fun providePresenter(): TokenBurnConfirmationPresenter = presenter

    override fun configLayoutRes() = R.layout.activity_token_burn_confirmation


    override fun onCreate(savedInstanceState: Bundle?) {
        translucentStatusBar = true
        super.onCreate(savedInstanceState)
    }

    override fun onViewReady(savedInstanceState: Bundle?) {
        setupToolbar(toolbar_view, true, getString(R.string.token_burn_confirmation_toolbar_title), R.drawable.ic_toolbar_back_white)

        button_confirm.click {
            toolbar_view.invisiable()
            card_content.gone()
            card_progress.visiable()
            var rotation = AnimationUtils.loadAnimation(this@TokenBurnConfirmationActivity, R.anim.rotate);
            rotation.fillAfter = true;
            image_loader.startAnimation(rotation);
            runDelayed(2000, {
                image_loader.clearAnimation()
                card_progress.gone()
                relative_success.visiable()
                button_okay.click {
                    finish()
                }
            })
        }
    }

}
