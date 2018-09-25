package com.wavesplatform.wallet.v2.ui.home.quick_action.receive

import android.os.Bundle
import android.support.v4.view.ViewPager
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import kotlinx.android.synthetic.main.activity_receive.*
import pers.victor.ext.dp2px

class ReceiveActivity : BaseActivity(), ReceiveView {

    override fun configLayoutRes(): Int = R.layout.activity_receive

    override fun onViewReady(savedInstanceState: Bundle?) {
        setStatusBarColor(R.color.basic50)
        setupToolbar(toolbar_view, true, getString(R.string.receive_toolbar),
                R.drawable.ic_toolbar_back_black)
        setupUI()
    }

    private fun setupUI() {
        viewpager_receive.adapter = ReceivePageAdapter(supportFragmentManager, this)
        stl_receive.setViewPager(viewpager_receive)

        stl_receive.getTitleView(CRYPTOCURRENCY).setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_r_gateway_14_white, 0, 0, 0)
        stl_receive.getTitleView(INVOICE).setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_r_inwaves_14_basic_500, 0, 0, 0)
        stl_receive.getTitleView(CARD).setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_r_card_14_basic_500, 0, 0, 0)

        stl_receive.getTitleView(CRYPTOCURRENCY).compoundDrawablePadding = dp2px(8)
        stl_receive.getTitleView(INVOICE).compoundDrawablePadding = dp2px(8)
        stl_receive.getTitleView(CARD).compoundDrawablePadding = dp2px(8)


        viewpager_receive.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                when (position) {
                    CRYPTOCURRENCY -> {
                        stl_receive.getTitleView(0).setCompoundDrawablesWithIntrinsicBounds(
                                R.drawable.ic_r_gateway_14_white, 0, 0, 0)
                        stl_receive.getTitleView(1).setCompoundDrawablesWithIntrinsicBounds(
                                R.drawable.ic_r_inwaves_14_basic_500, 0, 0, 0)
                        stl_receive.getTitleView(2).setCompoundDrawablesWithIntrinsicBounds(
                                R.drawable.ic_r_card_14_basic_500, 0, 0, 0)
                    }
                    INVOICE -> {
                        stl_receive.getTitleView(0).setCompoundDrawablesWithIntrinsicBounds(
                                R.drawable.ic_r_gateway_14_basic_500, 0, 0, 0)
                        stl_receive.getTitleView(1).setCompoundDrawablesWithIntrinsicBounds(
                                R.drawable.ic_r_inwaves_14_white, 0, 0, 0)
                        stl_receive.getTitleView(2).setCompoundDrawablesWithIntrinsicBounds(
                                R.drawable.ic_r_card_14_basic_500, 0, 0, 0)
                    }
                    CARD -> {
                        stl_receive.getTitleView(0).setCompoundDrawablesWithIntrinsicBounds(
                                R.drawable.ic_r_gateway_14_basic_500, 0, 0, 0)
                        stl_receive.getTitleView(1).setCompoundDrawablesWithIntrinsicBounds(
                                R.drawable.ic_r_inwaves_14_basic_500, 0, 0, 0)
                        stl_receive.getTitleView(2).setCompoundDrawablesWithIntrinsicBounds(
                                R.drawable.ic_r_card_14_white, 0, 0, 0)
                    }
                }
            }
        })
        stl_receive.currentTab = CRYPTOCURRENCY
    }

    companion object {
        const val CRYPTOCURRENCY = 0
        const val INVOICE = 1
        const val CARD = 2
    }
}
