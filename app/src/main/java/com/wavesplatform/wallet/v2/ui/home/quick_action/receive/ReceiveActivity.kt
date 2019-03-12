package com.wavesplatform.wallet.v2.ui.home.quick_action.receive

import android.os.Bundle
import android.support.v4.view.ViewPager
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.ui.home.quick_action.receive.card.CardFragment
import com.wavesplatform.wallet.v2.ui.home.quick_action.receive.cryptocurrency.CryptoCurrencyFragment
import com.wavesplatform.wallet.v2.ui.home.quick_action.receive.invoice.InvoiceFragment
import com.wavesplatform.wallet.v2.ui.home.wallet.your_assets.YourAssetsActivity
import kotlinx.android.synthetic.main.activity_receive.*
import pers.victor.ext.dp2px

class ReceiveActivity : BaseActivity(), ReceiveView {

    private var adapter: ReceivePageAdapter? = null

    override fun configLayoutRes(): Int = R.layout.activity_receive

    override fun onCreate(savedInstanceState: Bundle?) {
        overridePendingTransition(R.anim.slide_in_right, R.anim.null_animation)
        super.onCreate(savedInstanceState)
    }

    override fun onViewReady(savedInstanceState: Bundle?) {
        setStatusBarColor(R.color.basic50)
        setNavigationBarColor(R.color.basic50)
        setupToolbar(toolbar_view, true, getString(R.string.receive_toolbar),
                R.drawable.ic_toolbar_back_black)
        setupUI()
    }

    private fun setupUI() {
        adapter = if (intent.hasExtra(YourAssetsActivity.BUNDLE_ASSET_ITEM)) {
            val assetBalance =
                    intent.getParcelableExtra<AssetBalance>(YourAssetsActivity.BUNDLE_ASSET_ITEM)
            ReceivePageAdapter(supportFragmentManager, this, assetBalance)
        } else {
            ReceivePageAdapter(supportFragmentManager, this, null)
        }

        viewpager_receive.adapter = adapter
        stl_receive.setViewPager(viewpager_receive)

        viewpager_receive.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
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
        for (i in adapter!!.data.indices) {
            if (adapter!!.getItem(position) == adapter!!.data[i]) {
                when (adapter!!.data[i]) {
                    is CryptoCurrencyFragment -> {
                        stl_receive.getTitleView(i).setCompoundDrawablesWithIntrinsicBounds(
                                R.drawable.ic_r_gateway_14_white, 0, 0, 0)
                        stl_receive.getTitleView(i).compoundDrawablePadding = dp2px(8)
                    }
                    is InvoiceFragment -> {
                        stl_receive.getTitleView(i).setCompoundDrawablesWithIntrinsicBounds(
                                R.drawable.ic_r_inwaves_14_white, 0, 0, 0)
                        stl_receive.getTitleView(i).compoundDrawablePadding = dp2px(8)
                    }
                    is CardFragment -> {
                        stl_receive.getTitleView(i).setCompoundDrawablesWithIntrinsicBounds(
                                R.drawable.ic_r_card_14_white, 0, 0, 0)
                        stl_receive.getTitleView(i).compoundDrawablePadding = dp2px(8)
                    }
                }
            }
        }
    }

    private fun resetFragmentsTitles() {
        for (i in adapter!!.data.indices) {
            when (adapter!!.data[i]) {
                is CryptoCurrencyFragment -> {
                    stl_receive.getTitleView(i).setCompoundDrawablesWithIntrinsicBounds(
                            R.drawable.ic_r_gateway_14_basic_500, 0, 0, 0)
                    stl_receive.getTitleView(i).compoundDrawablePadding = dp2px(8)
                }
                is InvoiceFragment -> {
                    stl_receive.getTitleView(i).setCompoundDrawablesWithIntrinsicBounds(
                            R.drawable.ic_r_inwaves_14_basic_500, 0, 0, 0)
                    stl_receive.getTitleView(i).compoundDrawablePadding = dp2px(8)
                }
                is CardFragment -> {
                    stl_receive.getTitleView(i).setCompoundDrawablesWithIntrinsicBounds(
                            R.drawable.ic_r_card_14_basic_500, 0, 0, 0)
                    stl_receive.getTitleView(i).compoundDrawablePadding = dp2px(8)
                }
            }
        }
    }

    override fun onBackPressed() {
        finish()
        overridePendingTransition(R.anim.null_animation, R.anim.slide_out_right)
    }

    override fun needToShowNetworkMessage(): Boolean = true
}
