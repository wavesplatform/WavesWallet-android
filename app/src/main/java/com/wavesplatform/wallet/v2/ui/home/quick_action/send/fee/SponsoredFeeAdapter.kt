package com.wavesplatform.wallet.v2.ui.home.quick_action.send.fee

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.local.SponsoredAssetItem
import com.wavesplatform.wallet.v2.util.makeBackgroundWithRippleEffect
import kotlinx.android.synthetic.main.recycle_item_sponsored_fee.view.*
import pers.victor.ext.findColor
import javax.inject.Inject

class SponsoredFeeAdapter @Inject constructor() : BaseQuickAdapter<SponsoredAssetItem, BaseViewHolder>(R.layout.recycle_item_sponsored_fee, null) {

    var currentAssetId: String? = null

    override fun convert(helper: BaseViewHolder, item: SponsoredAssetItem) {
        helper.setText(R.id.text_asset_name, item.assetBalance.getName())
                .setChecked(R.id.checkbox_choose, item.assetBalance.assetId == currentAssetId)
                .setText(R.id.text_asset_value,
                        if (item.isActive) "${item.fee} ${item.assetBalance.getName()}"
                        else mContext.getString(R.string.sponsored_fee_dialog_not_available))
                .setGone(R.id.checkbox_choose, item.isActive)
                .setTextColor(R.id.text_asset_name,
                        if (item.isActive) findColor(R.color.disabled900)
                        else findColor(R.color.basic500))
                .setAlpha(R.id.image_asset_icon,
                        if (item.isActive) 1.0f
                        else 0.3f)

        if (item.isActive) helper.itemView.root.makeBackgroundWithRippleEffect()
        else helper.itemView.root.background = null

        helper.itemView.image_asset_icon.setAsset(item.assetBalance)

        helper.itemView.checkbox_choose.isChecked = item.assetBalance.assetId == currentAssetId
    }
}