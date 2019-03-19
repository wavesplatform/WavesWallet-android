package com.wavesplatform.wallet.v2.ui.whats_new

import android.os.Bundle
import android.support.v4.view.ViewPager
import android.view.Menu
import android.view.MenuItem
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.local.WhatsNewItem
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import kotlinx.android.synthetic.main.activity_whats_new.*
import pers.victor.ext.click
import javax.inject.Inject

class WhatsNewActivity : BaseActivity(), WhatsNewView {

    @Inject
    @InjectPresenter
    lateinit var presenter: WhatsNewPresenter
    @Inject
    lateinit var adapter: WhatsNewAdapter

    @ProvidePresenter
    fun providePresenter(): WhatsNewPresenter = presenter

    override fun configLayoutRes() = R.layout.activity_whats_new

    override fun onViewReady(savedInstanceState: Bundle?) {
        setStatusBarColor(R.color.basic50)
        setNavigationBarColor(R.color.basic50)
        setupToolbar(toolbar_view)

        adapter.items = populateList()

        view_pager.adapter = adapter
        view_pager.offscreenPageLimit = 5
        view_pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                if (view_pager.currentItem == adapter.items.size - 1) {
                    text_next.text = getString(R.string.whats_new_okay)
                    text_next.click {
                        finish()
                    }
                } else {
                    text_next.text = getString(R.string.whats_new_next)
                    text_next.click {
                        view_pager.currentItem = view_pager.currentItem + 1
                    }
                }
            }
        })
        view_pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
            }
        })
    }

    private fun populateList(): ArrayList<WhatsNewItem> {
        return arrayListOf(WhatsNewItem(R.drawable.text_logo, R.string.app_name, R.string.app_name), WhatsNewItem(R.drawable.text_logo, R.string.app_name, R.string.app_name),
                WhatsNewItem(R.drawable.text_logo, R.string.app_name, R.string.app_name),
                WhatsNewItem(R.drawable.text_logo, R.string.app_name, R.string.app_name),
                WhatsNewItem(R.drawable.text_logo, R.string.app_name, R.string.app_name),
                WhatsNewItem(R.drawable.text_logo, R.string.app_name, R.string.app_name),
                WhatsNewItem(R.drawable.text_logo, R.string.app_name, R.string.app_name))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_close -> {
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_close, menu)
        return super.onCreateOptionsMenu(menu)
    }
}
