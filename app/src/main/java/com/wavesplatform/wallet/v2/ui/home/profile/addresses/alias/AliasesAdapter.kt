package com.wavesplatform.wallet.v2.ui.home.profile.addresses.alias

import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.AppCompatTextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.util.copyToClipboard
import pers.victor.ext.click
import javax.inject.Inject

class AliasesAdapter @Inject constructor() : BaseQuickAdapter<AliasModel, BaseViewHolder>(R.layout.aliases_layout) {

    override fun convert(helper: BaseViewHolder?, item: AliasModel?) {
        val textAliasName = helper?.getView<AppCompatTextView>(R.id.text_alias_name)
        val imageCopy = helper?.getView<AppCompatImageView>(R.id.image_copy)

        textAliasName?.text = item?.name
        imageCopy?.click {
            it.copyToClipboard(textAliasName?.text.toString(), R.drawable.ic_copy_18_submit_400)
        }
    }
}

