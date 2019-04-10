/*
 * Created by Ershov Aleksandr on 9/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.search_asset

import android.support.v7.widget.CardView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.util.getScaledAmount
import com.wavesplatform.wallet.v2.util.makeTextHalfBold
import com.wavesplatform.wallet.v2.util.setMargins
import kotlinx.android.synthetic.main.wallet_asset_item.view.*
import pers.victor.ext.dp2px
import javax.inject.Inject

class SearchAssetAdapter  @Inject constructor() :
        BaseQuickAdapter<MultiItemEntity, BaseViewHolder>(R.layout.wallet_asset_item, null) {


    override fun convert(helper: BaseViewHolder, item: MultiItemEntity) {

        if (helper.adapterPosition == 0) {
            helper.getView<CardView>(R.id.card_asset).setMargins(top = dp2px(16))
        } else {
            helper.getView<CardView>(R.id.card_asset).setMargins(top = dp2px(2))
        }

        val item = item as AssetBalance
        helper.setText(R.id.text_asset_name, item.getName())
                .setText(R.id.text_asset_value, getScaledAmount(
                        item.getAvailableBalance(), item.getDecimals()))
                .setGone(R.id.image_favourite, item.isFavorite)
                .setGone(R.id.text_my_asset, item.issueTransaction?.sender
                        == App.getAccessManager().getWallet()?.address)
                .setGone(R.id.text_tag_spam, item.isSpam)

        helper.itemView.image_asset_icon.setAsset(item)
        helper.itemView.text_asset_value.makeTextHalfBold()
    }
}