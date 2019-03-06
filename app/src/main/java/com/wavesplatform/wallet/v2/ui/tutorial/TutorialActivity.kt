package com.wavesplatform.wallet.v2.ui.tutorial

import android.os.Bundle
import android.support.v4.view.ViewPager
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.local.Language
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.ui.welcome.WelcomeActivity
import com.wavesplatform.wallet.v2.util.launchActivity
import kotlinx.android.synthetic.main.activity_tutorial.*
import pers.victor.ext.click
import pers.victor.ext.dp2px
import pers.victor.ext.findColor
import javax.inject.Inject

class TutorialActivity : BaseActivity(), TutorialView {

    @Inject
    @InjectPresenter
    lateinit var presenter: TutorialPresenter
    lateinit var adapter: TutorialAdapter

    @ProvidePresenter
    fun providePresenter(): TutorialPresenter = presenter

    override fun configLayoutRes() = R.layout.activity_tutorial

    override fun askPassCode() = false

    override fun onCreate(savedInstanceState: Bundle?) {
        setLanguage(Language.getLocale(intent.getStringExtra(BUNDLE_LANG)))
        super.onCreate(savedInstanceState)
    }

    override fun onViewReady(savedInstanceState: Bundle?) {
        setStatusBarColor(R.color.basic50)
        setNavigationBarColor(R.color.basic50)
        adapter = TutorialAdapter(this)
        adapter.items = arrayListOf(1, 2, 3, 4, 5)
        adapter.listener = object : TutorialAdapter.EndOfScrollListener {
            override fun onEndOfScroll(position: Int) {
                if (position == view_pager.currentItem) {
                    text_next.setTextColor(findColor(R.color.black))
                    text_next.isClickable = true
                    view_pager.setPagingEnabled(true)
                }
            }

            override fun onNotEndOfScroll(position: Int) {
                if (position == view_pager.currentItem) {
                    text_next.setTextColor(findColor(R.color.accent100))
                    text_next.isClickable = false
                    view_pager.setPagingEnabled(false)
                }
            }
        }

        text_next.click {
            view_pager.currentItem = view_pager.currentItem + 1
        }

        view_pager.adapter = adapter
        view_pager.offscreenPageLimit = 5
        view_pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                if (view_pager.currentItem == adapter.items.size - 1) {
                    text_next.text = getString(R.string.card_tutorial_understand)
                    text_next.click {
                        preferencesHelper.setTutorialPassed(true)
                        exitAnimation()
                    }
                } else {
                    text_next.text = getString(R.string.card_tutorial_next)
                    text_next.click {
                        view_pager.currentItem = view_pager.currentItem + 1
                    }
                }
            }
        })
    }

    private fun exitAnimation() {
        relative_root.post {
            relative_root.animate()
                    .translationY(dp2px(50).toFloat())
                    .alpha(0f)
                    .setDuration(250)
                    .withEndAction {
                        launchActivity<WelcomeActivity>(clear = true)
                        overridePendingTransition(0, R.anim.fade_out)
                    }
                    .start()
        }
    }

    companion object {
        var BUNDLE_LANG = "lang"
    }
}
