package com.wavesplatform.wallet.v2.ui.home.history.filter.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import kotlinx.android.synthetic.main.history_asset_item.view.*
import pers.victor.ext.gone
import pers.victor.ext.visiable
import javax.inject.Inject

class AssetsAdapter @Inject constructor() : BaseQuickAdapter<AssetBalance, BaseViewHolder>(R.layout.history_asset_item, null) {

    override fun convert(helper: BaseViewHolder, item: AssetBalance) {
        helper.setText(R.id.text_asset_name, item.getName())

        helper.itemView.image_asset_icon.setAsset(item)

        if (item.isChecked) {
            helper.itemView.container_checked.visiable()
        } else {
            helper.itemView.container_checked.gone()
        }
    }
}
