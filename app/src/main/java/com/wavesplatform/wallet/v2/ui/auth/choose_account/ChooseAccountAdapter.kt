/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.auth.choose_account

import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.wavesplatform.sdk.utils.Identicon
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.db.userdb.AddressBookUserDb
import javax.inject.Inject

class ChooseAccountAdapter @Inject constructor() : BaseQuickAdapter<AddressBookUserDb, BaseViewHolder>(R.layout.content_choose_address_layout, null) {

    private val identicon = Identicon()

    override fun convert(helper: BaseViewHolder, item: AddressBookUserDb) {
        helper.setText(R.id.text_address, item.address)
                .setText(R.id.text_name, item.name)

        Glide.with(helper.itemView.context)
                .load(identicon.create(item.address))
                .apply(RequestOptions().circleCrop())
                .into(helper.getView(R.id.image_asset))
    }
}
