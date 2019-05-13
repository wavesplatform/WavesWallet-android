/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.util.drag_helper

import android.support.v7.widget.RecyclerView

interface ItemDragListener {
    fun onStartDrag(viewHolder: RecyclerView.ViewHolder, position: Int)

    fun onMoved(fromHolder: RecyclerView.ViewHolder?, fromPosition: Int,
                toHolder: RecyclerView.ViewHolder?, toPosition: Int)

    fun onEndDrag(viewHolder: RecyclerView.ViewHolder)
}
