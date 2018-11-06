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
import com.wavesplatform.wallet.v2.data.model.local.WatchMarket
import com.wavesplatform.wallet.v2.ui.base.view.BaseBottomSheetDialogFragment
import com.wavesplatform.wallet.v2.ui.home.dex.trade.TradeActivity
import com.wavesplatform.wallet.v2.ui.home.dex.trade.buy_and_sell.order.TradeOrderFragment
import com.wavesplatform.wallet.v2.util.notNull
import kotlinx.android.synthetic.main.buy_and_sell_bottom_sheet_dialog_layout.*
import kotlinx.android.synthetic.main.buy_and_sell_bottom_sheet_dialog_layout.view.*
import pers.victor.ext.dp2px
import pers.victor.ext.findColor
import javax.inject.Inject


class TradeBuyAndSellBottomSheetFragment : BaseBottomSheetDialogFragment(), TradeBuyAndSellView {

    @Inject
    @InjectPresenter
    lateinit var presenter: TradeBuyAndSellPresenter

    @ProvidePresenter
    fun providePresenter(): TradeBuyAndSellPresenter = presenter

    companion object {
        var BUNDLE_OPEN = "init_open"
        var BUNDLE_PRICE = "init_price"
        var BUNDLE_AMOUNT = "init_amount"
        var BUY_TYPE = 0
        var SELL_TYPE = 1

        fun newInstance(watchMarket: WatchMarket?, initOpen: Int, initPrice: Long? = null, initAmount: Long? = null): TradeBuyAndSellBottomSheetFragment {
            val args = Bundle()
            args.classLoader = WatchMarket::class.java.classLoader
            args.putParcelable(TradeActivity.BUNDLE_MARKET, watchMarket)
            args.putInt(BUNDLE_OPEN, initOpen)
            initPrice.notNull {
                args.putLong(BUNDLE_PRICE, it)
            }
            initAmount.notNull {
                args.putLong(BUNDLE_AMOUNT, it)
            }
            val fragment = TradeBuyAndSellBottomSheetFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.buy_and_sell_bottom_sheet_dialog_layout, container, false)

        presenter.watchMarket = arguments?.getParcelable<WatchMarket>(TradeActivity.BUNDLE_MARKET)

        val fragments = arrayListOf<Fragment>(
                TradeOrderFragment.newInstance(presenter.watchMarket, BUY_TYPE, arguments?.getLong(BUNDLE_PRICE), arguments?.getLong(BUNDLE_AMOUNT)),
                TradeOrderFragment.newInstance(presenter.watchMarket, SELL_TYPE, arguments?.getLong(BUNDLE_PRICE), arguments?.getLong(BUNDLE_AMOUNT))
        )

        val adapter = TradeBuyAndSellPageAdapter(childFragmentManager, fragments,
                arrayOf(getString(R.string.buy_and_sell_dialog_buy_tab), getString(R.string.buy_and_sell_dialog_sell_tab)))
        rootView.viewpager_buy_sell.adapter = adapter
        rootView.stl_buy_sell.setViewPager(rootView.viewpager_buy_sell)
        if (arguments?.getInt(BUNDLE_OPEN, 0) == BUY_TYPE) {
            rootView.stl_buy_sell.currentTab = 0
        } else if (arguments?.getInt(BUNDLE_OPEN, 0) == SELL_TYPE) {
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
}