package com.wavesplatform.wallet.v2.ui.home.dex

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.Events
import com.wavesplatform.wallet.v2.data.model.local.WatchMarket
import com.wavesplatform.wallet.v2.data.model.remote.response.MarketResponse
import com.wavesplatform.wallet.v2.ui.base.view.BaseFragment
import com.wavesplatform.wallet.v2.ui.home.MainActivity
import com.wavesplatform.wallet.v2.ui.home.dex.markets.DexMarketsActivity
import com.wavesplatform.wallet.v2.ui.home.dex.sorting.ActiveMarketsSortingActivity
import com.wavesplatform.wallet.v2.ui.home.dex.trade.TradeActivity
import com.wavesplatform.wallet.v2.ui.home.wallet.assets.AssetsFragment.Companion.REQUEST_ASSET_DETAILS
import com.wavesplatform.wallet.v2.util.launchActivity
import kotlinx.android.synthetic.main.empty_dex_layout.view.*
import kotlinx.android.synthetic.main.fragment_dex_new.*
import pers.victor.ext.click
import pers.victor.ext.inflate
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
    private var onElevationAppBarChangeListener: MainActivity.OnElevationAppBarChangeListener? = null


    override fun configLayoutRes(): Int = R.layout.fragment_dex_new

    companion object {
        const val RESULT_NEED_UPDATE = "need_update"
        const val REQUEST_SORTING = 121

        fun newInstance(): DexFragment {
            return DexFragment()
        }
    }

    override fun onViewReady(savedInstanceState: Bundle?) {

        recycle_dex.layoutManager = LinearLayoutManager(baseActivity)
        recycle_dex.adapter = adapter


        eventSubscriptions.add(rxEventBus.filteredObservable(Events.ScrollToTopEvent::class.java)
                .subscribe {
                    if (it.position == MainActivity.DEX_SCREEN) {
                        recycle_dex.scrollToPosition(0)
                    }
                })

        adapter.onItemClickListener = BaseQuickAdapter.OnItemClickListener { adapter, view, position ->
            launchActivity<TradeActivity> {
                putExtra(TradeActivity.BUNDLE_MARKET, this@DexFragment.adapter.getItem(position))
            }
        }

        recycle_dex.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                onElevationAppBarChangeListener?.onChange(!recycle_dex.canScrollVertically(-1))
            }
        })
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
                launchActivity<ActiveMarketsSortingActivity>(REQUEST_SORTING)
            }
            R.id.action_add_market -> {
                launchActivity<DexMarketsActivity> { }
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun afterSuccessLoadMarkets(list: ArrayList<WatchMarket>) {
        adapter.setNewData(list)
        adapter.emptyView = getEmptyView()
        if (list.isEmpty()) {
            menu?.findItem(R.id.action_sorting)?.isVisible = false
            adapter.removeAllHeaderView()
        } else {
            menu?.findItem(R.id.action_sorting)?.isVisible = true
            adapter.setHeaderView(getHeaderView())
        }
    }

    private fun getHeaderView(): View? {
        return inflate(R.layout.header_dex_layout)
    }

    private fun getEmptyView(): View {
        val view = inflate(R.layout.empty_dex_layout)
        view.button_add_markets.click {
            launchActivity<DexMarketsActivity> { }
        }
        return view
    }

    fun setOnElevationChangeListener(listener: MainActivity.OnElevationAppBarChangeListener) {
        this.onElevationAppBarChangeListener = listener
    }

    override fun afterFailedLoadMarkets() {
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_SORTING -> {
                if (resultCode == Constants.RESULT_OK) {
                    val needToUpdate = data?.getBooleanExtra(RESULT_NEED_UPDATE, false)
                    if (needToUpdate == true) {
                        presenter.loadActiveMarkets()
                    }
                }
            }
        }
    }
}
