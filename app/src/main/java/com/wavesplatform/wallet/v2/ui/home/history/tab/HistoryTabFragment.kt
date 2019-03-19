package com.wavesplatform.wallet.v2.ui.home.history.tab

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.ethanhua.skeleton.RecyclerViewSkeletonScreen
import com.ethanhua.skeleton.Skeleton
import com.oushangfeng.pinnedsectionitemdecoration.PinnedHeaderItemDecoration
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.Events
import com.wavesplatform.wallet.v2.data.model.local.HistoryItem
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.ui.base.view.BaseFragment
import com.wavesplatform.wallet.v2.ui.custom.SpeedyLinearLayoutManager
import com.wavesplatform.wallet.v2.ui.home.MainActivity
import com.wavesplatform.wallet.v2.ui.home.history.HistoryFragment
import com.wavesplatform.wallet.v2.ui.home.history.details.HistoryDetailsBottomSheetFragment
import com.wavesplatform.wallet.v2.util.notNull
import kotlinx.android.synthetic.main.fragment_history_tab.*
import pers.victor.ext.inflate
import java.util.*
import javax.inject.Inject

class HistoryTabFragment : BaseFragment(), HistoryTabView {

    @Inject
    @InjectPresenter
    lateinit var presenter: HistoryTabPresenter

    @ProvidePresenter
    fun providePresenter(): HistoryTabPresenter = presenter

    @Inject
    lateinit var adapter: HistoryTabItemAdapter
    lateinit var layoutManager: LinearLayoutManager
    var changeTabBarVisibilityListener: ChangeTabBarVisibilityListener? = null
    private var skeletonScreen: RecyclerViewSkeletonScreen? = null
    private var skeletonShow = false
    private lateinit var headerItemDecoration: PinnedHeaderItemDecoration

    override fun configLayoutRes(): Int = R.layout.fragment_history_tab

    override fun onViewReady(savedInstanceState: Bundle?) {
        eventSubscriptions.add(rxEventBus.filteredObservable(Events.ScrollToTopEvent::class.java)
                .subscribe {
                    if (it.position == MainActivity.HISTORY_SCREEN) {
                        recycle_history.scrollToPosition(0)
                        changeTabBarVisibilityListener?.changeTabBarVisibility(true)
                    }
                })

        swipe_refresh.setColorSchemeResources(R.color.submit400)
        swipe_refresh.setOnRefreshListener { presenter.loadLastTransactions() }
        layoutManager = SpeedyLinearLayoutManager(baseActivity)
        recycle_history.layoutManager = layoutManager
        recycle_history.adapter = adapter

        presenter.type = arguments?.getString("type")
        presenter.assetBalance = arguments?.getParcelable(HistoryFragment.BUNDLE_ASSET)

        adapter.bindToRecyclerView(recycle_history)
        adapter.emptyView = inflate(R.layout.layout_empty_data)

        skeletonScreen = Skeleton.bind(recycle_history)
                .adapter(recycle_history.adapter)
                .shimmer(true)
                .count(5)
                .color(R.color.basic100)
                .load(R.layout.item_skeleton_wallet)
                .frozen(false)
                .show()
        skeletonShow = true

        // make skeleton as designed
        recycle_history.post {
            recycle_history.layoutManager?.findViewByPosition(1)?.alpha = 0.7f
            recycle_history.layoutManager?.findViewByPosition(2)?.alpha = 0.5f
            recycle_history.layoutManager?.findViewByPosition(3)?.alpha = 0.4f
            recycle_history.layoutManager?.findViewByPosition(4)?.alpha = 0.2f
        }

        eventSubscriptions.add(rxEventBus.filteredObservable(Events.NeedUpdateHistoryScreen::class.java)
                .subscribe {
                    presenter.loadTransactions()
                    swipe_refresh.isRefreshing = false
                })

        eventSubscriptions.add(rxEventBus.filteredObservable(Events.StopUpdateHistoryScreen::class.java)
                .subscribe {
                    swipe_refresh.isRefreshing = false
                    skeletonScreen.notNull { skeleton ->
                        if (skeletonShow) {
                            skeleton.hide()
                            skeletonShow = false
                        }
                    }
                })

        presenter.loadTransactions()

        adapter.setEmptyView(R.layout.layout_empty_data)
        adapter.onItemClickListener = BaseQuickAdapter.OnItemClickListener { adapter, view, position ->
            if (position == 0) return@OnItemClickListener // handle click on empty space

            val historyItem = adapter.getItem(position) as HistoryItem
            if (historyItem.header.isEmpty()) {
                val bottomSheetFragment = HistoryDetailsBottomSheetFragment()

                val data = adapter?.data as ArrayList<HistoryItem>

                var sectionSize = 1 // 1 because first is empty view
                for (i in 0..position) {
                    if (data[i].header.isNotEmpty()) sectionSize++
                }

                val selectedPositionWithoutHeaders = position - sectionSize

                bottomSheetFragment.configureData(historyItem.data, selectedPositionWithoutHeaders)
                bottomSheetFragment.show(fragmentManager, bottomSheetFragment.tag)
            }
        }

        headerItemDecoration = PinnedHeaderItemDecoration.Builder(HistoryItem.TYPE_HEADER)
                .disableHeaderClick(true).create()
        recycle_history.addItemDecoration(headerItemDecoration)
    }

    override fun afterSuccessLoadTransaction(data: ArrayList<HistoryItem>, type: String?) {
        adapter.setNewData(data)
        skeletonScreen.notNull {
            skeletonShow = false
            it.hide()
        }
        swipe_refresh.isRefreshing = false
    }

    override fun onShowError(res: Int) {
        swipe_refresh.isRefreshing = false
    }

    companion object {

        const val all = "All"
        const val send = "Sent"
        const val received = "Received"
        const val exchanged = "Exchanged"
        const val leased = "Leased"
        const val issued = "Issued"

        const val leasing_all = "Leasing All"
        const val leasing_active_now = "Active now"
        const val leasing_canceled = "Canceled"

        const val TYPE = "type"

        fun newInstance(type: String, asset: AssetBalance?): HistoryTabFragment {
            val historyDateItemFragment = HistoryTabFragment()
            val bundle = Bundle()
            bundle.putString(TYPE, type)
            bundle.putParcelable(HistoryFragment.BUNDLE_ASSET, asset)
            historyDateItemFragment.arguments = bundle
            return historyDateItemFragment
        }
    }

    interface ChangeTabBarVisibilityListener {
        fun changeTabBarVisibility(show: Boolean, onlyExpand: Boolean = false)
    }
}
