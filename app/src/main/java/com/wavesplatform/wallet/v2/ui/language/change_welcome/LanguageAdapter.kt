/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.language.change_welcome

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.wavesplatform.wallet.R
import com.wavesplatform.sdk.model.LanguageItem
import kotlinx.android.synthetic.main.item_choose_language.view.*
import pers.victor.ext.dp2px
import pers.victor.ext.setPaddingEnd
import pers.victor.ext.setPaddingStart
import javax.inject.Inject

class LanguageAdapter @Inject constructor() : BaseQuickAdapter<LanguageItem, BaseViewHolder>(R.layout.item_choose_language_24dp, null) {

    var changeRootPadding = false

    override fun convert(helper: BaseViewHolder, item: LanguageItem) {
        if (changeRootPadding) {
            helper.itemView.root.setPaddingEnd(dp2px(16))
            helper.itemView.root.setPaddingStart(dp2px(16))
        }

        helper.setText(R.id.text_language, item.language.title)
                .setImageResource(R.id.image_flag, item.language.image)
                .setChecked(R.id.checkbox_choose, item.checked)
    }
}