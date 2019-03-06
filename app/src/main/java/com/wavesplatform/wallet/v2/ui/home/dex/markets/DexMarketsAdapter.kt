package com.wavesplatform.wallet.v2.ui.home.dex.markets

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.remote.response.MarketResponse
import javax.inject.Inject

class DexMarketsAdapter @Inject constructor() : BaseQuickAdapter<MarketResponse, BaseViewHolder>(R.layout.dex_markets_item, null) {

    var allData: MutableList<MarketResponse> = arrayListOf()

    override fun convert(helper: BaseViewHolder, item: MarketResponse) {
        helper.setText(R.id.text_name, "${item.amountAssetShortName} / ${item.priceAssetShortName}")
                .setText(R.id.text_full_name, "${item.amountAssetLongName} / ${item.priceAssetLongName}")
                .setChecked(R.id.checkbox_choose, item.checked)
                .addOnClickListener(R.id.image_info)
    }

    fun filter(text: String) {
        data.clear()
        if (text.trim().isEmpty()) {
            setNewData(ArrayList<MarketResponse>(allData))
        } else {
            for (item in allData) {
                val name = "${item.amountAssetShortName}/${item.priceAssetShortName} " +
                        "${item.amountAssetLongName}/${item.priceAssetLongName} " +
                        "${item.amountAsset}/${item.priceAsset}"
                if (name.toLowerCase().contains(text.toLowerCase())) {
                    data.add(item)
                }
            }
        }
        notifyDataSetChanged()
    }
}