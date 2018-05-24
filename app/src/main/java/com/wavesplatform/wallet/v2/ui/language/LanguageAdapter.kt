package com.wavesplatform.wallet.v2.ui.language

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.local.Language
import com.wavesplatform.wallet.v2.data.model.local.LanguageItem
import javax.inject.Inject

class LanguageAdapter @Inject constructor() : BaseQuickAdapter<LanguageItem, BaseViewHolder>(R.layout.choose_language_item, null) {

    override fun convert(helper: BaseViewHolder, item: LanguageItem) {
        helper.setText(R.id.text_language, item.language.title)
                .setImageResource(R.id.image_flag, item.language.image)
                .setChecked(R.id.checkbox_choose, item.checked)
    }
}