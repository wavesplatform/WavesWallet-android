/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.wallet.assets.details.content

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.jakewharton.rxbinding3.view.clicks
import com.wavesplatform.sdk.model.response.node.AssetBalanceResponse
import com.wavesplatform.sdk.utils.MoneyUtil
import com.wavesplatform.sdk.utils.notNull
import com.wavesplatform.sdk.utils.stripZeros
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.Events
import com.wavesplatform.wallet.v2.data.analytics.AnalyticEvents
import com.wavesplatform.wallet.v2.data.analytics.analytics
import com.wavesplatform.wallet.v2.data.model.local.HistoryItem
import com.wavesplatform.wallet.v2.data.model.local.HistoryTab
import com.wavesplatform.wallet.v2.ui.base.view.BaseFragment
import com.wavesplatform.wallet.v2.ui.home.history.HistoryActivity
import com.wavesplatform.wallet.v2.ui.home.history.HistoryFragment
import com.wavesplatform.wallet.v2.ui.home.history.tab.HistoryTabFragment
import com.wavesplatform.wallet.v2.ui.home.quick_action.receive.ReceiveActivity
import com.wavesplatform.wallet.v2.ui.home.quick_action.send.SendActivity
import com.wavesplatform.wallet.v2.ui.home.wallet.assets.details.AssetDetailsActivity
import com.wavesplatform.wallet.v2.ui.home.wallet.assets.token_burn.TokenBurnActivity
import com.wavesplatform.wallet.v2.ui.home.wallet.assets.token_burn.confirmation.TokenBurnConfirmationActivity
import com.wavesplatform.wallet.v2.ui.home.wallet.your_assets.YourAssetsActivity
import com.wavesplatform.wallet.v2.util.copyToClipboard
import com.wavesplatform.wallet.v2.util.launchActivity
import com.wavesplatform.wallet.v2.util.makeTextHalfBold
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.fragment_asset_details_layout.*
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import pers.victor.ext.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AssetDetailsContentFragment : BaseFragment(), AssetDetailsContentView {

    @Inject
    @InjectPresenter
    lateinit var presenter: AssetDetailsContentPresenter

    @ProvidePresenter
    fun providePresenter(): AssetDetailsContentPresenter = presenter

    lateinit var historyAdapter: HistoryTransactionPagerAdapter
    private var formatter: SimpleDateFormat = SimpleDateFormat("dd.MM.yyyy 'at' HH:mm")

    override fun configLayoutRes() = R.layout.fragment_asset_details_layout

    override fun onViewReady(savedInstanceState: Bundle?) {
        presenter.assetBalance = arguments?.getParcelable(BUNDLE_ASSET)

        historyAdapter = HistoryTransactionPagerAdapter(childFragmentManager, presenter.prefsUtil)

        view_pager_transaction_history.adapter = historyAdapter
        view_pager_transaction_history.offscreenPageLimit = 3
        view_pager_transaction_history.clipToPadding = false
        view_pager_transaction_history.setPadding(dp2px(14), 0, dp2px(14), 0)
        view_pager_transaction_history.pageMargin = dp2px(7)

        eventSubscriptions.add(image_copy_issuer.clicks()
                .throttleFirst(1500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    image_copy_issuer.copyToClipboard(text_view_issuer_value.text.toString())
                })

        eventSubscriptions.add(image_copy_id.clicks()
                .throttleFirst(1500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    image_copy_id.copyToClipboard(text_view_id_value.text.toString())
                })

        receive.click {
            launchActivity<ReceiveActivity> {
                putExtra(YourAssetsActivity.BUNDLE_ASSET_ITEM, presenter.assetBalance)
            }
        }

        send.click {
            launchActivity<SendActivity> {
                putExtra(SendActivity.KEY_INTENT_ASSET_DETAILS, true)
                putExtra(YourAssetsActivity.BUNDLE_ASSET_ITEM, presenter.assetBalance)
            }
        }

        fillInformation(presenter.assetBalance)

        presenter.assetBalance.notNull {
            presenter.loadLastTransactionsFor(it, (activity as AssetDetailsActivity).getAllTransactions())
        }

        eventSubscriptions.add(rxEventBus.filteredObservable(Events.NeedUpdateHistoryScreen::class.java)
                .subscribe {
                    presenter.reloadAssetDetails()
                })

        eventSubscriptions.add(rxEventBus.filteredObservable(Events.UpdateAssetsDetailsHistory::class.java)
                .subscribe {
                    if (historyAdapter.items.isEmpty()) {
                        presenter.assetBalance.notNull {
                            presenter.loadLastTransactionsFor(it, (activity as AssetDetailsActivity).getAllTransactions())
                        }
                    }
                })
    }

    override fun onAssetAddressBalanceLoadSuccess(assetBalance: AssetBalanceResponse) {
        presenter.assetBalance = assetBalance
        fillInformation(assetBalance)
    }

    override fun showLastTransactions(data: MutableList<HistoryItem>) {

        if (data.isNotEmpty()) {
            // configure clickable card
            card_transaction.setCardBackgroundColor(findColor(R.color.white))
            card_transaction.cardElevation = dp2px(2).toFloat()
            relative_transaction.setBackgroundResource(0)
            text_view_history.setTextColor(findColor(R.color.black))
            card_transaction.click {
                launchActivity<HistoryActivity> {
                    val bundle = Bundle().apply {
                        putParcelable(HistoryFragment.BUNDLE_ASSET, presenter.assetBalance)
                        putParcelableArrayList(HistoryFragment.BUNDLE_TABS, configureTabsAccordingTo(presenter.assetBalance))
                    }
                    putExtras(bundle)
                }
            }

            historyAdapter.items = data
            historyAdapter.notifyDataSetChanged()
        } else {
            // configure not clickable card
            card_transaction.setCardBackgroundColor(findColor(android.R.color.transparent))
            card_transaction.cardElevation = 0f
            relative_transaction.setBackgroundResource(R.drawable.shape_rect_outline_basic300_transparent)
            text_view_history.setTextColor(findColor(R.color.accent100))
            text_last_transaction_title.text = getString(R.string.asset_details_last_transactions_empty)
            card_transaction.click {}
        }
    }

    private fun configureTabsAccordingTo(assetBalance: AssetBalanceResponse?): ArrayList<HistoryTab> {
        val tabs = arrayListOf<HistoryTab>()
        assetBalance?.let { asset ->
            tabs.add(HistoryTab(HistoryTabFragment.all, getString(R.string.history_all)))
            tabs.add(HistoryTab(HistoryTabFragment.send, getString(R.string.history_sent)))
            tabs.add(HistoryTab(HistoryTabFragment.received, getString(R.string.history_received)))
            tabs.add(HistoryTab(HistoryTabFragment.exchanged, getString(R.string.history_exchanged)))
            if (asset.isWaves()) {
                tabs.add(HistoryTab(HistoryTabFragment.leased, getString(R.string.history_leased)))
            } else {
                tabs.add(HistoryTab(HistoryTabFragment.issued, getString(R.string.history_issued)))
            }
        }
        return tabs
    }

    private fun fillInformation(assetBalance: AssetBalanceResponse?) {
        formatter.timeZone = TimeZone.getTimeZone("UTC")

        text_available_balance.text = assetBalance?.getDisplayAvailableBalance()
        text_in_order.text = assetBalance?.getDisplayInOrderBalance()
        text_leased.text = assetBalance?.getDisplayLeasedBalance()
        text_total.text = assetBalance?.getDisplayTotalBalance()

        frame_in_order.goneIf { assetBalance?.inOrderBalance == 0L }
        frame_leased.goneIf { assetBalance?.leasedBalance == 0L }
        frame_total.goneIf { text_total.text.toString().trim() == text_available_balance.text.toString().trim() }

        text_available_balance.makeTextHalfBold()

        text_view_asset_name_value.text = assetBalance?.getName()
        text_reusable_value.text =
                if (assetBalance?.reissuable == true) getString(R.string.asset_details_reissuable)
                else getString(R.string.asset_details_not_reissuable)

        text_description_value.text =
                if (assetBalance?.issueTransaction?.description.isNullOrEmpty()) getString(R.string.common_dash)
                else assetBalance?.issueTransaction?.description

        text_view_issuer_value.text =
                if (assetBalance?.issueTransaction?.sender.isNullOrEmpty()) getString(R.string.common_dash)
                else assetBalance?.issueTransaction?.sender

        text_view_id_value.text =
                if (assetBalance?.issueTransaction?.assetId.isNullOrEmpty()) getString(R.string.common_dash)
                else assetBalance?.issueTransaction?.assetId

        text_issue_date_value.text = getString(R.string.common_dash)
        assetBalance?.issueTransaction?.timestamp.notNull {
            text_issue_date_value.text = formatter.format(Date(it))
        }

        text_view_asset_decimals_value.text =
                if (assetBalance?.issueTransaction?.decimals == null) getString(R.string.common_dash)
                else assetBalance.issueTransaction?.decimals.toString()

        text_view_total_amount_value.text = getString(R.string.common_dash)
        assetBalance?.quantity.notNull {
            text_view_total_amount_value.text = MoneyUtil.getScaledText(it, assetBalance).stripZeros()
        }

        when {
            assetBalance?.isWaves() == true -> {
                relative_issuer.gone()
                text_view_issuer.gone()
                text_description.gone()
                text_description_value.gone()
            }
            assetBalance?.isSpam == true -> {
                linear_last_transactions.gone()
                linear_transfer_buttons.gone()
                linear_blocked_transfer_buttons.visiable()
                setBurnButton(spam_card_burn_container)
            }
            else -> {
                setBurnButton(card_burn_container)
            }
        }
    }

    private fun setBurnButton(cardBurnContainer: View) {
        cardBurnContainer.click {
            analytics.trackEvent(AnalyticEvents.BurnTokenTapEvent)
            launchActivity<TokenBurnActivity>(requestCode = TokenBurnActivity.REQUEST_BURN_CONFIRM) {
                putExtra(TokenBurnActivity.KEY_INTENT_ASSET_BALANCE, presenter.assetBalance)
            }
        }
        cardBurnContainer.visiable()
    }

    override fun onNetworkConnectionChanged(networkConnected: Boolean) {
        super.onNetworkConnectionChanged(networkConnected)
        if (networkConnected) {
            enableView(send)
            enableView(receive)
            enableView(exchange)
            enableView(card_burn_container)
            enableView(spam_card_burn_container)
            card_burn_container.isClickable = true
            spam_card_burn_container.isClickable = true
        } else {
            disableView(send)
            disableView(receive)
            disableView(exchange)
            disableView(card_burn_container)
            disableView(spam_card_burn_container)
            card_burn_container.isClickable = false
            spam_card_burn_container.isClickable = false
        }
    }

    private fun enableView(view: View) {
        view.isClickable = true
        view.alpha = Constants.View.ENABLE_VIEW
    }

    private fun disableView(view: View) {
        view.isClickable = false
        view.alpha = Constants.View.DISABLE_VIEW
    }

    override fun onDestroyView() {
        historyAdapter.items = emptyList()
        view_pager_transaction_history.adapter = null
        super.onDestroyView()
    }

    override fun onDestroy() {
        mvpDelegate.onDestroy()
        super.onDestroy()
    }

    companion object {
        var BUNDLE_ASSET = "asset"
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Constants.RESULT_OK) {
            if (requestCode == TokenBurnActivity.REQUEST_BURN_CONFIRM) {
                val totalBurn = data?.getBooleanExtra(TokenBurnConfirmationActivity.BUNDLE_TOTAL_BURN, false)
                        ?: false

                if (totalBurn) {
                    onBackPressed()
                } else {
                    presenter.reloadAssetDetails(3000)
                }
            }
        }
    }
}
