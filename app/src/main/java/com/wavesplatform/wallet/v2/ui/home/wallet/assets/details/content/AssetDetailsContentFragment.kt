package com.wavesplatform.wallet.v2.ui.home.wallet.assets.details.content


import android.os.Bundle
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.ui.base.view.BaseFragment
import com.wavesplatform.wallet.v2.ui.home.history.HistoryItem
import com.wavesplatform.wallet.v2.ui.home.wallet.assets.details.HistoryTransactionPagerAdapter
import com.wavesplatform.wallet.v2.ui.home.wallet.assets.token_burn.TokenBurnActivity
import com.wavesplatform.wallet.v2.util.copyToClipboard
import com.wavesplatform.wallet.v2.util.launchActivity
import com.wavesplatform.wallet.v2.util.notNull
import kotlinx.android.synthetic.main.fragment_asset_details_content.*
import pers.victor.ext.click
import pers.victor.ext.dp2px
import pyxis.uzuki.live.richutilskt.utils.runAsync
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


class AssetDetailsContentFragment : BaseFragment(), AssetDetailsContentView {

    @Inject
    @InjectPresenter
    lateinit var presenter: AssetDetailsContentPresenter

    @ProvidePresenter
    fun providePresenter(): AssetDetailsContentPresenter = presenter

    @Inject
    lateinit var historyAdapter: HistoryTransactionPagerAdapter
    var formatter: SimpleDateFormat = SimpleDateFormat("dd.MM.yyyy 'at' HH:mm")

    override fun configLayoutRes() = R.layout.fragment_asset_details_content

    companion object {
        var BUNDLE_ASSET = "asset"
    }


    override fun onViewReady(savedInstanceState: Bundle?) {
        presenter.assetBalance = arguments?.getParcelable<AssetBalance>(BUNDLE_ASSET)

        view_pager_transaction_history.adapter = historyAdapter
        view_pager_transaction_history.offscreenPageLimit = 3
        view_pager_transaction_history.clipToPadding = false
        view_pager_transaction_history.setPadding(dp2px(14), 0, dp2px(14), 0);
        view_pager_transaction_history.pageMargin = dp2px(7)

        image_copy_issuer.click {
            text_view_issuer_value.copyToClipboard(it)
        }

        image_copy_id.click {
            text_view_id_value.copyToClipboard(it)
        }

        card_burn.click {
            launchActivity<TokenBurnActivity> { }
        }

        fillInformation(presenter.assetBalance)


        runAsync({
            presenter.loadLastTransactions()
        })
    }

    override fun showLastTransactions(data: MutableList<HistoryItem>) {
        historyAdapter.items = data
        historyAdapter.notifyDataSetChanged()
    }

    private fun fillInformation(assetBalance: AssetBalance?) {
        text_view_asset_name_value.text = assetBalance?.getName()
        text_reusable_value.text =
                if (assetBalance?.reissuable == true) getString(R.string.asset_details_reissuable)
                else getString(R.string.asset_details_not_reissuable)
        text_description.text =
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

        if (assetBalance?.isWaves() == true) {

        } else if (assetBalance?.isGateway == true) {

        } else if (assetBalance?.isFlatMoney == true) {

        } else if (assetBalance?.isSpam == true) {

        } else {

        }
    }

}
