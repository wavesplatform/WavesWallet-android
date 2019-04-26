/*
 * Created by Eduard Zaydel on 25/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.wallet.assets.sorting

import android.support.v7.widget.RecyclerView
import android.view.MotionEvent
import android.view.View
import com.chad.library.adapter.base.BaseMultiItemQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.v2.data.model.local.AssetSortingItem
import com.wavesplatform.wallet.v2.util.drag_helper.ItemDragListener
import com.wavesplatform.wallet.v2.util.drag_helper.ItemTouchHelperAdapter
import com.wavesplatform.wallet.v2.util.drag_helper.ItemTouchHelperViewHolder
import kotlinx.android.synthetic.main.wallet_asset_sorting_item.view.*
import pers.victor.ext.dp2px
import pers.victor.ext.findColor
import javax.inject.Inject


class AssetsSortingAdapter @Inject constructor() : BaseMultiItemQuickAdapter<AssetSortingItem, AssetsSortingAdapter.ItemViewHolder>(null),
        ItemTouchHelperAdapter {

    var mDragStartListener: ItemDragListener? = null

    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        val movedItem = data.removeAt(fromPosition)
        data.add(toPosition, movedItem)
        notifyItemMoved(fromPosition, toPosition)
        mDragStartListener?.onMoved(recyclerView.layoutManager?.findViewByPosition(fromPosition),
                fromPosition,
                recyclerView.layoutManager?.findViewByPosition(toPosition),
                toPosition)
        return true
    }

    fun resolveType(position: Int): Int {
        val favoriteLinePosition = data.indexOfFirst { it.itemType == AssetSortingItem.TYPE_LINE }
        val hiddenLinePosition = data.indexOfFirst { it.itemType == AssetSortingItem.TYPE_HIDDEN_HEADER }
        return when (position) {
            in 0..favoriteLinePosition -> {
                AssetSortingItem.TYPE_FAVORITE
            }
            in favoriteLinePosition..hiddenLinePosition -> {
                AssetSortingItem.TYPE_NOT_FAVORITE
            }
            in hiddenLinePosition until data.size -> {
                AssetSortingItem.TYPE_HIDDEN
            }
            else -> {
                AssetSortingItem.TYPE_FAVORITE
            }
        }
    }

    override fun onDragEnd(viewHolder: RecyclerView.ViewHolder) {
        mDragStartListener?.onEndDrag(viewHolder)
    }

    init {
        addItemType(AssetSortingItem.TYPE_FAVORITE, com.wavesplatform.wallet.R.layout.wallet_asset_sorting_item)
        addItemType(AssetSortingItem.TYPE_NOT_FAVORITE, com.wavesplatform.wallet.R.layout.wallet_asset_sorting_item)
        addItemType(AssetSortingItem.TYPE_HIDDEN, com.wavesplatform.wallet.R.layout.wallet_asset_sorting_item)
        addItemType(AssetSortingItem.TYPE_LINE, com.wavesplatform.wallet.R.layout.wallet_asset_sorting_line_item)
        addItemType(AssetSortingItem.TYPE_HIDDEN_HEADER, com.wavesplatform.wallet.R.layout.content_asset_sorting_hidden_header)
    }

    var onHiddenChangeListener: OnHiddenChangeListener? = null

    override fun convert(helper: ItemViewHolder, globalItem: AssetSortingItem) {
        when (helper.itemViewType) {
            AssetSortingItem.TYPE_NOT_FAVORITE, AssetSortingItem.TYPE_FAVORITE, AssetSortingItem.TYPE_HIDDEN -> {
                val item = globalItem.asset
                helper.setText(com.wavesplatform.wallet.R.id.text_asset_name, item.getName())
                        .setImageResource(com.wavesplatform.wallet.R.id.image_favorite, if (item.isFavorite) com.wavesplatform.wallet.R.drawable.ic_favorite_14_submit_300 else com.wavesplatform.wallet.R.drawable.ic_favorite_14_disabled_400)
                        .addOnClickListener(com.wavesplatform.wallet.R.id.image_favorite)
                        .setVisible(com.wavesplatform.wallet.R.id.text_my_asset, item.issueTransaction?.sender == App.getAccessManager().getWallet()?.address)
                        .setOnCheckedChangeListener(com.wavesplatform.wallet.R.id.switch_visible, null) // fix bug with incorrect call listener
                        .setChecked(com.wavesplatform.wallet.R.id.switch_visible, !item.isHidden)
                        .setOnCheckedChangeListener(com.wavesplatform.wallet.R.id.switch_visible) { buttonView, isChecked ->
                            if (isChecked) {
                                helper.itemView.card_asset.setCardBackgroundColor(findColor(com.wavesplatform.wallet.R.color.white))
                                helper.itemView.card_asset.cardElevation = dp2px(2).toFloat()
                            } else {
                                helper.itemView.card_asset.setCardBackgroundColor(findColor(android.R.color.transparent))
                                helper.itemView.card_asset.cardElevation = 0f
                            }
                            onHiddenChangeListener?.onHiddenStateChanged(globalItem, isChecked, helper.adapterPosition)
                        }
                        .setGone(com.wavesplatform.wallet.R.id.switch_visible, item.configureVisibleState)
                        .setGone(com.wavesplatform.wallet.R.id.image_drag, !item.configureVisibleState)

                helper.itemView.image_drag.setOnTouchListener { v, event ->
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            mDragStartListener?.onStartDrag(helper, helper.adapterPosition)
                        }
                    }
                    false
                }

                if (item.isHidden || item.isFavorite) {
                    helper.itemView.card_asset.cardElevation = 0f
                    helper.itemView.card_asset.setCardBackgroundColor(findColor(android.R.color.transparent))
                } else {
                    helper.itemView.card_asset.cardElevation = dp2px(2).toFloat()
                    helper.itemView.card_asset.setCardBackgroundColor(findColor(com.wavesplatform.wallet.R.color.white))
                }

                helper.itemView.image_asset_icon.setAsset(item)
            }
            AssetSortingItem.TYPE_LINE, AssetSortingItem.TYPE_HIDDEN_HEADER -> {
                // nothing
            }
        }
    }

    class ItemViewHolder(itemView: View) : BaseViewHolder(itemView), ItemTouchHelperViewHolder {
        override fun onItemSelected() {
            // change background to selected
        }

        override fun onItemClear() {
            // change background to default
        }
    }

    interface OnHiddenChangeListener {
        fun onHiddenStateChanged(item: AssetSortingItem, checked: Boolean, position: Int)
    }
}
