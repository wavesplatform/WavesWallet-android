package com.wavesplatform.wallet.v2.ui.home.wallet.your_assets

import android.view.View
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.util.notNull
import kotlinx.android.synthetic.main.your_assets_item.view.*
import javax.inject.Inject

class YourAssetsAdapter @Inject constructor() : BaseQuickAdapter<AssetBalance, BaseViewHolder>(R.layout.your_assets_item, null) {

    var allData: MutableList<AssetBalance> = arrayListOf()
    var currentAssetId: String? = null

    override fun convert(helper: BaseViewHolder, item: AssetBalance) {
        helper.setText(R.id.text_asset_name, item.getName())
                .setText(R.id.text_asset_value, item.getDisplayAvailableBalance())
                .setVisible(R.id.image_favourite, item.isFavorite)
        helper.itemView.text_asset_value.visibility =
                if (item.getAvailableBalance() == 0L) {
                    View.GONE
                } else {
                    View.VISIBLE
                }
        helper.itemView.image_asset_icon.setAsset(item)
        helper.itemView.checkbox_choose.isChecked = item.assetId == currentAssetId
    }

    fun filter(text: String) {
        data.clear()
        if (text.trim().isEmpty()) {
            setNewData(ArrayList<AssetBalance>(allData))
        } else {
            for (item in allData) {
                item.getName().notNull {
                    if (it.toLowerCase().contains(text.toLowerCase())) {
                        data.add(item)
                    }
                }
            }
        }
        notifyDataSetChanged()
    }
}