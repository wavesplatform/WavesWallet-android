package com.wavesplatform.wallet.v2.ui.success

import android.os.Bundle
import android.view.WindowManager
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


class SuccessActivity : BaseActivity(), SuccessView {

    @Inject
    @InjectPresenter
    lateinit var presenter: SuccessPresenter

    @ProvidePresenter
    fun providePresenter(): SuccessPresenter = presenter

    override fun configLayoutRes() = R.layout.activity_success

    override fun onCreate(savedInstanceState: Bundle?) {
        translucentStatusBar = true
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
    }

    override fun onViewReady(savedInstanceState: Bundle?) {
        intent.extras.notNull {
            val title = it.getString(KEY_INTENT_TITLE, "")
            val subtitle = it.getString(KEY_INTENT_SUBTITLE, "")
            text_title.text = title
            text_subtitle.text = subtitle
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