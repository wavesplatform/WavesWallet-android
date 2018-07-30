package com.wavesplatform.wallet.v2.ui.home.dex.trade.last_trades

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.remote.response.Market
import com.wavesplatform.wallet.v2.ui.home.history.TestObject
import com.wavesplatform.wallet.v2.util.random
import pers.victor.ext.findColor
import javax.inject.Inject

class TradeLastTradesAdapter @Inject constructor() : BaseQuickAdapter<TestObject, BaseViewHolder>(R.layout.recycle_item_last_trades, null) {

    override fun convert(helper: BaseViewHolder, item: TestObject) {
        if ((0..1).random() == 0) {
            helper.setTextColor(R.id.text_price_value, findColor(R.color.error400))
        }else{
            helper.setTextColor(R.id.text_price_value, findColor(R.color.submit400))
        }
    }
}