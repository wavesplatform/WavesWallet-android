/*
 * Created by Eduard Zaydel on 2/10/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.auth.drawer

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.daimajia.swipe.SwipeLayout
import com.wavesplatform.sdk.utils.Identicon
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.db.userdb.AddressBookUserDb
import com.wavesplatform.wallet.v2.data.model.local.MigrateAccountItem
import com.wavesplatform.wallet.v2.data.model.local.widget.MyAccountItem
import pers.victor.ext.click
import pers.victor.ext.findColor
import pyxis.uzuki.live.richutilskt.utils.runDelayed
import javax.inject.Inject

class MyAccountsAdapter @Inject constructor() : BaseMultiItemQuickAdapter<MyAccountItem, BaseViewHolder>(null) {

    private val identicon = Identicon()
    var chooseAccountOnClickListener: MyAccountOnClickListener? = null

    init {
        addItemType(TYPE_HEADER, R.layout.item_drawer_account_header)
        addItemType(TYPE_ACCOUNT, R.layout.item_drawer_account)
    }

    override fun convert(helper: BaseViewHolder, globalItem: MyAccountItem) {
        when (helper.itemViewType) {
            TYPE_HEADER -> {
                val item = globalItem.data as Int

                helper.setText(R.id.text_header, item)
            }
            TYPE_ACCOUNT -> {
                val item = globalItem.data as AddressBookUserDb

                helper.setText(R.id.text_address, item.address)
                        .setText(R.id.text_name, item.name)
                        .setGone(R.id.image_lock, globalItem.locked)
                        .setGone(R.id.image_active, globalItem.active)
                        .setBackgroundColor(R.id.relative_account,
                                if (globalItem.active) findColor(R.color.submit400_0_07)
                                else findColor(R.color.white))

                Glide.with(helper.itemView.context)
                        .load(identicon.create(item.address))
                        .apply(RequestOptions().circleCrop())
                        .into(helper.getView(R.id.image_asset))

                val swipeLayout = helper.getView<SwipeLayout>(R.id.swipe_layout)
                swipeLayout.showMode = SwipeLayout.ShowMode.LayDown

                swipeLayout.surfaceView.setOnClickListener {
                    chooseAccountOnClickListener?.onItemClicked(data.indexOf(globalItem), item)
                }

                helper.getView<ImageView>(R.id.image_edit_address).click {
                    swipeLayout.close(true)
                    runDelayed(250) {
                        chooseAccountOnClickListener?.onEditClicked(data.indexOf(globalItem), item)
                    }
                }
                helper.getView<ImageView>(R.id.image_delete_address).click {
                    swipeLayout.close(true)

                    runDelayed(250) {
                        chooseAccountOnClickListener?.onDeleteClicked(data.indexOf(globalItem), item)
                    }
                }
            }
        }
    }

    companion object {
        const val TYPE_HEADER = 0
        const val TYPE_ACCOUNT = 1
    }

    interface MyAccountOnClickListener {
        fun onEditClicked(position: Int, item: AddressBookUserDb)
        fun onDeleteClicked(position: Int, item: AddressBookUserDb)
        fun onItemClicked(position: Int, item: AddressBookUserDb)
    }
}
