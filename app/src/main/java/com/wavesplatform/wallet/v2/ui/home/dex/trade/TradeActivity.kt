package com.wavesplatform.wallet.v2.ui.home.dex.trade

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.local.WatchMarket
import com.wavesplatform.wallet.v2.data.model.remote.response.MarketResponse
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.ui.home.dex.markets.DexMarketInformationBottomSheetFragment
import com.wavesplatform.wallet.v2.util.notNull
import kotlinx.android.synthetic.main.activity_trade.*
import javax.inject.Inject


class TradeActivity : BaseActivity(), TradeView {

    @Inject
    @InjectPresenter
    lateinit var presenter: TradePresenter

    @ProvidePresenter
    fun providePresenter(): TradePresenter = presenter

    companion object {
        var BUNDLE_MARKET = "market"
    }

    override fun configLayoutRes() = R.layout.activity_trade


    override fun onCreate(savedInstanceState: Bundle?) {
        translucentStatusBar = true
        super.onCreate(savedInstanceState)
    }

    override fun onViewReady(savedInstanceState: Bundle?) {
        setupToolbar(toolbar_view, true, getToolbarTitle(), R.drawable.ic_toolbar_back_white)

        viewpageer_trade.adapter = TradeFragmentPageAdapter(supportFragmentManager, arrayOf(getString(R.string.dex_trade_tab_orderbook), getString(R.string.dex_trade_tab_chart),
                getString(R.string.dex_trade_tab_last_trades), getString(R.string.dex_trade_tab_my_orders)))
        stl_trade.setViewPager(viewpageer_trade)
        stl_trade.currentTab = 0
    }

    private fun getToolbarTitle(): String {
        val market = intent.getParcelableExtra<WatchMarket>(BUNDLE_MARKET)
        presenter.market = market
        return "${market.market.amountAssetName} / ${market.market.priceAssetName}"
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_info -> {
                presenter.market.notNull {
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


}
