package com.wavesplatform.wallet.v2.ui.widget.adapters

import android.support.v7.widget.AppCompatCheckBox
import android.support.v7.widget.AppCompatTextView
import android.view.View
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.wavesplatform.sdk.model.response.data.AssetInfoResponse
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.custom.AssetAvatarView
import pers.victor.ext.click
import javax.inject.Inject

class AssetsAdapter @Inject constructor() : BaseQuickAdapter<AssetInfoResponse,
        BaseViewHolder>(R.layout.bottom_sheet_dialog_search_asset_item, null) {

    var allData: MutableList<AssetInfoResponse> = arrayListOf()
    var chosenAssets = arrayListOf<String>()

    override fun convert(helper: BaseViewHolder, item: AssetInfoResponse) {

        val assetTitle = helper.getView<AppCompatTextView>(R.id.asset_title)
        assetTitle.text = item.name

        val assetIcon = helper.getView<AssetAvatarView>(R.id.asset_icon)
        assetIcon.setAsset(item)

        val checkBox = helper.getView<AppCompatCheckBox>(R.id.asset_checkbox)

        val assetRoot = helper.getView<View>(R.id.asset_root)
        assetRoot.click {
            if (checkBox.isChecked) {
                checkBox.isChecked = false
                chosenAssets.remove(item.id)
            } else {
                checkBox.isChecked = true
                chosenAssets.add(item.id)
                if (chosenAssets.size > 10) {
                    chosenAssets.removeAt(0)
                }
            }
        }

        val asset = chosenAssets.firstOrNull { it == item.id }
        if (asset != null) {
            checkBox.isChecked = true
        }
    }
}