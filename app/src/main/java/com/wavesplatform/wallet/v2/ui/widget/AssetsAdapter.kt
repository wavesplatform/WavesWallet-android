package com.wavesplatform.wallet.v2.ui.widget

import android.support.v7.widget.AppCompatCheckBox
import android.support.v7.widget.AppCompatTextView
import android.support.v7.widget.RecyclerView
import android.view.View
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.wavesplatform.sdk.model.response.data.AssetInfoResponse
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.custom.AssetAvatarView
import com.wavesplatform.wallet.v2.util.drag_helper.ItemDragListener
import com.wavesplatform.wallet.v2.util.drag_helper.ItemTouchHelperAdapter
import com.wavesplatform.wallet.v2.util.drag_helper.ItemTouchHelperViewHolder
import pers.victor.ext.click
import javax.inject.Inject

class AssetsAdapter @Inject constructor() : BaseMultiItemQuickAdapter<AssetsAdapter.AssetInfoMultiItemEntity, AssetsAdapter.ItemViewHolder>(null),
        ItemTouchHelperAdapter {

    val chosenAssets = mutableListOf<AssetInfoMultiItemEntity>()

    var dragStartListener: ItemDragListener? = null

    init {
        addItemType(TYPE_ASSET, R.layout.bottom_sheet_dialog_search_asset_item)
    }


    override fun convert(helper: ItemViewHolder, item: AssetInfoMultiItemEntity) {

        val assetTitle = helper.getView<AppCompatTextView>(R.id.asset_title)
        assetTitle.text = item.name

        val assetIcon = helper.getView<AssetAvatarView>(R.id.asset_icon)
        assetIcon.setAsset(item)

        val checkBox = helper.getView<AppCompatCheckBox>(R.id.asset_checkbox)

        val assetRoot = helper.getView<View>(R.id.asset_root)
        assetRoot.click {
            if (checkBox.isChecked) {
                checkBox.isChecked = false
                chosenAssets.remove(item)
            } else {
                checkBox.isChecked = true
                chosenAssets.add(item)
                if (chosenAssets.size > 10) {
                    chosenAssets.removeAt(0)
                }
            }
        }

        val asset = chosenAssets.firstOrNull { it.id == item.id }
        if (asset != null) {
            checkBox.isChecked = true
        }
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        val movedItem = data.removeAt(fromPosition)
        data.add(toPosition, movedItem)
        notifyItemMoved(fromPosition, toPosition)
        dragStartListener?.onMoved(recyclerView.findViewHolderForAdapterPosition(fromPosition),
                fromPosition,
                recyclerView.findViewHolderForAdapterPosition(toPosition),
                toPosition)
        return true
    }

    override fun onDragEnd(viewHolder: RecyclerView.ViewHolder) {
        dragStartListener?.onEndDrag(viewHolder)
    }

    var allData: MutableList<AssetInfoMultiItemEntity> = arrayListOf()


    fun filter(text: String) {
        data.clear()
        if (text.trim().isEmpty()) {
            setNewData(ArrayList(allData))
        } else {
            for (item in allData) {
                val name = item.name
                if (name.toLowerCase().contains(text.toLowerCase())) {
                    data.add(item)
                }
            }
        }
        notifyDataSetChanged()
    }

    class ItemViewHolder(itemView: View) : BaseViewHolder(itemView), ItemTouchHelperViewHolder {
        override fun onItemSelected() {
            // change background to selected
        }

        override fun onItemClear() {
            // change background to default
        }
    }


    class AssetInfoMultiItemEntity(assetInfo: AssetInfoResponse) : AssetInfoResponse(), MultiItemEntity {

        init {
            ticker = assetInfo.ticker
            id = assetInfo.id
            name = assetInfo.name
            precision = assetInfo.precision
            description = assetInfo.description
            height = assetInfo.height
            timestamp = assetInfo.timestamp
            sender = assetInfo.sender
            quantity = assetInfo.quantity
            hasScript = assetInfo.hasScript
            minSponsoredFee = assetInfo.minSponsoredFee
            reissuable = assetInfo.reissuable
            isSpam = assetInfo.isSpam
        }

        override fun getItemType(): Int {
            return TYPE_ASSET
        }

    }

    companion object {

        const val TYPE_ASSET = 1

        fun convert(list: List<AssetInfoResponse>): ArrayList<AssetInfoMultiItemEntity> {
            val multiItemEntityList = arrayListOf<AssetInfoMultiItemEntity>()
            list.forEach {
                multiItemEntityList.add(AssetInfoMultiItemEntity(it))
            }
            return multiItemEntityList
        }
    }
}