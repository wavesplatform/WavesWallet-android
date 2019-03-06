package com.wavesplatform.wallet.v2.ui.home.wallet.assets.sorting

import android.view.MotionEvent
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.local.AssetSortingItem
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.util.drag_helper.ItemDragListener
import com.wavesplatform.wallet.v2.util.drag_helper.ItemTouchHelperAdapter
import kotlinx.android.synthetic.main.wallet_asset_sorting_favorite_item.view.*
import kotlinx.android.synthetic.main.wallet_asset_sorting_item.view.*
import pers.victor.ext.dp2px
import pers.victor.ext.findColor
import javax.inject.Inject

class AssetsSortingAdapter @Inject constructor() : BaseMultiItemQuickAdapter<AssetSortingItem, BaseViewHolder>(null),
        ItemTouchHelperAdapter {

    var mDragStartListener: ItemDragListener? = null

    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        val movedItem = data.removeAt(fromPosition)
        data.add(toPosition, movedItem)
        notifyItemMoved(fromPosition, toPosition)
        mDragStartListener?.onMoved(recyclerView.layoutManager?.findViewByPosition(fromPosition), fromPosition, recyclerView.layoutManager?.findViewByPosition(toPosition), toPosition)
        return true
    }

    override fun onDragEnd() {
        mDragStartListener?.onEndDrag()
    }

    init {
        addItemType(AssetSortingItem.TYPE_FAVORITE, R.layout.wallet_asset_sorting_favorite_item)
        addItemType(AssetSortingItem.TYPE_NOT_FAVORITE, R.layout.wallet_asset_sorting_item)
        addItemType(AssetSortingItem.TYPE_LINE, R.layout.wallet_asset_sorting_line_item)
    }

    var onHiddenChangeListener: OnHiddenChangeListener? = null

    override fun convert(helper: BaseViewHolder, item: AssetSortingItem) {
        when (helper.itemViewType) {
            AssetSortingItem.TYPE_FAVORITE -> {
                val item = item.asset
                helper.setText(R.id.text_asset_name, item.getName())
                        .addOnClickListener(R.id.image_favorite)
                        .setVisible(R.id.text_my_asset, item.issueTransaction?.sender == App.getAccessManager().getWallet()?.address)

                helper.itemView.image_asset_favorite_icon.setAsset(item)
            }
            AssetSortingItem.TYPE_NOT_FAVORITE -> {
                val item = item.asset
                helper.setText(R.id.text_asset_name, item.getName())
                        .addOnClickListener(R.id.image_favorite)
                        .setVisible(R.id.text_my_asset, item.issueTransaction?.sender == App.getAccessManager().getWallet()?.address)
                        .setOnCheckedChangeListener(R.id.switch_visible, null) // fix bug with incorrect call listener
                        .setChecked(R.id.switch_visible, !item.isHidden)
                        .setOnCheckedChangeListener(R.id.switch_visible) { buttonView, isChecked ->
                            if (isChecked) {
                                helper.itemView.card_asset.setCardBackgroundColor(findColor(R.color.white))
                                helper.itemView.card_asset.cardElevation = dp2px(2).toFloat()
                            } else {
                                helper.itemView.card_asset.setCardBackgroundColor(findColor(android.R.color.transparent))
                                helper.itemView.card_asset.cardElevation = 0f
                            }
                            onHiddenChangeListener?.onHiddenStateChanged(item, isChecked)
                        }
                        .setGone(R.id.switch_visible, item.configureVisibleState)
                        .setGone(R.id.image_drag, !item.configureVisibleState)

                helper.itemView.image_drag.setOnTouchListener { v, event ->
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            mDragStartListener?.onStartDrag(helper, helper.adapterPosition)
                        }
                    }
                    false
                }

                if (item.isHidden) {
                    helper.itemView.card_asset.cardElevation = 0f
                    helper.itemView.card_asset.setCardBackgroundColor(findColor(android.R.color.transparent))
                } else {
                    helper.itemView.card_asset.cardElevation = dp2px(2).toFloat()
                    helper.itemView.card_asset.setCardBackgroundColor(findColor(R.color.white))
                }

                helper.itemView.image_asset_icon.setAsset(item)
            }
            AssetSortingItem.TYPE_LINE -> {
                // nothing
            }
        }
    }

    interface OnHiddenChangeListener {
        fun onHiddenStateChanged(item: AssetBalance, checked: Boolean)
    }
}
