package com.wavesplatform.wallet.v2.ui.home.dex.trade

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.view.Menu
import android.view.MenuItem
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.Events
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.ui.home.dex.markets.DexMarketInformationBottomSheetFragment
import com.wavesplatform.wallet.v2.ui.home.dex.trade.chart.TradeChartFragment
import com.wavesplatform.wallet.v2.ui.home.dex.trade.last_trades.TradeLastTradesFragment
import com.wavesplatform.wallet.v2.ui.home.dex.trade.my_orders.TradeMyOrdersFragment
import com.wavesplatform.wallet.v2.ui.home.dex.trade.orderbook.TradeOrderBookFragment
import com.wavesplatform.wallet.v2.util.notNull
import kotlinx.android.synthetic.main.activity_trade.*
import javax.inject.Inject

class TradeActivity : BaseActivity(), TradeView {

    @Inject
    @InjectPresenter
    lateinit var presenter: TradePresenter

    @ProvidePresenter
    fun providePresenter(): TradePresenter = presenter

    override fun configLayoutRes() = R.layout.activity_trade

    override fun onCreate(savedInstanceState: Bundle?) {
        translucentStatusBar = true
        overridePendingTransition(R.anim.slide_in_right, R.anim.null_animation)
        super.onCreate(savedInstanceState)
    }

    override fun onViewReady(savedInstanceState: Bundle?) {
        presenter.watchMarket = intent.getParcelableExtra(BUNDLE_MARKET)

        setupToolbar(toolbar_view, true, getToolbarTitle(), R.drawable.ic_toolbar_back_white)

        val pages = arrayListOf<Pair<Fragment, String>>(
                TradeOrderBookFragment.newInstance(presenter.watchMarket) to getString(R.string.dex_trade_tab_orderbook),
                TradeChartFragment.newInstance(presenter.watchMarket) to getString(R.string.dex_trade_tab_chart),
                TradeLastTradesFragment.newInstance(presenter.watchMarket) to getString(R.string.dex_trade_tab_last_trades),
                TradeMyOrdersFragment.newInstance(presenter.watchMarket) to getString(R.string.dex_trade_tab_my_orders))

        viewpageer_trade.adapter = TradeFragmentPageAdapter(supportFragmentManager, pages)
        viewpageer_trade.offscreenPageLimit = 4
        viewpageer_trade.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(p0: Int) {
            }

            override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {
            }

            override fun onPageSelected(position: Int) {
                if (position == 0) {
                    mRxEventBus.post(Events.OrderBookTabClickEvent())
                }
            }
        })

        stl_trade.setViewPager(viewpageer_trade)
        stl_trade.currentTab = 0
    }

    private fun getToolbarTitle(): String {
        return "${presenter.watchMarket?.market?.amountAssetShortName} / ${presenter.watchMarket?.market?.priceAssetShortName}"
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_info -> {
                presenter.watchMarket.notNull {
                    val infoDialog = DexMarketInformationBottomSheetFragment()
                    infoDialog.withMarketInformation(it.market)
                    infoDialog.show(supportFragmentManager, infoDialog::class.java.simpleName)
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_trade, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onBackPressed() {
        finish()
        overridePendingTransition(R.anim.null_animation, R.anim.slide_out_right)
    }

    override fun needToShowNetworkMessage() = true

    companion object {
        var BUNDLE_MARKET = "watchMarket"
    }
}
