package com.wavesplatform.wallet.v2.ui.home.wallet.assets

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.helpers.PublicKeyAccountHelper
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import kotlinx.android.synthetic.main.wallet_asset_item.view.*
import javax.inject.Inject

class AssetsAdapter @Inject constructor(var publicKeyAccountHelper: PublicKeyAccountHelper) : BaseQuickAdapter<AssetBalance, BaseViewHolder>(R.layout.wallet_asset_item, null) {

    override fun convert(helper: BaseViewHolder, item: AssetBalance) {
        helper.setText(R.id.text_asset_name, item.getName())
                .setText(R.id.text_asset_value, item.getDisplayBalance())
                .setGone(R.id.image_favourite, item.isFavorite)
                .setGone(R.id.text_my_asset, item.issueTransaction?.sender == publicKeyAccountHelper.publicKeyAccount?.address)
                .setGone(R.id.image_down_arrow, item.isGateway)
                .setGone(R.id.text_tag_spam, item.isSpam)
                .setGone(R.id.text_bitcoin_value, !item.isSpam)

        helper.itemView.image_asset_icon.isOval = true
        helper.itemView.image_asset_icon.setAsset(item)
    }
}
