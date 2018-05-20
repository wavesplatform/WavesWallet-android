package com.wavesplatform.wallet.v2.ui.home.history.adapter

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import com.wavesplatform.wallet.v2.ui.home.history.item.HistoryDateItemFragment
import com.wavesplatform.wallet.v2.ui.home.history.item.HistoryDateItemFragment.Companion.all
import com.wavesplatform.wallet.v2.ui.home.history.item.HistoryDateItemFragment.Companion.received
import com.wavesplatform.wallet.v2.ui.home.history.item.HistoryDateItemFragment.Companion.send

class HistoryFragmentPageAdapter(fm: FragmentManager?) : FragmentStatePagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        when (position) {
            0 -> {
                return HistoryDateItemFragment.newInstance(all)
            }
            1 -> {
                return HistoryDateItemFragment.newInstance(send)
            }
            2 -> {
                return HistoryDateItemFragment.newInstance(received)
            }
        }
        return HistoryDateItemFragment.newInstance(all)
    }

    override fun getCount(): Int = 3
}

