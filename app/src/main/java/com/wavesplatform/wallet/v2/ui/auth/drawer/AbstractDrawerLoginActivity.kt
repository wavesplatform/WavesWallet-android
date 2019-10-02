/*
 * Created by Eduard Zaydel on 2/10/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.auth.drawer

import android.graphics.Color
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.view.ViewGroup
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.analytics.AnalyticEvents
import com.wavesplatform.wallet.v2.data.analytics.analytics
import com.wavesplatform.wallet.v2.ui.auth.add_account.AddAccountActivity
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.util.launchActivity
import com.yarolegovich.slidingrootnav.SlidingRootNav
import com.yarolegovich.slidingrootnav.SlidingRootNavBuilder
import com.yarolegovich.slidingrootnav.callback.DragStateListener
import kotlinx.android.synthetic.main.content_login_drawer.view.*
import pers.victor.ext.*
import javax.inject.Inject

abstract class AbstractDrawerLoginActivity : BaseActivity() {

    lateinit var slidingRootNav: SlidingRootNav
    private var view: View? = null

    @Inject
    lateinit var accountsAdapter: MyAccountsAdapter

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
                    slidingRootNav.layout.root_drawer.scaleX = 1.5f - (progress / 2)
                    slidingRootNav.layout.root_drawer.scaleY = 1.5f - (progress / 2)

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
                .withMenuLayout(R.layout.content_login_drawer)
                .inject()

        slidingRootNav.layout.recycle_accounts.layoutManager = LinearLayoutManager(this)
        accountsAdapter.bindToRecyclerView(slidingRootNav.layout.recycle_accounts)

        slidingRootNav.layout.linear_add_account.click {
            launchActivity<AddAccountActivity>(REQUEST_ADD_ACCOUNT)
        }

        createCloseView()
        slidingRootNav.layout.root_drawer.scaleX = 1.5f
        slidingRootNav.layout.root_drawer.scaleY = 1.5f
    }

    private fun createCloseView() {
        view = View(this@AbstractDrawerLoginActivity)

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

    companion object {
        private const val REQUEST_ADD_ACCOUNT = 432
    }
}
