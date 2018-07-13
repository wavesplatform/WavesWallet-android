package com.wavesplatform.wallet.v2.ui.home.profile.address_book

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.home.profile.address_book.AddressBookActivity.Companion.BUNDLE_TYPE
import com.wavesplatform.wallet.v2.ui.home.profile.address_book.AddressBookActivity.Companion.REQUEST_EDIT_ADDRESS
import com.wavesplatform.wallet.v2.ui.home.profile.address_book.AddressBookActivity.Companion.SCREEN_TYPE_EDITABLE
import com.wavesplatform.wallet.v2.ui.home.profile.address_book.edit.EditAddressActivity
import kotlinx.android.synthetic.main.address_book_item.view.*
import pers.victor.ext.gone
import pers.victor.ext.visiable
import javax.inject.Inject

class AddressBookAdapter @Inject constructor() : BaseQuickAdapter<AddressTestObject, BaseViewHolder>(R.layout.address_book_item, null) {

    var allData: MutableList<AddressTestObject> = arrayListOf()
    var screenType: Int = AddressBookActivity.AddressBookScreenType.EDIT.type

    override fun convert(helper: BaseViewHolder, item: AddressTestObject) {
        helper.setText(R.id.text_address, item.address)
                .setText(R.id.text_name, item.name)

        if (screenType == AddressBookActivity.AddressBookScreenType.EDIT.type){
            helper.itemView.checkbox_choose.gone()
            helper.itemView.image_edit.visiable()
        }else if(screenType == AddressBookActivity.AddressBookScreenType.CHOOSE.type){
            helper.itemView.checkbox_choose.visiable()
            helper.itemView.image_edit.gone()
        }
    }


    fun filter(text: String) {
        data.clear()
        if (text.trim().isEmpty()) {
            setNewData(ArrayList<AddressTestObject>(allData))
        } else {
            for (item in allData) {
                if (item.name.toLowerCase().contains(text.toLowerCase())) {
                    data.add(item)
                }
            }
        }
        notifyDataSetChanged()
    }


}