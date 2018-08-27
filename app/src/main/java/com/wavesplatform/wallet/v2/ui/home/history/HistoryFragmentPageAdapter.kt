package com.wavesplatform.wallet.v2.ui.home.history

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import com.wavesplatform.wallet.v2.ui.home.history.item.HistoryDateItemFragment
import com.wavesplatform.wallet.v2.ui.home.history.item.HistoryDateItemFragment.Companion.all

class HistoryFragmentPageAdapter(fm: FragmentManager?, var titles: Array<String>) : FragmentStatePagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        when (position) {
            0 -> {
                return HistoryDateItemFragment.newInstance(HistoryDateItemFragment.all)
            }
            1 -> {
                return HistoryDateItemFragment.newInstance(HistoryDateItemFragment.send)
            }
            2 -> {
                return HistoryDateItemFragment.newInstance(HistoryDateItemFragment.received)
            }
            3 -> {
                return HistoryDateItemFragment.newInstance(HistoryDateItemFragment.exchanged)
            }
            4 -> {
                return HistoryDateItemFragment.newInstance(HistoryDateItemFragment.leased)
            }
            5 -> {
                return HistoryDateItemFragment.newInstance(HistoryDateItemFragment.issued)
            }
        }
        return HistoryDateItemFragment.newInstance(all)
    }

    override fun getCount(): Int = titles.size

    override fun getPageTitle(position: Int): CharSequence? {
        return titles[position]
    }
}

