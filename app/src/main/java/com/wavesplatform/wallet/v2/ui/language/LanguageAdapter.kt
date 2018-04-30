package com.wavesplatform.wallet.v2.ui.language

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.local.Language
import javax.inject.Inject

class LanguageAdapter @Inject constructor() : BaseQuickAdapter<Language, BaseViewHolder>(R.layout.choose_language_item, null) {

    override fun convert(helper: BaseViewHolder, item: Language) {
        helper.setText(R.id.text_language, item.title)
                .setImageResource(R.id.image_flag, item.image)
                .setChecked(R.id.checkbox_choose, item.checked)
    }
}