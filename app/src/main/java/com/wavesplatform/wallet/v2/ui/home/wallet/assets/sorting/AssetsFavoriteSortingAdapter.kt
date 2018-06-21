package com.wavesplatform.wallet.v2.ui.home.wallet.assets.sorting

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import kotlinx.android.synthetic.main.wallet_asset_sorting_favorite_item.view.*
import javax.inject.Inject

class AssetsFavoriteSortingAdapter @Inject constructor() : BaseQuickAdapter<AssetBalance, BaseViewHolder>(R.layout.wallet_asset_sorting_favorite_item,null) {

    override fun convert(helper: BaseViewHolder, item: AssetBalance) {
        helper.setText(R.id.text_asset_name, item.getName())
                .addOnClickListener(R.id.image_favorite)

        helper.itemView.image_asset_icon.isOval = true
        helper.itemView.image_asset_icon.setAsset(item)
    }
}
