package com.wavesplatform.wallet.v2.ui.auth.choose_account

import android.graphics.Color
import android.widget.ImageView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.daimajia.swipe.SwipeLayout
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.custom.Identicon
import com.wavesplatform.wallet.v2.ui.home.profile.address_book.AddressBookUser
import pers.victor.ext.click
import pyxis.uzuki.live.richutilskt.utils.runDelayed
import javax.inject.Inject

class ChooseAccountAdapter @Inject constructor() : BaseQuickAdapter<AddressBookUser, BaseViewHolder>(R.layout.choose_address_layout, null) {

    var allData: MutableList<AddressBookUser> = arrayListOf()
    var chooseAccountOnClickListener: ChooseAccountOnClickListener? = null

    override fun convert(helper: BaseViewHolder, item: AddressBookUser) {
        helper.setText(R.id.text_address, item.address)
                .setText(R.id.text_name, item.name)
                .setImageBitmap(R.id.image_asset, Identicon.create(item.address,
                        Identicon.Options.Builder()
                                .setBlankColor(Color.WHITE)
                                .create()))

        val swipeLayout = helper.getView<SwipeLayout>(R.id.swipe_layout)
        swipeLayout.showMode = SwipeLayout.ShowMode.LayDown

        swipeLayout.surfaceView.setOnClickListener {
            chooseAccountOnClickListener?.onItemClicked(item)
        }

        helper.getView<ImageView>(R.id.image_edit_address).click {
            swipeLayout.close(true)
            runDelayed(250) {
                chooseAccountOnClickListener?.onEditClicked(data.indexOf(item))
            }
        }
        helper.getView<ImageView>(R.id.image_delete_address).click {
            swipeLayout.close(true)

            runDelayed(250) {
                chooseAccountOnClickListener?.onDeleteClicked(data.indexOf(item))
            }
        }
    }

}
