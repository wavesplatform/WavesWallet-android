package com.wavesplatform.wallet.v2.ui.home.wallet.leasing.history

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import com.wavesplatform.wallet.v2.ui.home.history.tab.HistoryTabFragment
import com.wavesplatform.wallet.v2.ui.home.history.tab.HistoryTabFragment.Companion.all

class LeasingHistoryFragmentPageAdapter(fm: FragmentManager?, var titles: Array<String>) : FragmentStatePagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        when (position) {
            0 -> {
                return HistoryTabFragment.newInstance(HistoryTabFragment.leasing_all)
            }
            1 -> {
                return HistoryTabFragment.newInstance(HistoryTabFragment.leasing_active_now)
            }
            2 -> {
                return HistoryTabFragment.newInstance(HistoryTabFragment.leasing_canceled)
            }
        }
        return HistoryTabFragment.newInstance(HistoryTabFragment.leasing_all)
    }

    override fun getCount(): Int = titles.size

    override fun getPageTitle(position: Int): CharSequence? {
        return titles[position]
    }
}

