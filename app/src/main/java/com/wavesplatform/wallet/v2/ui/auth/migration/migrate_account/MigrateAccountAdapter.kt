/*
 * Created by Eduard Zaydel on 30/9/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.auth.migration.migrate_account

import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.wavesplatform.sdk.utils.Identicon
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.db.userdb.AddressBookUserDb
import com.wavesplatform.wallet.v2.data.model.local.MigrateAccountItem
import javax.inject.Inject

class MigrateAccountAdapter @Inject constructor() : BaseMultiItemQuickAdapter<MigrateAccountItem, BaseViewHolder>(null) {

    private val identicon = Identicon()

    init {
        addItemType(TYPE_HEADER, R.layout.item_migrate_account_header)
        addItemType(TYPE_ACCOUNT, R.layout.item_migrate_account_account)
    }

    override fun convert(helper: BaseViewHolder, globalItem: MigrateAccountItem) {
        when (helper.itemViewType) {
            TYPE_HEADER -> {
                val item = globalItem.data as Int

                helper.setText(R.id.text_header, item)
            }
            TYPE_ACCOUNT -> {
                val item = globalItem.data as AddressBookUserDb

                helper.setText(R.id.text_address, item.address)
                        .setText(R.id.text_name, item.name)
                        .setImageResource(R.id.image_lock,
                                if (globalItem.locked) R.drawable.ic_draglock_22_disabled_400
                                else R.drawable.ic_verified_multy_22)

                Glide.with(helper.itemView.context)
                        .load(identicon.create(item.address))
                        .apply(RequestOptions().circleCrop())
                        .into(helper.getView(R.id.image_asset))
            }
        }
    }

    fun addUnlockedAccount(item: MigrateAccountItem, currentPosition: Int) {
        item.locked = false

        remove(currentPosition)
        addData(data.indexOfFirst { it.itemType == TYPE_HEADER } + 1, item)

        if (data.indexOfLast { it.itemType == TYPE_HEADER } == data.lastIndex) {
            remove(data.lastIndex)
        }
    }

    companion object {
        const val TYPE_HEADER = 0
        const val TYPE_ACCOUNT = 1
    }
}
