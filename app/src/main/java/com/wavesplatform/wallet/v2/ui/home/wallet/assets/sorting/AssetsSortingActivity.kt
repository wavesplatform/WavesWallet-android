/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.wallet.assets.sorting

import android.os.Bundle
import android.support.v4.view.ViewPager
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.local.TabItem
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.ui.home.wallet.assets.sorting.tab.AssetsSortingTabFragment
import com.wavesplatform.wallet.v2.util.setTabIcon
import kotlinx.android.synthetic.main.activity_assets_sorting.*
import pers.victor.ext.dp
import javax.inject.Inject


class AssetsSortingActivity : BaseActivity(), AssetsSortingView, AssetsSortingTabFragment.ToolbarShadowListener {

    @Inject
    @InjectPresenter
    lateinit var presenter: AssetsSortingPresenter
    lateinit var adapter: AssetSortingPagerAdapter

    private val tabs: Array<TabItem> by lazy {
        arrayOf(
                TabItem(getString(R.string.wallet_sorting_toolbar_position_action),
                        R.drawable.ic_position_18_black,
                        R.drawable.ic_position_18_basic_500),
                TabItem(getString(
                        R.string.wallet_sorting_toolbar_visibility_action),
                        R.drawable.ic_visibility_18_black,
                        R.drawable.ic_visibility_18_basic_500))
    }

    @ProvidePresenter
    fun providePresenter(): AssetsSortingPresenter = presenter

    override fun configLayoutRes() = R.layout.activity_assets_sorting

    override fun onCreate(savedInstanceState: Bundle?) {
        overridePendingTransition(R.anim.slide_in_right, R.anim.null_animation)
        super.onCreate(savedInstanceState)
    }

    override fun onViewReady(savedInstanceState: Bundle?) {
        setStatusBarColor(R.color.basic50)
        setupToolbar(toolbar_view, true, getString(com.wavesplatform.wallet.R.string.wallet_sorting_toolbar_title), R.drawable.ic_toolbar_back_black)

        val positionFragment = AssetsSortingTabFragment.newInstance(AssetsSortingTabFragment.TYPE_POSITION)
        val visibilityFragment = AssetsSortingTabFragment.newInstance(AssetsSortingTabFragment.TYPE_VISIBILITY)

        positionFragment.shadowListener = this
        visibilityFragment.shadowListener = this

        adapter = AssetSortingPagerAdapter(supportFragmentManager,
                tabs.map { it.tabTitle }.toTypedArray(),
                arrayOf(positionFragment, visibilityFragment))
        viewpager_sorting_assets.adapter = adapter
        viewpager_sorting_assets.setPagingEnabled(false)

        stl_receive.setViewPager(viewpager_sorting_assets)


        viewpager_sorting_assets.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                defaultTabsConfig()
                highLight(position)
            }
        })

        defaultTabsConfig()
        highLight(0)
        stl_receive.currentTab = 0
    }


    private fun highLight(position: Int) {
        stl_receive.setTabIcon(position, tabs[position].tabSelectedIcon, 14.dp)
    }

    private fun defaultTabsConfig() {
        tabs.forEachIndexed { index, tabItem ->
            stl_receive.setTabIcon(index, tabItem.tabUnselectedIcon, 14.dp)
        }
    }

    override fun showToolbarShadow(show: Boolean) {
        appbar_layout.isSelected = show
    }

}
