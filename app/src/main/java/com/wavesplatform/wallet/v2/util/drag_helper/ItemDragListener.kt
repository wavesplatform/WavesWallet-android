/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.util.drag_helper

import android.support.v7.widget.RecyclerView
import android.view.View
import com.wavesplatform.wallet.v2.ui.home.wallet.assets.sorting.AssetsSortingAdapter

interface ItemDragListener {
    fun onStartDrag(viewHolder: RecyclerView.ViewHolder, position: Int)

    fun onMoved(fromHolder: View?, fromPosition: Int,
                toHolder: View?, toPosition: Int)

    fun onEndDrag(viewHolder: RecyclerView.ViewHolder)
}
