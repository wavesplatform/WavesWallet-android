package com.wavesplatform.wallet.v2.ui.home.wallet.leasing.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.home.wallet.assets.TestObject
import javax.inject.Inject

class AdapterActiveLeasing @Inject constructor() : BaseQuickAdapter<TestObject, BaseViewHolder>(R.layout.wallet_leasing_item, null) {

    override fun convert(helper: BaseViewHolder, item: TestObject) {

    }
}

