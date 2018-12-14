package com.wavesplatform.wallet.v2.util.drag_helper

interface ItemTouchHelperAdapter {

    fun onItemMove(fromPosition: Int, toPosition: Int): Boolean
}
