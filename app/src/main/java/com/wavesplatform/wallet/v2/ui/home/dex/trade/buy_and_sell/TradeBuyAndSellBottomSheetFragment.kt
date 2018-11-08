package com.wavesplatform.wallet.v2.ui.home.dex.trade.buy_and_sell

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.flyco.tablayout.listener.OnTabSelectListener
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.local.BuySellData
import com.wavesplatform.wallet.v2.data.model.local.WatchMarket
import com.wavesplatform.wallet.v2.ui.base.view.BaseBottomSheetDialogFragment
import com.wavesplatform.wallet.v2.ui.home.dex.trade.TradeActivity
import com.wavesplatform.wallet.v2.ui.home.dex.trade.buy_and_sell.order.TradeOrderFragment
import com.wavesplatform.wallet.v2.ui.home.wallet.leasing.confirmation.ConfirmationLeasingActivity.Companion.BUNDLE_AMOUNT
import com.wavesplatform.wallet.v2.util.notNull
import kotlinx.android.synthetic.main.buy_and_sell_bottom_sheet_dialog_layout.*
import kotlinx.android.synthetic.main.buy_and_sell_bottom_sheet_dialog_layout.view.*
import pers.victor.ext.dp2px
import pers.victor.ext.findColor
import javax.inject.Inject


class TradeBuyAndSellBottomSheetFragment : BaseBottomSheetDialogFragment(), TradeBuyAndSellView, SuccessOrderListener {
    @Inject
    @InjectPresenter
    lateinit var presenter: TradeBuyAndSellPresenter

    @ProvidePresenter
    fun providePresenter(): TradeBuyAndSellPresenter = presenter

    companion object {
        var BUNDLE_DATA = "data"
        var BUY_TYPE = 0
        var SELL_TYPE = 1

        fun newInstance(data: BuySellData): TradeBuyAndSellBottomSheetFragment {
            val args = Bundle()
            args.classLoader = BuySellData::class.java.classLoader
            args.putParcelable(BUNDLE_DATA, data)
            val fragment = TradeBuyAndSellBottomSheetFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.buy_and_sell_bottom_sheet_dialog_layout, container, false)

        presenter.data = arguments?.getParcelable<BuySellData>(BUNDLE_DATA)

        val fragments = arrayListOf<Fragment>(
                TradeOrderFragment.newInstance(presenter.data, this),
                TradeOrderFragment.newInstance(presenter.data, this)
        )

        val adapter = TradeBuyAndSellPageAdapter(childFragmentManager, fragments,
                arrayOf(getString(R.string.buy_and_sell_dialog_buy_tab), getString(R.string.buy_and_sell_dialog_sell_tab)))
        rootView.viewpager_buy_sell.adapter = adapter
        rootView.stl_buy_sell.setViewPager(rootView.viewpager_buy_sell)
        if (presenter.data?.orderType == BUY_TYPE) {
            rootView.stl_buy_sell.currentTab = 0
        } else if (presenter.data?.orderType == SELL_TYPE) {
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
        mColorAnimation.addUpdateListener { animation ->
            rootView.stl_buy_sell.indicatorColor = (animation?.animatedValue.toString().toInt());
        }
        mColorAnimation.duration = (2 - 1) * 10000000000L

        rootView.viewpager_buy_sell.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                mColorAnimation.currentPlayTime = ((positionOffset + position) * 10000000000L).toLong();

                if (position < adapter.count - 1 && position < colors.size - 1) {
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

    override fun onSuccessPlaceOrder() {
        dismiss()
    }

}

interface SuccessOrderListener {
    fun onSuccessPlaceOrder()
}
