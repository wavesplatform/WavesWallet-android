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
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.local.AssetSortingItem
import com.wavesplatform.wallet.v2.util.drag_helper.ItemDragListener
import com.wavesplatform.wallet.v2.util.drag_helper.ItemTouchHelperAdapter
import kotlinx.android.synthetic.main.item_wallet_asset_sorting.view.*
import com.wavesplatform.wallet.v2.util.drag_helper.ItemTouchHelperViewHolder
import pers.victor.ext.dp2px
import pers.victor.ext.findColor
import javax.inject.Inject


class AssetsSortingAdapter @Inject constructor() : BaseMultiItemQuickAdapter<AssetSortingItem, AssetsSortingAdapter.ItemViewHolder>(null),
        ItemTouchHelperAdapter {

    var dragStartListener: ItemDragListener? = null

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

    fun resolveType(position: Int): Int {
        val favoriteLinePosition = data.indexOfFirst { it.itemType == AssetSortingItem.TYPE_FAVORITE_SEPARATOR }
        val hiddenLinePosition = data.indexOfFirst { it.itemType == AssetSortingItem.TYPE_HIDDEN_HEADER }
        return when (position) {
            in 0..favoriteLinePosition -> {
                AssetSortingItem.TYPE_FAVORITE_ITEM
            }
            in favoriteLinePosition..hiddenLinePosition -> {
                AssetSortingItem.TYPE_DEFAULT_ITEM
            }
            in hiddenLinePosition until data.size -> {
                AssetSortingItem.TYPE_HIDDEN_ITEM
            }
            else -> {
                AssetSortingItem.TYPE_FAVORITE_ITEM
            }
        }
    }

    override fun onDragEnd(viewHolder: RecyclerView.ViewHolder) {
        dragStartListener?.onEndDrag(viewHolder)
    }

    init {
        // items
        addItemType(AssetSortingItem.TYPE_FAVORITE_ITEM, R.layout.item_wallet_asset_sorting)
        addItemType(AssetSortingItem.TYPE_DEFAULT_ITEM, R.layout.item_wallet_asset_sorting)
        addItemType(AssetSortingItem.TYPE_HIDDEN_ITEM, R.layout.item_wallet_asset_sorting)
        // separators
        addItemType(AssetSortingItem.TYPE_FAVORITE_SEPARATOR, R.layout.item_wallet_asset_sorting_line)
        addItemType(AssetSortingItem.TYPE_HIDDEN_HEADER, R.layout.content_asset_sorting_hidden_header)
        //empty
        addItemType(AssetSortingItem.TYPE_EMPTY_HIDDEN, R.layout.content_asset_sorting_empty)
        addItemType(AssetSortingItem.TYPE_EMPTY_DEFAULT, R.layout.content_asset_sorting_empty)
        addItemType(AssetSortingItem.TYPE_EMPTY_FAVORITE, R.layout.content_asset_sorting_empty)
    }

    var onHiddenChangeListener: OnHiddenChangeListener? = null

    override fun convert(helper: ItemViewHolder, globalItem: AssetSortingItem) {
        when (helper.itemViewType) {
            AssetSortingItem.TYPE_DEFAULT_ITEM, AssetSortingItem.TYPE_FAVORITE_ITEM, AssetSortingItem.TYPE_HIDDEN_ITEM -> {
                val item = globalItem.asset
                helper.setText(R.id.text_asset_name, item.getName())
                        .setImageResource(R.id.image_favorite, if (item.isFavorite) R.drawable.ic_favorite_14_submit_300 else R.drawable.ic_favorite_14_disabled_400)
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
                            onHiddenChangeListener?.onHiddenStateChanged(globalItem, isChecked, helper.adapterPosition)
                        }
                        .setGone(R.id.switch_visible, item.configureVisibleState)
                        .setGone(R.id.image_drag, !item.configureVisibleState)

                helper.itemView.image_drag.setOnTouchListener { v, event ->
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            dragStartListener?.onStartDrag(helper, helper.adapterPosition)
                        }
                    }
                    false
                }

                if (item.isHidden || item.isFavorite) {
                    helper.itemView.card_asset.cardElevation = 0f
                    helper.itemView.card_asset.setCardBackgroundColor(findColor(android.R.color.transparent))
                } else {
                    helper.itemView.card_asset.cardElevation = dp2px(2).toFloat()
                    helper.itemView.card_asset.setCardBackgroundColor(findColor(R.color.white))
                }

                helper.itemView.image_asset_icon.setAsset(item)
            }
            AssetSortingItem.TYPE_EMPTY_FAVORITE -> {
                helper.setText(R.id.text_empty, mContext.getString(R.string.wallet_sorting_favorite_empty))
                        .setImageResource(R.id.image_empty, R.drawable.ic_favorite_14_submit_300)
            }
            AssetSortingItem.TYPE_EMPTY_DEFAULT -> {
                helper.setText(R.id.text_empty, mContext.getString(R.string.wallet_sorting_default_empty))
                        .setImageResource(R.id.image_empty, R.drawable.ic_userimg_empty_80)
            }
            AssetSortingItem.TYPE_EMPTY_HIDDEN -> {
                helper.setText(R.id.text_empty, mContext.getString(R.string.wallet_sorting_hidden_empty))
                        .setImageResource(R.id.image_empty, R.drawable.ic_visibility_18_basic_500)
            }
            AssetSortingItem.TYPE_FAVORITE_SEPARATOR, AssetSortingItem.TYPE_HIDDEN_HEADER -> {
                // nothing
            }
        }
    }

    fun getLinePosition(): Int {
        return data.indexOfFirst { it.itemType == AssetSortingItem.TYPE_FAVORITE_SEPARATOR }
    }


    fun getHiddenLinePosition(): Int {
        return data.indexOfFirst { it.itemType == AssetSortingItem.TYPE_HIDDEN_HEADER }
    }

    private fun findEmptyViewPositionOf(type: Int): Int {
        return data.indexOfFirst { it.type == type }
    }

    fun checkEmptyViews() {
        val favoriteEmptyViewPosition = findEmptyViewPositionOf(AssetSortingItem.TYPE_EMPTY_FAVORITE)
        val defaultEmptyViewPosition = findEmptyViewPositionOf(AssetSortingItem.TYPE_EMPTY_DEFAULT)
        val hiddenEmptyViewPosition = findEmptyViewPositionOf(AssetSortingItem.TYPE_EMPTY_HIDDEN)

        clearEmptyViewIfNeed(favoriteEmptyViewPosition, defaultEmptyViewPosition, hiddenEmptyViewPosition)
        addEmptyViewIfNeed(favoriteEmptyViewPosition, defaultEmptyViewPosition, hiddenEmptyViewPosition)
    }

    private fun clearEmptyViewIfNeed(favoriteEmptyViewPosition: Int, defaultEmptyViewPosition: Int, hiddenEmptyViewPosition: Int) {
        val needClearFavoriteEmptyView = favoriteEmptyViewPosition != NOT_FOUND
                && data.indexOfFirst { it.type == AssetSortingItem.TYPE_FAVORITE_ITEM } != NOT_FOUND
        val needClearDefaultEmptyView = defaultEmptyViewPosition != NOT_FOUND
                && data.indexOfFirst { it.type == AssetSortingItem.TYPE_DEFAULT_ITEM } != NOT_FOUND
        val needClearHiddenEmptyView = hiddenEmptyViewPosition != NOT_FOUND
                && data.indexOfFirst { it.type == AssetSortingItem.TYPE_HIDDEN_ITEM } != NOT_FOUND

        if (needClearFavoriteEmptyView) {
            remove(favoriteEmptyViewPosition)
        }
        if (needClearDefaultEmptyView) {
            remove(defaultEmptyViewPosition)
        }
        if (needClearHiddenEmptyView) {
            remove(hiddenEmptyViewPosition)
        }
    }

    private fun addEmptyViewIfNeed(favoriteEmptyViewPosition: Int, defaultEmptyViewPosition: Int, hiddenEmptyViewPosition: Int) {
        val needAddFavoriteEmptyView = favoriteEmptyViewPosition == NOT_FOUND
                && data.indexOfFirst { it.type == AssetSortingItem.TYPE_FAVORITE_ITEM } == NOT_FOUND
        val needAddDefaultEmptyView = defaultEmptyViewPosition == NOT_FOUND
                && data.indexOfFirst { it.type == AssetSortingItem.TYPE_DEFAULT_ITEM } == NOT_FOUND
        val needAddHiddenEmptyView = hiddenEmptyViewPosition == NOT_FOUND
                && data.indexOfFirst { it.type == AssetSortingItem.TYPE_HIDDEN_ITEM } == NOT_FOUND

        if (needAddFavoriteEmptyView) {
            addData(getLinePosition(), AssetSortingItem(AssetSortingItem.TYPE_EMPTY_FAVORITE))
        }
        if (needAddDefaultEmptyView) {
            addData(getLinePosition() + HEADER_SEPARATOR_POSITION, AssetSortingItem(AssetSortingItem.TYPE_EMPTY_DEFAULT))
        }
        if (needAddHiddenEmptyView) {
            addData(getHiddenLinePosition() + HEADER_SEPARATOR_POSITION, AssetSortingItem(AssetSortingItem.TYPE_EMPTY_HIDDEN))
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

    // todo check item: AssetBalanceResponse
    interface OnHiddenChangeListener {
        fun onHiddenStateChanged(item: AssetSortingItem, checked: Boolean, position: Int)
    }

    companion object {
        const val NOT_FOUND = -1
        const val HEADER_SEPARATOR_POSITION = 1
    }
}
