package com.wavesplatform.wallet.v2.ui.home.profile.address_book

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.wavesplatform.wallet.R
import javax.inject.Inject

class AddressBookAdapter @Inject constructor() : BaseQuickAdapter<AddressTestObject, BaseViewHolder>(R.layout.address_book_item, null) {

    var allData: MutableList<AddressTestObject> = arrayListOf()

    override fun convert(helper: BaseViewHolder, item: AddressTestObject) {
        helper.setText(R.id.text_address, item.address)
                .setText(R.id.text_name, item.name)
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