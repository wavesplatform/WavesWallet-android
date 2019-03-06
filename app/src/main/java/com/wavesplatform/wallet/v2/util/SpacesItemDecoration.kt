package com.wavesplatform.wallet.v2.util

import android.graphics.Rect
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View

/**
 * Adds spaces (between) between Item views.
 *
 * Supports GridLayoutManager and LinearLayoutManager. Extend this class and override the
 * [.getSpanLookup] method to support other
 * LayoutManagers.
 *
 * Currently only supports LayoutManagers in VERTICAL orientation.
 */
class SpacesItemDecoration private constructor(private val itemSplitMarginEven: Int, private val itemSplitMarginLarge: Int, private val itemSplitMarginSmall: Int, private val verticalSpacing: Int) : RecyclerView.ItemDecoration() {

    /*fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State?) {
        val layoutParams = view.layoutParams as RecyclerView.LayoutParams
        val itemPosition = layoutParams.viewPosition
        val childCount = parent.adapter!!.itemCount

        val spanLookup = getSpanLookup(view, parent)
        applyItemHorizontalOffsets(spanLookup, itemPosition, outRect)
        applyItemVerticalOffsets(outRect, itemPosition, childCount, spanLookup.spanCount, spanLookup)
    }*/

    private fun getSpanLookup(view: View, parent: RecyclerView): SpanLookup {
        val layoutManager = parent.layoutManager
        return if (layoutManager is GridLayoutManager) {
            SpanLookupFactory.gridLayoutSpanLookup(layoutManager)
        } else SpanLookupFactory.singleSpan()
    }

    private fun applyItemVerticalOffsets(outRect: Rect, itemPosition: Int, childCount: Int, spanCount: Int, spanLookup: SpanLookup) {
        outRect.top = getItemTopSpacing(spanLookup, verticalSpacing, itemPosition, spanCount, childCount)
        outRect.bottom = getItemBottomSpacing(spanLookup, verticalSpacing, itemPosition, childCount)
    }

    private fun applyItemHorizontalOffsets(spanLookup: SpanLookup, itemPosition: Int, offsets: Rect) {
        if (itemIsFullSpan(spanLookup, itemPosition)) {
            offsets.left = 0
            offsets.right = 0
            return
        }

        if (itemStartsAtTheLeftEdge(spanLookup, itemPosition)) {
            offsets.left = 0
            offsets.right = itemSplitMarginLarge
            return
        }

        if (itemEndsAtTheRightEdge(spanLookup, itemPosition)) {
            offsets.left = itemSplitMarginLarge
            offsets.right = 0
            return
        }

        if (itemIsNextToAnItemThatStartsOnTheLeftEdge(spanLookup, itemPosition)) {
            offsets.left = itemSplitMarginSmall
        } else {
            offsets.left = itemSplitMarginEven
        }

        if (itemIsNextToAnItemThatEndsOnTheRightEdge(spanLookup, itemPosition)) {
            offsets.right = itemSplitMarginSmall
        } else {
            offsets.right = itemSplitMarginEven
        }
    }

    companion object {

        fun newInstance(horizontalSpacing: Int, verticalSpacing: Int, spanCount: Int): SpacesItemDecoration {
            val maxNumberOfSpaces = spanCount - 1
            val totalSpaceToSplitBetweenItems = maxNumberOfSpaces * horizontalSpacing

            val itemSplitMarginEven = (0.5f * horizontalSpacing).toInt()
            val itemSplitMarginLarge = totalSpaceToSplitBetweenItems / spanCount
            val itemSplitMarginSmall = horizontalSpacing - itemSplitMarginLarge

            return SpacesItemDecoration(itemSplitMarginEven, itemSplitMarginLarge, itemSplitMarginSmall, verticalSpacing)
        }

        private fun itemIsNextToAnItemThatStartsOnTheLeftEdge(spanLookup: SpanLookup, itemPosition: Int): Boolean {
            return !itemStartsAtTheLeftEdge(spanLookup, itemPosition) && itemStartsAtTheLeftEdge(spanLookup, itemPosition - 1)
        }

        private fun itemIsNextToAnItemThatEndsOnTheRightEdge(spanLookup: SpanLookup, itemPosition: Int): Boolean {
            return !itemEndsAtTheRightEdge(spanLookup, itemPosition) && itemEndsAtTheRightEdge(spanLookup, itemPosition + 1)
        }

        private fun itemIsFullSpan(spanLookup: SpanLookup, itemPosition: Int): Boolean {
            return itemStartsAtTheLeftEdge(spanLookup, itemPosition) && itemEndsAtTheRightEdge(spanLookup, itemPosition)
        }

        private fun itemStartsAtTheLeftEdge(spanLookup: SpanLookup, itemPosition: Int): Boolean {
            return spanLookup.getSpanIndex(itemPosition) == 0
        }

        private fun itemEndsAtTheRightEdge(spanLookup: SpanLookup, itemPosition: Int): Boolean {
            return spanLookup.getSpanIndex(itemPosition) + spanLookup.getSpanSize(itemPosition) == spanLookup.spanCount
        }

        private fun getItemTopSpacing(spanLookup: SpanLookup, verticalSpacing: Int, itemPosition: Int, spanCount: Int, childCount: Int): Int {
            return if (itemIsOnTheTopRow(spanLookup, itemPosition, spanCount, childCount)) {
                0
            } else {
                (.5f * verticalSpacing).toInt()
            }
        }

        private fun itemIsOnTheTopRow(spanLookup: SpanLookup, itemPosition: Int, spanCount: Int, childCount: Int): Boolean {
            var latestCheckedPosition = 0
            for (i in 0 until childCount) {
                latestCheckedPosition = i
                val spanEndIndex = spanLookup.getSpanIndex(i) + spanLookup.getSpanSize(i) - 1
                if (spanEndIndex == spanCount - 1) {
                    break
                }
            }
            return itemPosition <= latestCheckedPosition
        }

        private fun getItemBottomSpacing(spanLookup: SpanLookup, verticalSpacing: Int, itemPosition: Int, childCount: Int): Int {
            return if (itemIsOnTheBottomRow(spanLookup, itemPosition, childCount)) {
                0
            } else {
                (.5f * verticalSpacing).toInt()
            }
        }

        private fun itemIsOnTheBottomRow(spanLookup: SpanLookup, itemPosition: Int, childCount: Int): Boolean {
            var latestCheckedPosition = 0
            for (i in childCount - 1 downTo 0) {
                latestCheckedPosition = i
                val spanIndex = spanLookup.getSpanIndex(i)
                if (spanIndex == 0) {
                    break
                }
            }
            return itemPosition >= latestCheckedPosition
        }
    }
}

internal object SpanLookupFactory {

    fun singleSpan(): SpanLookup {
        return object : SpanLookup {
            override val spanCount: Int
                get() = 1

            override fun getSpanIndex(itemPosition: Int): Int {
                return 0
            }

            override fun getSpanSize(itemPosition: Int): Int {
                return 1
            }
        }
    }

    fun gridLayoutSpanLookup(layoutManager: GridLayoutManager): SpanLookup {
        return object : SpanLookup {
            override val spanCount: Int
                get() = layoutManager.spanCount

            override fun getSpanIndex(itemPosition: Int): Int {
                return layoutManager.spanSizeLookup.getSpanIndex(itemPosition, spanCount)
            }

            override fun getSpanSize(itemPosition: Int): Int {
                return layoutManager.spanSizeLookup.getSpanSize(itemPosition)
            }
        }
    }
}

internal interface SpanLookup {

    /**
     * @return number of spans in a row
     */
    val spanCount: Int

    /**
     * @param itemPosition
     * @return start span for the item at the given adapterActiveAdapter position
     */
    fun getSpanIndex(itemPosition: Int): Int

    /**
     * @param itemPosition
     * @return number of spans the item at the given adapterActiveAdapter position occupies
     */
    fun getSpanSize(itemPosition: Int): Int
}