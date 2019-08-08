/*
 * Created by Eduard Zaydel on 7/8/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.widget.configuration.options

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.local.OptionsDialogItem
import com.wavesplatform.wallet.v2.data.model.local.OptionsDialogModel
import javax.inject.Inject

class OptionsAdapter<T : OptionsDialogModel> @Inject constructor() : BaseQuickAdapter<OptionsDialogItem<T>, BaseViewHolder>(R.layout.bottom_sheet_dialog_options_item, null) {

    override fun convert(helper: BaseViewHolder, item: OptionsDialogItem<T>) {
        helper.setText(R.id.option_title, item.data.itemTitle())
                .setChecked(R.id.option_checkbox, item.checked)
    }
}