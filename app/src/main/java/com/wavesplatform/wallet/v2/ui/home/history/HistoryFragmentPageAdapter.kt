package com.wavesplatform.wallet.v2.ui.home.history

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import com.wavesplatform.wallet.v2.ui.home.history.tab.HistoryTabFragment
import com.wavesplatform.wallet.v2.ui.home.history.tab.HistoryTabFragment.Companion.all

class HistoryFragmentPageAdapter(fm: FragmentManager?, var titles: Array<String>) : FragmentStatePagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        when (position) {
            0 -> {
                return HistoryTabFragment.newInstance(HistoryTabFragment.all)
            }
            1 -> {
                return HistoryTabFragment.newInstance(HistoryTabFragment.send)
            }
            2 -> {
                return HistoryTabFragment.newInstance(HistoryTabFragment.received)
            }
            3 -> {
                return HistoryTabFragment.newInstance(HistoryTabFragment.exchanged)
            }
            4 -> {
                return HistoryTabFragment.newInstance(HistoryTabFragment.leased)
            }
            5 -> {
                return HistoryTabFragment.newInstance(HistoryTabFragment.issued)
            }
        }
        return HistoryTabFragment.newInstance(all)
    }

    override fun getCount(): Int = titles.size

    override fun getPageTitle(position: Int): CharSequence? {
        return titles[position]
    }
}

