package com.wavesplatform.wallet.v2.ui.search_asset

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.ui.home.wallet.assets.AssetsAdapter
import com.wavesplatform.wallet.v2.util.getScaledAmount
import com.wavesplatform.wallet.v2.util.makeTextHalfBold
import com.wavesplatform.wallet.v2.util.setMargins
import kotlinx.android.synthetic.main.wallet_asset_item.view.*
import pers.victor.ext.dp2px
import javax.inject.Inject

class SearchAssetAdapter  @Inject constructor() :
        BaseQuickAdapter<MultiItemEntity, BaseViewHolder>(R.layout.wallet_asset_item, null) {


    override fun convert(helper: BaseViewHolder, item: MultiItemEntity) {

        try {
            if (data[helper.adapterPosition + 1].itemType == AssetsAdapter.TYPE_HEADER) {
                helper.itemView.card_asset.setMargins(bottom = dp2px(18))
            } else {
                helper.itemView.card_asset.setMargins(bottom = dp2px(6))
            }
        } catch (e: Throwable) {
            helper.itemView.card_asset.setMargins(bottom = dp2px(6))
            e.printStackTrace()
        }

        val item = item as AssetBalance
        helper.setText(R.id.text_asset_name, item.getName())
                .setText(R.id.text_asset_value, getScaledAmount(
                        item.getAvailableBalance() ?: 0L, item.getDecimals()))
                .setGone(R.id.image_favourite, item.isFavorite)
                .setGone(R.id.text_my_asset, item.issueTransaction?.sender
                        == App.getAccessManager().getWallet()?.address)
                .setGone(R.id.text_tag_spam, item.isSpam)

//                helper.itemView.image_asset_icon.isOval = true
        helper.itemView.image_asset_icon.setAsset(item)

        helper.itemView.text_asset_value.makeTextHalfBold()
    }
}