package com.wavesplatform.wallet.v2.ui.home.dex.trade.buy_and_sell

import android.animation.ArgbEvaluator
import android.os.Bundle
import android.support.v4.view.ViewPager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.flyco.tablayout.listener.OnTabSelectListener
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.base.view.BaseBottomSheetDialogFragment
import kotlinx.android.synthetic.main.buy_and_sell_bottom_sheet_dialog_layout.view.*
import pers.victor.ext.findColor
import android.animation.ValueAnimator
import kotlinx.android.synthetic.main.buy_and_sell_bottom_sheet_dialog_layout.*
import pers.victor.ext.dp2px


class BuyAndSellBottomSheetFragment : BaseBottomSheetDialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.buy_and_sell_bottom_sheet_dialog_layout, container, false)


        var adapter = BuyAndSellPageAdapter(childFragmentManager,
                arrayOf(getString(R.string.buy_and_sell_dialog_buy_tab), getString(R.string.buy_and_sell_dialog_sell_tab)))
        rootView.viewpager_buy_sell.adapter = adapter
        rootView.stl_buy_sell.setViewPager(rootView.viewpager_buy_sell)
        rootView.stl_buy_sell.currentTab = 0
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

        rootView.appbar_layout.addOnOffsetChangedListener({ appBarLayout, verticalOffset ->
            val offsetForShowShadow = appbar_layout.totalScrollRange - dp2px(9)
            if (-verticalOffset > offsetForShowShadow) {
                rootView.viewpager_buy_sell.setPagingEnabled(false)
            } else {
                rootView.viewpager_buy_sell.setPagingEnabled(true)
            }
        })

        val colors = arrayOf<Int>(findColor(R.color.submit400), findColor(R.color.error400))
        var argbEvaluator = ArgbEvaluator()
        val mColorAnimation = ValueAnimator.ofObject(argbEvaluator, findColor(R.color.submit400), findColor(R.color.error400))
        mColorAnimation.addUpdateListener(object: ValueAnimator.AnimatorUpdateListener{
            override fun onAnimationUpdate(animation: ValueAnimator?) {
                rootView.stl_buy_sell.indicatorColor = (animation?.animatedValue.toString().toInt());
            }
        })
        mColorAnimation.duration = (2 - 1) * 10000000000L

        rootView.viewpager_buy_sell.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                mColorAnimation.currentPlayTime = ((positionOffset + position)* 10000000000L).toLong();

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