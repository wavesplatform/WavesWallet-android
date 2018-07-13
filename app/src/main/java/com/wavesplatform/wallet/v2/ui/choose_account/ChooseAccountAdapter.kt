package com.wavesplatform.wallet.v2.ui.choose_account

import android.widget.ImageView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.daimajia.swipe.SwipeLayout
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.home.profile.address_book.AddressTestObject
import com.wavesplatform.wallet.v2.util.notNull
import pers.victor.ext.click
import javax.inject.Inject

class ChooseAccountAdapter @Inject constructor() : BaseQuickAdapter<AddressTestObject, BaseViewHolder>(R.layout.choose_address_layout, null) {

    var allData: MutableList<AddressTestObject> = arrayListOf()
    var chooseAccountOnClickListener: ChooseAccountOnClickListener? = null

    override fun convert(helper: BaseViewHolder, item: AddressTestObject) {
        helper.setText(R.id.text_address, item.address)
                .setText(R.id.text_name, item.name)

        val swipeLayout = helper.getView<SwipeLayout>(R.id.swipe_layout)
        swipeLayout.showMode = SwipeLayout.ShowMode.LayDown

        helper.getView<ImageView>(R.id.image_edit_address).click {
            swipeLayout.close(true)

            chooseAccountOnClickListener?.onEditClicked(data.indexOf(item))
        }
        helper.getView<ImageView>(R.id.image_delete_address).click {
            swipeLayout.close(true)

            chooseAccountOnClickListener?.onDeleteClicked()
        }
    }

}
