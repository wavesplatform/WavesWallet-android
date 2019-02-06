package com.wavesplatform.wallet.v2.ui.home.quick_action.send.fee

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import kotlinx.android.synthetic.main.your_assets_item.view.*
import javax.inject.Inject

class SponsoredFeeAdapter @Inject constructor() : BaseQuickAdapter<AssetBalance, BaseViewHolder>(R.layout.recycle_item_sponsored_fee, null) {

    var currentAssetId: String? = null

    override fun convert(helper: BaseViewHolder, item: AssetBalance) {
        helper.setText(R.id.text_asset_name, item.getName())
                .setChecked(R.id.checkbox_choose, item.assetId == currentAssetId)
                .setText(R.id.text_asset_value, "${item.getDisplayAvailableBalance()} ${item.getName()}")
//        helper.itemView.text_asset_value.visibility =
//                if ((item.getDisplayAvailableBalance()
//                                .clearBalance()
//                                .toDouble()) == 0.toDouble()) {
//                    View.GONE
//                } else {
//                    View.VISIBLE
//                }
        helper.itemView.image_asset_icon.isOval = true
        helper.itemView.image_asset_icon.setAsset(item)

        helper.itemView.checkbox_choose.isChecked = item.assetId == currentAssetId
    }
}