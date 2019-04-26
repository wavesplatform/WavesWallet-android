/*
 * Created by Ershov Aleksandr on 9/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.wallet.assets.search_asset

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.wavesplatform.sdk.net.model.response.AssetBalanceResponse
import com.wavesplatform.sdk.utils.getScaledAmount
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.home.wallet.assets.AssetsAdapter
import com.wavesplatform.wallet.v2.util.makeTextHalfBold
import kotlinx.android.synthetic.main.item_wallet_asset.view.*
import javax.inject.Inject

class SearchAssetAdapter  @Inject constructor() :
        BaseMultiItemQuickAdapter<MultiItemEntity, BaseViewHolder>(null) {

    init {
        addItemType(AssetsAdapter.TYPE_ASSET, R.layout.item_wallet_asset)
        addItemType(AssetsAdapter.TYPE_HIDDEN_ASSET, R.layout.item_wallet_asset)
        addItemType(AssetsAdapter.TYPE_HEADER, R.layout.hidden_header_item)
    }

    override fun convert(helper: BaseViewHolder, multiItemEntity: MultiItemEntity) {

        when (helper.itemViewType) {
            AssetsAdapter.TYPE_ASSET, AssetsAdapter.TYPE_HIDDEN_ASSET -> {
                val assetBalance = multiItemEntity as AssetBalanceResponse
                helper.setText(R.id.text_asset_name, assetBalance.getName())
                        .setText(R.id.text_asset_value, getScaledAmount(
                                assetBalance.getAvailableBalance(), assetBalance.getDecimals()))
                        .setGone(R.id.image_favourite, assetBalance.isFavorite)
                        .setGone(R.id.text_my_asset, assetBalance.issueTransaction?.sender
                                == App.getAccessManager().getWallet()?.address)
                        .setGone(R.id.text_tag_spam, assetBalance.isSpam)

                helper.itemView.image_asset_icon.setAsset(assetBalance)
                helper.itemView.text_asset_value.makeTextHalfBold()
            }

            AssetsAdapter.TYPE_HEADER -> {
                // do nothing
            }
        }
    }
}