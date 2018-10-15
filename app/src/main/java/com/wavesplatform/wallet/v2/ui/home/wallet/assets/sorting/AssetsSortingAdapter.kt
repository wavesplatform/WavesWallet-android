package com.wavesplatform.wallet.v2.ui.home.wallet.assets.sorting

import com.chad.library.adapter.base.BaseItemDraggableAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import kotlinx.android.synthetic.main.wallet_asset_sorting_item.view.*
import pers.victor.ext.dp2px
import pers.victor.ext.findColor
import javax.inject.Inject

class AssetsSortingAdapter @Inject constructor() : BaseItemDraggableAdapter<AssetBalance, BaseViewHolder>(R.layout.wallet_asset_sorting_item, null) {
    var onHiddenChangeListener: OnHiddenChangeListener? = null

    override fun convert(helper: BaseViewHolder, item: AssetBalance) {
        helper.setText(R.id.text_asset_name, item.getName())
                .addOnClickListener(R.id.image_favorite)
                .setVisible(R.id.text_my_asset, item.issueTransaction?.sender == App.getAccessManager().getWallet()?.address)
                .setOnCheckedChangeListener(R.id.switch_visible, null) // fix bug with incorrect call listener
                .setChecked(R.id.switch_visible, !item.isHidden)
                .setOnCheckedChangeListener(R.id.switch_visible) { buttonView, isChecked ->
                    if (isChecked) {
                        helper.itemView.card_asset.setCardBackgroundColor(findColor(R.color.white))
                        helper.itemView.card_asset.cardElevation = dp2px(2).toFloat()
                    } else {
                        helper.itemView.card_asset.setCardBackgroundColor(findColor(android.R.color.transparent))
                        helper.itemView.card_asset.cardElevation = 0f
                    }
                    onHiddenChangeListener?.onHiddenStateChanged(item, isChecked)
                }
                .setGone(R.id.switch_visible, item.configureVisibleState)
                .setGone(R.id.image_down_arrow, item.isGateway && !item.isWaves())
                .setGone(R.id.image_drag, !item.configureVisibleState)

        if (item.isHidden) {
            helper.itemView.card_asset.cardElevation = 0f
            helper.itemView.card_asset.setCardBackgroundColor(findColor(android.R.color.transparent))
        }

        helper.itemView.image_asset_icon.isOval = true
        helper.itemView.image_asset_icon.setAsset(item)
    }

    interface OnHiddenChangeListener {
        fun onHiddenStateChanged(item: AssetBalance, checked: Boolean)
    }
}
