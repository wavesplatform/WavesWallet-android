/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.util.drag_helper

import androidx.recyclerview.widget.RecyclerView

interface ItemDragListener {
    fun onStartDrag(viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int)

    fun onMoved(fromHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder?, fromPosition: Int,
                toHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder?, toPosition: Int)

    fun onEndDrag(viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder)
}
