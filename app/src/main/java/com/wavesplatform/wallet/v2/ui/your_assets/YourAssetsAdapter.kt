package com.wavesplatform.wallet.v2.ui.your_assets

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.ui.home.profile.address_book.AddressBookActivity
import com.wavesplatform.wallet.v2.ui.home.profile.address_book.AddressBookActivity.Companion.BUNDLE_TYPE
import com.wavesplatform.wallet.v2.ui.home.profile.address_book.AddressBookActivity.Companion.REQUEST_EDIT_ADDRESS
import com.wavesplatform.wallet.v2.ui.home.profile.address_book.AddressBookActivity.Companion.SCREEN_TYPE_EDITABLE
import com.wavesplatform.wallet.v2.ui.home.profile.address_book.AddressTestObject
import com.wavesplatform.wallet.v2.ui.home.profile.address_book.edit.EditAddressActivity
import com.wavesplatform.wallet.v2.util.notNull
import kotlinx.android.synthetic.main.address_book_item.view.*
import kotlinx.android.synthetic.main.your_assets_item.view.*
import pers.victor.ext.gone
import pers.victor.ext.visiable
import javax.inject.Inject

class YourAssetsAdapter @Inject constructor() : BaseQuickAdapter<AssetBalance, BaseViewHolder>(R.layout.your_assets_item, null) {

    var allData: MutableList<AssetBalance> = arrayListOf()

    override fun convert(helper: BaseViewHolder, item: AssetBalance) {
        helper.setText(R.id.text_asset_name, item.getName())
                .setText(R.id.text_asset_value, item.getDisplayBalance())
                .setVisible(R.id.image_favourite, item.isFavorite)
//                .setGone(R.id.image_down_arrow, item.isOut)
//                .setVisible(R.id.text_tag_spam, item.isSpam)

        helper.itemView.image_asset_icon.isOval = true
        helper.itemView.image_asset_icon.setAsset(item)
    }


    fun filter(text: String) {
        data.clear()
        if (text.trim().isEmpty()) {
            setNewData(ArrayList<AssetBalance>(allData))
        } else {
            for (item in allData) {
                item.getName().notNull {
                    if (it.toLowerCase().contains(text.toLowerCase())) {
                        data.add(item)
                    }
                }
            }
        }
        notifyDataSetChanged()
    }


}