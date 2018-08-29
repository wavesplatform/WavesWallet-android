package com.wavesplatform.wallet.v2.ui.home.wallet.leasing

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v1.util.MoneyUtil
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.data.model.remote.response.Transaction
import com.wavesplatform.wallet.v2.ui.base.view.BaseFragment
import com.wavesplatform.wallet.v2.ui.home.wallet.leasing.history.LeasingHistoryActivity
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

        presenter.getActiveLeasing()

        card_view_history.click {
            launchActivity<LeasingHistoryActivity> { }
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
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        val item = menu.findItem(R.id.action_sorting)
        item.isVisible = false
    }

    override fun showBalances(wavesAsset: AssetBalance, leasedValue: Long, availableValue: Long?) {
        text_available_balance.text = MoneyUtil.getScaledText(availableValue, wavesAsset)
        text_available_balance.makeTextHalfBold()
        text_leased.text = MoneyUtil.getScaledText(leasedValue, wavesAsset)
        text_total.text = wavesAsset.getDisplayBalance()
        wavesAsset.balance.notNull {
            progress_of_leasing.progress = ((leasedValue * 100) / it).toInt()
        }
    }

    override fun showActiveLeasingTransaction(transactions: List<Transaction>) {
        linear_active_leasing.goneIf { transactions.isEmpty() }
        adapterActiveAdapter.setNewData(transactions)
        text_active_leasing.text = getString(R.string.wallet_leasing_active_now, transactions.size.toString())
    }
}
