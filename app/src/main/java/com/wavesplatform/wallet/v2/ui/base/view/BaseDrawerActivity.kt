package com.wavesplatform.wallet.v2.ui.base.view

import android.graphics.Paint
import android.os.Bundle
import android.view.View
import javax.inject.Inject

import com.arellomobile.mvp.presenter.InjectPresenter
import com.wavesplatform.wallet.v2.ui.welcome.WelcomeView
import com.wavesplatform.wallet.v2.ui.welcome.WelcomePresenter

import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity;

import com.arellomobile.mvp.presenter.ProvidePresenter;
import com.wavesplatform.wallet.R
import com.yarolegovich.slidingrootnav.SlidingRootNav
import com.yarolegovich.slidingrootnav.SlidingRootNavBuilder
import kotlinx.android.synthetic.main.activity_welcome.*
import pers.victor.ext.dp2px
import pers.victor.ext.px2dp
import pers.victor.ext.screenWidth
import android.graphics.Paint.UNDERLINE_TEXT_FLAG
import com.wavesplatform.wallet.R.id.toolbar_view
import com.wavesplatform.wallet.v2.util.setSystemBarTheme
import com.yarolegovich.slidingrootnav.callback.DragStateListener
import kotlinx.android.synthetic.main.menu_left_drawer.*
import kotlinx.android.synthetic.main.menu_left_drawer.view.*


abstract class BaseDrawerActivity : BaseActivity(), WelcomeView {

    protected lateinit var slidingRootNav: SlidingRootNav

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        slidingRootNav = SlidingRootNavBuilder(this)
                .withDragDistance(px2dp(screenWidth) - dp2px(18))
                .withRootViewScale(0.87f)
                .withRootViewElevation(10)
                .withToolbarMenuToggle(toolbar)
                .addDragListener { progress ->
                    if (progress > 0.5) setSystemBarTheme(false)
                    else setSystemBarTheme(true)
                }
                .withMenuOpened(false)
                .withContentClickableWhenMenuOpened(false)
                .withSavedState(savedInstanceState)
                .withMenuLayout(R.layout.menu_left_drawer)
                .inject()


        slidingRootNav.layout.text_site.paintFlags = text_site.paintFlags or Paint.UNDERLINE_TEXT_FLAG
    }

}
