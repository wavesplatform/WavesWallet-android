package com.wavesplatform.wallet.v2.ui.home.wallet.leasing

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v1.util.MoneyUtil
import com.wavesplatform.wallet.v2.data.Events
import com.wavesplatform.wallet.v2.data.model.local.HistoryTab
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.data.model.remote.response.Transaction
import com.wavesplatform.wallet.v2.ui.base.view.BaseFragment
import com.wavesplatform.wallet.v2.ui.home.MainActivity
import com.wavesplatform.wallet.v2.ui.home.history.HistoryActivity
import com.wavesplatform.wallet.v2.ui.home.history.HistoryFragment
import com.wavesplatform.wallet.v2.ui.home.history.HistoryItem
import com.wavesplatform.wallet.v2.ui.home.history.details.HistoryDetailsBottomSheetFragment
import com.wavesplatform.wallet.v2.ui.home.history.tab.HistoryTabFragment
import com.wavesplatform.wallet.v2.ui.home.wallet.address.MyAddressQRActivity
import com.wavesplatform.wallet.v2.ui.home.wallet.leasing.start.StartLeasingActivity
import com.wavesplatform.wallet.v2.util.launchActivity
import com.wavesplatform.wallet.v2.util.makeTextHalfBold
import com.wavesplatform.wallet.v2.util.notNull
import kotlinx.android.synthetic.main.fragment_leasing.*
import pers.victor.ext.click
import pers.victor.ext.goneIf
import javax.inject.Inject

class LeasingFragment : BaseFragment(), LeasingView {

    @Inject
    @InjectPresenter
    lateinit var presenter: LeasingPresenter

    @ProvidePresenter
    fun providePresenter(): LeasingPresenter = presenter

    @Inject
    lateinit var adapterActiveAdapter: LeasingActiveAdapter
    var changeTabBarVisibilityListener: HistoryTabFragment.ChangeTabBarVisibilityListener? = null

    companion object {

        /**
         * @return LeasingFragment instance
         * */
        fun newInstance(): LeasingFragment {
            return LeasingFragment()
        }
    }

    override fun configLayoutRes(): Int = R.layout.fragment_leasing

    override fun onViewReady(savedInstanceState: Bundle?) {
        eventSubscriptions.add(rxEventBus.filteredObservable(Events.ScrollToTopEvent::class.java)
                .subscribe {
                    if (it.position == MainActivity.WALLET_SCREEN) {
//                        nested_scroll_view.smoothScrollTo(0, 0)
//                        changeTabBarVisibilityListener?.changeTabBarVisibility(true)
                    }
                })

        presenter.getActiveLeasing()

        card_view_history.click {
            launchActivity<HistoryActivity> {
                val bundle = Bundle().apply {
                    val tabs = arrayListOf(
                            HistoryTab(HistoryTabFragment.leasing_all, getString(R.string.history_all)),
                            HistoryTab(HistoryTabFragment.leasing_active_now, getString(R.string.history_active_now)),
                            HistoryTab(HistoryTabFragment.leasing_canceled, getString(R.string.history_canceled)))
                    putParcelableArrayList(HistoryFragment.BUNDLE_TABS, tabs)
                }
                putExtras(bundle)
            }
        }

        button_continue.click {
            launchActivity<StartLeasingActivity> { }
        }

        container_quick_note.click {
            if (expandable_layout_hidden.isExpanded) {
                expandable_layout_hidden.collapse()
                image_arrowup.animate()
                        .rotation(180f)
                        .setDuration(500)
                        .start()
            } else {
                expandable_layout_hidden.expand()
                image_arrowup.animate()
                        .rotation(0f)
                        .setDuration(500)
                        .start()
            }
        }

        text_active_leasing.click {
            if (expandable_layout_active_leasing.isExpanded) {
                expandable_layout_active_leasing.collapse()
                image_active_leasing.animate()
                        .rotation(180f)
                        .setDuration(500)
                        .start()
            } else {
                expandable_layout_active_leasing.expand()
                image_active_leasing.animate()
                        .rotation(0f)
                        .setDuration(500)
                        .start()
            }
        }

        recycle_active_leasing.layoutManager = LinearLayoutManager(baseActivity)
        recycle_active_leasing.adapter = adapterActiveAdapter
        recycle_active_leasing.isNestedScrollingEnabled = false

        adapterActiveAdapter.onItemClickListener = BaseQuickAdapter.OnItemClickListener { adapter, view, position ->
            val historyItem = adapter.getItem(position) as Transaction

            val bottomSheetFragment = HistoryDetailsBottomSheetFragment()
            bottomSheetFragment.selectedItem = historyItem
            bottomSheetFragment.allItems = adapter?.data as ArrayList<Transaction>

            bottomSheetFragment.selectedItemPosition = position
            bottomSheetFragment.show(fragmentManager, bottomSheetFragment.tag)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_leasing, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_your_address -> {
                launchActivity<MyAddressQRActivity>()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun showBalances(wavesAsset: AssetBalance, leasedValue: Long, availableValue: Long?) {
        text_available_balance.text = MoneyUtil.getScaledText(availableValue, wavesAsset)
        text_available_balance.makeTextHalfBold()
        text_leased.text = MoneyUtil.getScaledText(leasedValue, wavesAsset)
        text_total.text = wavesAsset.getDisplayBalance()
        wavesAsset.balance.notNull { wavesBalance ->
            if (wavesBalance != 0L) {
                progress_of_leasing.progress = ((leasedValue * 100) / wavesBalance).toInt()
            } else {
                progress_of_leasing.progress = 0
            }
        }
    }

    override fun showActiveLeasingTransaction(transactions: List<Transaction>) {
        linear_active_leasing.goneIf { transactions.isEmpty() }
        adapterActiveAdapter.setNewData(transactions)
        text_active_leasing.text = getString(R.string.wallet_leasing_active_now, transactions.size.toString())
    }
}
