/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.tutorial

import android.os.Bundle
import android.support.v4.view.ViewPager
import android.view.ViewPropertyAnimator
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.novoda.simplechromecustomtabs.SimpleChromeCustomTabs
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.analytics.AnalyticEvents
import com.wavesplatform.wallet.v2.data.analytics.analytics
import com.wavesplatform.wallet.v2.data.model.local.Language
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.ui.welcome.WelcomeActivity
import com.wavesplatform.wallet.v2.util.launchActivity
import com.wavesplatform.wallet.v2.util.openUrlWithChromeTab
import kotlinx.android.synthetic.main.activity_tutorial.*
import pers.victor.ext.click
import pers.victor.ext.dp
import pers.victor.ext.findColor
import javax.inject.Inject

class TutorialActivity : BaseActivity(), TutorialView {

    @Inject
    @InjectPresenter
    lateinit var presenter: TutorialPresenter
    lateinit var adapter: TutorialAdapter
    private lateinit var exitAnimator: ViewPropertyAnimator

    @ProvidePresenter
    fun providePresenter(): TutorialPresenter = presenter

    override fun configLayoutRes() = R.layout.activity_tutorial

    override fun askPassCode() = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setLanguage(Language.getLocale(intent.getStringExtra(BUNDLE_LANG)))
    }

    override fun onViewReady(savedInstanceState: Bundle?) {
        setStatusBarColor(R.color.basic50)
        setNavigationBarColor(R.color.basic50)

        val items = arrayListOf(R
                .layout.item_tutorial_1_card, R.layout.item_tutorial_2_card,
                R.layout.item_tutorial_3_card, R.layout.item_tutorial_4_card,
                R.layout.item_tutorial_5_card, R.layout.item_tutorial_6_card_confirm)

        adapter = TutorialAdapter(this, items)
        adapter.listener = object : TutorialAdapter.TutorialListener {
            override fun onEndOfScroll(position: Int) {
                if (position == view_pager.currentItem && position != adapter.items.size - 1) {
                    text_next.setTextColor(findColor(R.color.black))
                    text_next.isClickable = true
                    view_pager.setPagingEnabled(true)
                }
            }

            override fun onNotEndOfScroll(position: Int) {
                if (position == view_pager.currentItem && position != adapter.items.size - 1) {
                    text_next.setTextColor(findColor(R.color.accent100))
                    text_next.isClickable = false
                    view_pager.setPagingEnabled(false)
                }
            }

            override fun onSiteClicked(site: String) {
                openUrlWithChromeTab(site)
            }

            override fun canBegin(allCheckedToStart: Boolean) {
                if (allCheckedToStart) {
                    text_next.setTextColor(findColor(R.color.black))
                    text_next.isClickable = true
                } else {
                    text_next.setTextColor(findColor(R.color.accent100))
                    text_next.isClickable = false
                }
            }
        }

        text_next.click {
            view_pager.currentItem = view_pager.currentItem + 1
        }

        view_pager.adapter = adapter
        view_pager.offscreenPageLimit = 6
        view_pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                text_next.click {
                    view_pager.currentItem = view_pager.currentItem + 1
                }
                when {
                    view_pager.currentItem == adapter.items.size - 2 -> {
                        text_next.text = getString(R.string.card_tutorial_understand)
                    }
                    view_pager.currentItem == adapter.items.size - 1 -> {
                        text_next.text = getString(R.string.confirm_card_tutorial_submit)
                        text_next.click {
                            analytics.trackEvent(AnalyticEvents.NewUserConfirmEvent)
                            preferencesHelper.setTutorialPassed(true)
                            exitAnimation()
                        }

                        adapter.listener?.canBegin(adapter.isAllCheckedToStart())
                    }
                    else -> {
                        text_next.text = getString(R.string.card_tutorial_next)
                    }
                }
            }
        })
    }

    private fun exitAnimation() {
        if (::exitAnimator.isInitialized.not()) {
            relative_root.post {
                exitAnimator = relative_root.animate()
                        .translationY(50.dp.toFloat())
                        .alpha(0f)
                        .setDuration(250)
                        .withEndAction {
                            launchActivity<WelcomeActivity>(clear = true)
                            overridePendingTransition(0, R.anim.fade_out)
                        }
                exitAnimator.start()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        SimpleChromeCustomTabs.getInstance().connectTo(this)
    }

    override fun onPause() {
        SimpleChromeCustomTabs.getInstance().disconnectFrom(this)
        super.onPause()
    }


    companion object {
        var BUNDLE_LANG = "lang"
    }
}
