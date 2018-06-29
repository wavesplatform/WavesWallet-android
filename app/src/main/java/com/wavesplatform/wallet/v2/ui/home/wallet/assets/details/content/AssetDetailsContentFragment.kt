package com.wavesplatform.wallet.v2.ui.home.wallet.assets.details.content


import android.content.ClipData
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.AppCompatImageView
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.ui.base.view.BaseFragment
import com.wavesplatform.wallet.v2.ui.home.history.adapter.HistoryItem
import com.wavesplatform.wallet.v2.ui.home.wallet.assets.details.HistoryTransactionPagerAdapter
import com.wavesplatform.wallet.v2.util.notNull
import kotlinx.android.synthetic.main.fragment_asset_details_content.*
import pers.victor.ext.click
import pers.victor.ext.clipboardManager
import pers.victor.ext.dp2px
import pers.victor.ext.toast
import pyxis.uzuki.live.richutilskt.utils.runDelayed
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
        presenter.loadBundle()
    }

    override fun showData(data: ArrayList<HistoryItem>) {
        historyAdapter.items = data
        view_pager_transaction_history.adapter = historyAdapter
        view_pager_transaction_history.offscreenPageLimit = 3
        view_pager_transaction_history.clipToPadding = false
        view_pager_transaction_history.setPadding(dp2px(14), 0, dp2px(14), 0);
        view_pager_transaction_history.pageMargin = dp2px(7)
        historyAdapter.notifyDataSetChanged()

        image_copy_issuer.click {
            copyToClipboard(it, text_view_id_value.text)
        }

        image_copy_id.click {
            copyToClipboard(it, text_view_id_value.text)
        }

        presenter.assetBalance = arguments?.getParcelable<AssetBalance>(BUNDLE_ASSET)

        fillInformation(presenter.assetBalance)
    }

    private fun fillInformation(assetBalance: AssetBalance?) {
        text_view_asset_name_value.text = assetBalance?.getName()
        text_view_issuer_value.text = assetBalance?.issueTransaction?.sender
        text_view_id_value.text = assetBalance?.issueTransaction?.assetId
        text_reusable_value.text = if (assetBalance?.reissuable!!) getString(R.string.asset_details_reissuable) else getString(R.string.asset_details_not_reissuable)
        text_issue_date_value.text = formatter.format(Date(assetBalance.issueTransaction?.timestamp!!))
        text_description.text = assetBalance.issueTransaction?.description
    }

    private fun copyToClipboard(imageView: AppCompatImageView, text: CharSequence) {
        clipboardManager.primaryClip = ClipData.newPlainText(getString(R.string.app_name), text)
        activity.notNull { imageView.setImageDrawable(ContextCompat.getDrawable(it, R.drawable.ic_check_18_success_400)) }
        toast(getString(R.string.copied))
        runDelayed(2000, {
            activity.notNull { imageView.setImageDrawable(ContextCompat.getDrawable(it, R.drawable.ic_copy_18_black)) }
        })
    }

}
