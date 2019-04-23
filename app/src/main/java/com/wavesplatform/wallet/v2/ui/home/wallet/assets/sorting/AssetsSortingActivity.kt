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
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.ui.home.wallet.assets.sorting.tab.AssetsSortingTabFragment
import kotlinx.android.synthetic.main.activity_assets_sorting.*
import pers.victor.ext.dp
import javax.inject.Inject


class AssetsSortingActivity : BaseActivity(), AssetsSortingView, AssetsSortingTabFragment.ToolbarShadowListener {

    @Inject
    @InjectPresenter
    lateinit var presenter: AssetsSortingPresenter

    lateinit var adapter: AssetSortingPagerAdapter

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

        val positionFragment = AssetsSortingTabFragment()
        positionFragment.shadowListener = this
        val visibilityFragment = AssetsSortingTabFragment()
        visibilityFragment.shadowListener = this

        adapter = AssetSortingPagerAdapter(supportFragmentManager,
                arrayOf("Position", "Visibility"),
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
                resetFragmentsTitles()
                highLight(position)
            }
        })

        resetFragmentsTitles()
        highLight(0)
        stl_receive.currentTab = 0
    }


    private fun highLight(position: Int) {
        when (position) {
            0 -> {
                stl_receive
                        .getTitleView(position)
                        .setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_position_18_black, 0, 0, 0)
            }
            1 -> {
                stl_receive
                        .getTitleView(position)
                        .setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_visibility_18_black, 0, 0, 0)
            }
        }
        stl_receive.getTitleView(position).compoundDrawablePadding = 14.dp
    }

    private fun resetFragmentsTitles() {
        for (i in adapter.fragments.indices) {
            when (i) {
                0 -> {
                    stl_receive
                            .getTitleView(i)
                            .setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_position_18_basic_500, 0, 0, 0)
                }
                1 -> {
                    stl_receive
                            .getTitleView(i)
                            .setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_visibility_18_basic_500, 0, 0, 0)
                }
            }
            stl_receive.getTitleView(i).compoundDrawablePadding = 14.dp
        }
    }

    override fun showToolbarShadow(show: Boolean) {
        appbar_layout.isSelected = show
    }

}
