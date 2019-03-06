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
import com.wavesplatform.wallet.v1.util.PrefsUtil
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.Events
import com.wavesplatform.wallet.v2.data.model.local.WatchMarket
import com.wavesplatform.wallet.v2.ui.base.view.BaseFragment
import com.wavesplatform.wallet.v2.ui.home.MainActivity
import com.wavesplatform.wallet.v2.ui.home.dex.markets.DexMarketsActivity
import com.wavesplatform.wallet.v2.ui.home.dex.sorting.ActiveMarketsSortingActivity
import com.wavesplatform.wallet.v2.ui.home.dex.trade.TradeActivity
import com.wavesplatform.wallet.v2.util.currentDateAsTimeSpanString
import com.wavesplatform.wallet.v2.util.launchActivity
import com.wavesplatform.wallet.v2.util.notNull
import kotlinx.android.synthetic.main.empty_dex_layout.view.*
import kotlinx.android.synthetic.main.fragment_dex_new.*
import kotlinx.android.synthetic.main.header_dex_layout.view.*
import pers.victor.ext.click
import pers.victor.ext.inflate
import pers.victor.ext.isNetworkConnected
import java.util.*
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

    override fun onViewReady(savedInstanceState: Bundle?) {
        swipe_container.setColorSchemeResources(R.color.submit400)

        val linearLayoutManager = LinearLayoutManager(baseActivity)

        recycle_dex.layoutManager = linearLayoutManager
        recycle_dex.adapter = adapter

        swipe_container.setOnRefreshListener {
            loadInfoForPairs()
        }

        eventSubscriptions.add(rxEventBus.filteredObservable(Events.ScrollToTopEvent::class.java)
                .subscribe {
                    if (it.position == MainActivity.DEX_SCREEN) {
                        recycle_dex.scrollToPosition(0)
                    }
                })

        eventSubscriptions.add(rxEventBus.filteredObservable(Events.UpdateMarketAfterChangeChartTimeFrame::class.java)
                .subscribe { event ->
                    val market = adapter.data.firstOrNull { it.market.id == event.id }
                    market?.market?.currentTimeFrame = event.timeServer
                })

        adapter.onItemClickListener = BaseQuickAdapter.OnItemClickListener { adapter, view, position ->
            if (isNetworkConnected()) {
                val args = Bundle()
                args.classLoader = WatchMarket::class.java.classLoader
                args.putParcelable(TradeActivity.BUNDLE_MARKET, this@DexFragment.adapter.getItem(position))

                launchActivity<TradeActivity>(options = args)
            }
        }

        recycle_dex.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                presenter.hideShadow = !recycle_dex.canScrollVertically(-1)
                onElevationAppBarChangeListener?.onChange(presenter.hideShadow)
            }
        })

        presenter.loadActiveMarkets()
    }

    private fun loadInfoForPairs() {
        presenter.clearOldPairsSubscriptions()

        if (adapter.data.isNotEmpty()) {
            adapter.data.forEachIndexed { index, watchMarket ->
                presenter.loadDexPairInfo(watchMarket, index)
            }
        } else {
            swipe_container.notNull { it.isRefreshing = false }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.menu_dex, menu)
        this.menu = menu
        menu.findItem(R.id.action_sorting)?.isVisible = !adapter.data.isEmpty()
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_sorting -> {
                launchActivity<ActiveMarketsSortingActivity>(REQUEST_SORTING)
            }
            R.id.action_add_market -> {
                launchActivity<DexMarketsActivity>(REQUEST_SELECT_MARKETS)
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun afterSuccessLoadMarkets(list: ArrayList<WatchMarket>) {
        swipe_container.isRefreshing = false
        presenter.clearOldPairsSubscriptions()

        adapter.setNewData(list)

        if (adapter.emptyView == null) {
            adapter.emptyView = getEmptyView()
        }

        if (list.isEmpty()) {
            menu?.findItem(R.id.action_sorting)?.isVisible = false
            adapter.removeAllHeaderView()
        } else {
            menu?.findItem(R.id.action_sorting)?.isVisible = true
            adapter.setHeaderView(getHeaderView())

            // TODO: rewrite logic for request only for visible items
            list.forEachIndexed { index, watchMarket ->
                presenter.loadDexPairInfo(watchMarket, index)
            }
        }
    }

    override fun afterSuccessLoadPairInfo(watchMarket: WatchMarket, index: Int) {
        adapter.headerLayout?.text_last_update?.text = presenter.prefsUtil
                .getValue(PrefsUtil.KEY_LAST_UPDATE_DEX_INFO, 0L)
                .currentDateAsTimeSpanString(activity!!)

        swipe_container.isRefreshing = false

        adapter.setData(index, watchMarket)
    }

    private fun getHeaderView(): View? {
        val view = inflate(R.layout.header_dex_layout)
        view.text_last_update.text = presenter.prefsUtil.getValue(PrefsUtil.KEY_LAST_UPDATE_DEX_INFO, 0L)
                .currentDateAsTimeSpanString(activity!!)
        return view
    }

    private fun getEmptyView(): View {
        val view = inflate(R.layout.empty_dex_layout)
        view.button_add_markets.click {
            launchActivity<DexMarketsActivity>(REQUEST_SELECT_MARKETS)
        }
        view.button_add_markets.text = getString(R.string.dex_empty_button_text)
        view.dex_empty_title.text =
                getString(R.string.dex_empty_title)
        view.dex_empty_subtitle.text =
                getString(R.string.dex_empty_subtitle)
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
            REQUEST_SORTING, REQUEST_SELECT_MARKETS -> {
                if (resultCode == Constants.RESULT_OK) {
                    val needToUpdate = data?.getBooleanExtra(RESULT_NEED_UPDATE, false)
                    if (needToUpdate == true) {
                        presenter.loadActiveMarkets()
                    }
                }
            }
        }
    }

    override fun afterFailedLoadPairInfo() {
        swipe_container.isRefreshing = false
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (hidden) {
            presenter.clearOldPairsSubscriptions()
        } else {
            applyElevation()
            loadInfoForPairs()
        }
    }

    override fun onNetworkConnectionChanged(networkConnected: Boolean) {
        super.onNetworkConnectionChanged(networkConnected)
        val addMarketsItem = this.menu?.findItem(R.id.action_add_market)
        adapter.emptyView?.button_add_markets?.isEnabled = networkConnected
        addMarketsItem?.isEnabled = networkConnected
        if (networkConnected) {
            addMarketsItem?.icon?.alpha = 255 // 1.0
        } else {
            addMarketsItem?.icon?.alpha = 77 // 0.3
        }
    }

    private fun applyElevation() {
        onElevationAppBarChangeListener?.let {
            onElevationAppBarChangeListener?.onChange(presenter.hideShadow)
        }
    }

    companion object {
        const val RESULT_NEED_UPDATE = "need_update"
        const val REQUEST_SORTING = 121
        const val REQUEST_SELECT_MARKETS = 122

        fun newInstance(): DexFragment {
            return DexFragment()
        }
    }
}
