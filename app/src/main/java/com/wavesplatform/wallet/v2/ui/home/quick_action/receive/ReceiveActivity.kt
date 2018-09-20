package com.wavesplatform.wallet.v2.ui.home.quick_action.receive

import android.content.Intent
import android.os.Bundle
import android.support.v4.view.ViewPager
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import kotlinx.android.synthetic.main.activity_receive.*
import pers.victor.ext.dp2px

class ReceiveActivity : BaseActivity(), ReceiveView {

    override fun configLayoutRes(): Int = R.layout.activity_receive

    override fun onViewReady(savedInstanceState: Bundle?) {
        setupToolbar(toolbar_view, true, getString(R.string.receive_toolbar), R.drawable.ic_toolbar_back_black)

        setupUI()
    }

    private fun setupUI() {
        viewpager_receive.adapter = ReceivePageAdapter(supportFragmentManager, arrayOf(getString(R.string.receive_cryptocurrency), getString(R.string.receive_invoice),
                getString(R.string.receive_card),
                getString(R.string.receive_bank)))
        stl_receive.setViewPager(viewpager_receive)

        stl_receive.getTitleView(0).setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_r_gateway_14_white, 0, 0, 0)
        stl_receive.getTitleView(1).setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_r_inwaves_14_basic_500, 0, 0, 0)
        stl_receive.getTitleView(2).setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_r_card_14_basic_500, 0, 0, 0)
        stl_receive.getTitleView(3).setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_r_bank_14_basic_500, 0, 0, 0)

        stl_receive.getTitleView(0).compoundDrawablePadding = dp2px(8);
        stl_receive.getTitleView(1).compoundDrawablePadding = dp2px(8);
        stl_receive.getTitleView(2).compoundDrawablePadding = dp2px(8);
        stl_receive.getTitleView(3).compoundDrawablePadding = dp2px(8);


        viewpager_receive.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> {
                        stl_receive.getTitleView(0).setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_r_gateway_14_white, 0, 0, 0)
                        stl_receive.getTitleView(1).setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_r_inwaves_14_basic_500, 0, 0, 0)
                        stl_receive.getTitleView(2).setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_r_card_14_basic_500, 0, 0, 0)
                        stl_receive.getTitleView(3).setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_r_bank_14_basic_500, 0, 0, 0)
                    }
                    1 -> {
                        stl_receive.getTitleView(0).setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_r_gateway_14_basic_500, 0, 0, 0)
                        stl_receive.getTitleView(1).setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_r_inwaves_14_white, 0, 0, 0)
                        stl_receive.getTitleView(2).setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_r_card_14_basic_500, 0, 0, 0)
                        stl_receive.getTitleView(3).setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_r_bank_14_basic_500, 0, 0, 0)
                    }
                    2 -> {
                        stl_receive.getTitleView(0).setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_r_gateway_14_basic_500, 0, 0, 0)
                        stl_receive.getTitleView(1).setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_r_inwaves_14_basic_500, 0, 0, 0)
                        stl_receive.getTitleView(2).setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_r_card_14_white, 0, 0, 0)
                        stl_receive.getTitleView(3).setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_r_bank_14_basic_500, 0, 0, 0)
                    }
                    3 -> {
                        stl_receive.getTitleView(0).setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_r_gateway_14_basic_500, 0, 0, 0)
                        stl_receive.getTitleView(1).setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_r_inwaves_14_basic_500, 0, 0, 0)
                        stl_receive.getTitleView(2).setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_r_card_14_basic_500, 0, 0, 0)
                        stl_receive.getTitleView(3).setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_r_bank_14_white, 0, 0, 0)
                    }
                }
            }
        })

        stl_receive.currentTab = 0
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
//
    }
}
