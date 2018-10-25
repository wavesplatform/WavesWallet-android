package com.wavesplatform.wallet.v2.ui.home.dex.markets

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.remote.response.Market
import javax.inject.Inject

class DexMarketsAdapter @Inject constructor() : BaseQuickAdapter<Market, BaseViewHolder>(R.layout.dex_markets_item, null) {

    var allData: MutableList<Market> = arrayListOf()

    override fun convert(helper: BaseViewHolder, item: Market) {
        helper.setText(R.id.text_name, "${item.amountAssetShortName} / ${item.priceAssetShortName}")
                .setText(R.id.text_full_name, "${item.amountAssetLongName} / ${item.priceAssetLongName}")
                .setChecked(R.id.checkbox_choose, item.checked)
                .addOnClickListener(R.id.image_info)
    }


    fun filter(text: String) {
        data.clear()
        if (text.trim().isEmpty()) {
            setNewData(ArrayList<Market>(allData))
        } else {
            for (item in allData) {
                val name = "${item.amountAssetName}/${item.priceAssetName}"
                if (name.toLowerCase().contains(text.toLowerCase())) {
                    data.add(item)
                }
            }
        }
        notifyDataSetChanged()
    }


}