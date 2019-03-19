package com.wavesplatform.wallet.v2.ui.home.quick_action.send.fee

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v1.util.MoneyUtil
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.model.local.SponsoredAssetItem
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.ui.base.view.BaseSuperBottomSheetDialogFragment
import kotlinx.android.synthetic.main.fragment_sponsored_fee_bottom_sheet_dialog.view.*
import pyxis.uzuki.live.richutilskt.utils.runDelayed
import javax.inject.Inject

class SponsoredFeeBottomSheetFragment : BaseSuperBottomSheetDialogFragment(), SponsoredFeeDetailsView {
    private var rootView: View? = null

    private var selectedAssetId: String? = null
    private var wavesFee: Long = Constants.WAVES_MIN_FEE

    var onSelectedAssetListener: SponsoredAssetSelectedListener? = null

    @Inject
    @InjectPresenter
    lateinit var presenter: SponsoredFeeDetailsPresenter

    @Inject
    lateinit var adapter: SponsoredFeeAdapter

    @ProvidePresenter
    fun providePresenter(): SponsoredFeeDetailsPresenter = presenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        rootView = inflater.inflate(R.layout.fragment_sponsored_fee_bottom_sheet_dialog, container, false)

        rootView?.recycle_sponsored_fee_assets?.layoutManager = LinearLayoutManager(requireActivity())
        rootView?.recycle_sponsored_fee_assets?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                rootView?.appbar_layout?.isSelected = rootView?.recycle_sponsored_fee_assets!!.canScrollVertically(-1)
            }
        })

        presenter.wavesFee = wavesFee
        adapter.currentAssetId = selectedAssetId

        adapter.bindToRecyclerView(rootView?.recycle_sponsored_fee_assets)
        adapter.onItemClickListener = BaseQuickAdapter.OnItemClickListener { adapter, view, position ->
            val item = this.adapter.getItem(position) as SponsoredAssetItem

            if (item.isActive) {
                val oldCheckedPosition = this.adapter.data.indexOfFirst { it.assetBalance.assetId == this.adapter.currentAssetId }

                this.adapter.currentAssetId = item.assetBalance.assetId
                onSelectedAssetListener?.onSelected(item.assetBalance, MoneyUtil.getUnscaledValue(item.fee, item.assetBalance))

                adapter.notifyItemChanged(oldCheckedPosition)
                adapter.notifyItemChanged(position)

                runDelayed(250) {
                    dialog?.dismiss()
                }
            }
        }

        presenter.loadSponsoredAssets {
            rootView?.image_loader?.hide()
            adapter.setNewData(it)
        }

        return rootView
    }

    fun configureData(selectedAssetId: String, wavesFee: Long) {
        this.wavesFee = wavesFee
        this.selectedAssetId = selectedAssetId
    }

    override fun onDestroyView() {
        rootView?.image_loader?.hide()
        super.onDestroyView()
    }

    interface SponsoredAssetSelectedListener {
        fun onSelected(asset: AssetBalance, fee: Long)
    }
}