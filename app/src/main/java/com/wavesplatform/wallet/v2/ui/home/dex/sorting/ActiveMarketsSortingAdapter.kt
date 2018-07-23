package com.wavesplatform.wallet.v2.ui.home.dex.sorting

import com.chad.library.adapter.base.BaseItemDraggableAdapter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.helpers.PublicKeyAccountHelper
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.ui.home.wallet.assets.TestObject
import kotlinx.android.synthetic.main.wallet_asset_sorting_favorite_item.view.*
import pers.victor.ext.visiableIf
import javax.inject.Inject

class ActiveMarketsSortingAdapter @Inject constructor() : BaseItemDraggableAdapter<TestObject, BaseViewHolder>(R.layout.dex_active_markets_sorting_item,null) {

    override fun convert(helper: BaseViewHolder, item: TestObject) {
        helper
                .addOnClickListener(R.id.image_delete)
    }
}
