package com.wavesplatform.wallet.v2.ui.home.dex

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.remote.response.Market
import com.wavesplatform.wallet.v2.ui.base.view.BaseFragment
import com.wavesplatform.wallet.v2.ui.home.dex.markets.DexMarketsActivity
import com.wavesplatform.wallet.v2.ui.home.dex.sorting.ActiveMarketsSortingActivity
import com.wavesplatform.wallet.v2.ui.home.dex.trade.TradeActivity
import com.wavesplatform.wallet.v2.util.launchActivity
import kotlinx.android.synthetic.main.fragment_dex_new.*
import pers.victor.ext.gone
import pers.victor.ext.visiable
import javax.inject.Inject

class DexFragment : BaseFragment(), DexView {

    @Inject
    @InjectPresenter
    lateinit var presenter: DexPresenter

    @ProvidePresenter
    fun providePresenter(): DexPresenter = presenter

    @Inject
    lateinit var adapter: DexAdapter
    var menu: Menu? = null

    override fun configLayoutRes(): Int = R.layout.fragment_dex_new

    companion object {

        /**
         * @return DexFragment instance
         * */
        fun newInstance(): DexFragment {
            return DexFragment()
        }
    }

    override fun onViewReady(savedInstanceState: Bundle?) {

        recycle_dex.layoutManager = LinearLayoutManager(baseActivity)
        recycle_dex.adapter = adapter
        recycle_dex.isNestedScrollingEnabled = false

        adapter.onItemClickListener = BaseQuickAdapter.OnItemClickListener { adapter, view, position ->
            launchActivity<TradeActivity> {
                putExtra(TradeActivity.BUNDLE_MARKET, this@DexFragment.adapter.getItem(position))
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_dex, menu)
        this.menu = menu
        presenter.loadActiveMarkets()
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_sorting -> {
                launchActivity<ActiveMarketsSortingActivity> { }
            }
            R.id.action_add_market -> {
                launchActivity<DexMarketsActivity> { }
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun afterSuccessLoadMarkets(list: ArrayList<Market>) {
        if (list.isEmpty()) {
            linear_content.gone()
            linear_empty.visiable()
            menu?.findItem(R.id.action_sorting)?.isVisible = false
        } else {
            linear_empty.gone()
            linear_content.visiable()
            menu?.findItem(R.id.action_sorting)?.isVisible = true
            adapter.setNewData(list)
        }
    }

    override fun afterFailedLoadMarkets() {
    }
}
