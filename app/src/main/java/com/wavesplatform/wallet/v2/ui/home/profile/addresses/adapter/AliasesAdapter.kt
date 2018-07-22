package com.wavesplatform.wallet.v2.ui.home.profile.addresses.adapter

import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.home.profile.addresses.AliasModel
import com.wavesplatform.wallet.v2.util.setMargins
import pers.victor.ext.dp2px
import javax.inject.Inject

class AliasesAdapter @Inject constructor() : BaseQuickAdapter<AliasModel, BaseViewHolder>(R.layout.aliases_layout) {

    override fun convert(helper: BaseViewHolder?, item: AliasModel?) {
        helper?.setText(R.id.text_aliases_name, item?.name)

        when {
            data.indexOf(item) == 0 -> helper?.getView<TextView>(R.id.text_aliases_name)?.setMargins(dp2px(16), right = dp2px(4))
            data.indexOf(item) == data.size - 1 -> helper?.getView<TextView>(R.id.text_aliases_name)?.setMargins(right = dp2px(16), left = dp2px(4))
            else -> helper?.getView<TextView>(R.id.text_aliases_name)?.setMargins(right = dp2px(4), left = dp2px(4))
        }
    }
}

