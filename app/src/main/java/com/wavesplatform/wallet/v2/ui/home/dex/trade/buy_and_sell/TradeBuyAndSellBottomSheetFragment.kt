package com.wavesplatform.wallet.v2.ui.home.dex.trade.buy_and_sell

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.v4.view.ViewPager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.flyco.tablayout.listener.OnTabSelectListener
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.base.view.BaseBottomSheetDialogFragment
import kotlinx.android.synthetic.main.buy_and_sell_bottom_sheet_dialog_layout.*
import kotlinx.android.synthetic.main.buy_and_sell_bottom_sheet_dialog_layout.view.*
import pers.victor.ext.dp2px
import pers.victor.ext.findColor


class TradeBuyAndSellBottomSheetFragment : BaseBottomSheetDialogFragment() {

    companion object {
        var BUNDLE_OPEN = "init_open"
        var OPEN_BUY = 0
        var OPEN_SELL = 1
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.buy_and_sell_bottom_sheet_dialog_layout, container, false)


        var adapter = TradeBuyAndSellPageAdapter(childFragmentManager,
                arrayOf(getString(R.string.buy_and_sell_dialog_buy_tab), getString(R.string.buy_and_sell_dialog_sell_tab)))
        rootView.viewpager_buy_sell.adapter = adapter
        rootView.stl_buy_sell.setViewPager(rootView.viewpager_buy_sell)
        if (arguments?.getInt(BUNDLE_OPEN, 0) == OPEN_BUY) {
            rootView.stl_buy_sell.currentTab = 0
        } else if (arguments?.getInt(BUNDLE_OPEN, 0) == OPEN_SELL) {
            rootView.stl_buy_sell.currentTab = 1
        }
        rootView.stl_buy_sell.setOnTabSelectListener(object : OnTabSelectListener {
            override fun onTabSelect(position: Int) {
                when (position) {
                    0 -> {
                        rootView.stl_buy_sell.indicatorColor = findColor(R.color.submit400)
                    }
                    1 -> {
                        rootView.stl_buy_sell.indicatorColor = findColor(R.color.error400)
                    }
                }
            }

            override fun onTabReselect(position: Int) {

            }
        })

        rootView.appbar_layout.addOnOffsetChangedListener(
                AppBarLayout.OnOffsetChangedListener { _, verticalOffset ->
                    Log.d("TEST", verticalOffset.toString())
                    val offsetForShowShadow = appbar_layout.totalScrollRange - dp2px(9)
                    if (-verticalOffset > offsetForShowShadow) {
                        rootView.viewpager_buy_sell.setPagingEnabled(false)
                    } else {
                        rootView.viewpager_buy_sell.setPagingEnabled(true)
                    }
                })

        val colors = arrayOf<Int>(findColor(R.color.submit400), findColor(R.color.error400))
        val argbEvaluator = ArgbEvaluator()
        val mColorAnimation = ValueAnimator.ofObject(argbEvaluator, findColor(R.color.submit400), findColor(R.color.error400))
        mColorAnimation.addUpdateListener(object : ValueAnimator.AnimatorUpdateListener {
            override fun onAnimationUpdate(animation: ValueAnimator?) {
                rootView.stl_buy_sell.indicatorColor = (animation?.animatedValue.toString().toInt());
            }
        })
        mColorAnimation.duration = (2 - 1) * 10000000000L

        rootView.viewpager_buy_sell.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                mColorAnimation.currentPlayTime = ((positionOffset + position) * 10000000000L).toLong();

                if (position < adapter.getCount() - 1 && position < colors.size - 1) {

                    rootView.stl_buy_sell.indicatorColor = (argbEvaluator.evaluate(positionOffset, colors[position], colors[position + 1]) as Int)

                } else {

                    rootView.stl_buy_sell.indicatorColor = colors[colors.size - 1]

                }
            }

            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> {
                        rootView.stl_buy_sell.indicatorColor = findColor(R.color.submit400)
                    }
                    1 -> {
                        rootView.stl_buy_sell.indicatorColor = findColor(R.color.error400)
                    }
                }
            }

        })


        return rootView
    }
}