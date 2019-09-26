/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.base.view

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.analytics.AnalyticEvents
import com.wavesplatform.wallet.v2.data.analytics.analytics
import com.yarolegovich.slidingrootnav.SlidingRootNav
import com.yarolegovich.slidingrootnav.SlidingRootNavBuilder
import com.yarolegovich.slidingrootnav.callback.DragStateListener
import kotlinx.android.synthetic.main.content_menu_left_drawer.view.*
import pers.victor.ext.*

abstract class BaseDrawerLoginActivity : BaseActivity() {

    lateinit var slidingRootNav: SlidingRootNav
    private var view: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        slidingRootNav = SlidingRootNavBuilder(this)
                .withDragDistance(px2dp(screenWidth) - dp2px(18))
                .withRootViewScale(0.87f)
                .withRootViewElevation(10)
                .addDragStateListener(object : DragStateListener {
                    override fun onDragEnd(isMenuOpened: Boolean) {
                        if (isMenuOpened) {
                            analytics.trackEvent(AnalyticEvents.WavesMenuPageEvent)
                            view?.visiable()
                        } else {
                            view?.gone()
                        }
                    }

                    override fun onDragStart() {
                    }
                })
                .addDragListener { progress ->
                    slidingRootNav.layout.linear_drawer.scaleX = 1.5f - (progress / 2)
                    slidingRootNav.layout.linear_drawer.scaleY = 1.5f - (progress / 2)

                    if (progress > 0.02) {
                        if (window.statusBarColor != R.color.white)
                            setStatusBarColor(R.color.white)
                    } else {
                        if (window.statusBarColor != R.color.basic50)
                            setStatusBarColor(R.color.basic50)
                    }
                }
                .withMenuOpened(false)
                .withSavedState(savedInstanceState)
                .withMenuLayout(R.layout.content_menu_left_drawer) // TODO: Change to correct layout
                .inject()

        createCloseView()
        slidingRootNav.layout.linear_drawer.scaleX = 1.5f
        slidingRootNav.layout.linear_drawer.scaleY = 1.5f
    }

    private fun createCloseView() {
        view = View(this@BaseDrawerLoginActivity)

        val params: ViewGroup.LayoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, // This will define text view width
                ViewGroup.LayoutParams.MATCH_PARENT // This will define text view height
        )
        view?.layoutParams = params
        view?.setBackgroundColor(Color.TRANSPARENT)
        view?.click {
            slidingRootNav.closeMenu(true)
        }
        slidingRootNav.layout.findViewById<ViewGroup>(R.id.root).addView(view)

        view?.gone()
    }
}
