package com.wavesplatform.wallet.v2.ui.home.history

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import com.wavesplatform.wallet.v2.ui.home.history.tab.HistoryTabFragment

class HistoryFragmentPageAdapter(
        fm: FragmentManager?,
        private var titles: Array<String>) : FragmentPagerAdapter(fm) {

    private val fragments = arrayListOf<Fragment>()

    init {
        fragments.add(HistoryTabFragment.newInstance(HistoryTabFragment.all))
        fragments.add(HistoryTabFragment.newInstance(HistoryTabFragment.send))
        fragments.add(HistoryTabFragment.newInstance(HistoryTabFragment.received))
        fragments.add(HistoryTabFragment.newInstance(HistoryTabFragment.exchanged))
        fragments.add(HistoryTabFragment.newInstance(HistoryTabFragment.leased))
        fragments.add(HistoryTabFragment.newInstance(HistoryTabFragment.issued))
    }

    override fun getItem(position: Int): Fragment {
        when (position) {
            ALL -> {
                return fragments[ALL]
            }
            SEND -> {
                return fragments[SEND]
            }
            RECEIVED -> {
                return fragments[RECEIVED]
            }
            EXCHANGED -> {
                return fragments[EXCHANGED]
            }
            LEASED -> {
                return fragments[LEASED]
            }
            ISSUED -> {
                return fragments[ISSUED]
            }
        }
        return fragments[ALL]
    }

    override fun getCount(): Int = fragments.size

    override fun getPageTitle(position: Int): CharSequence? {
        return titles[position]
    }

    companion object {
        const val ALL = 0
        private const val SEND = 1
        private const val RECEIVED = 2
        private const val EXCHANGED = 3
        private const val LEASED = 4
        private const val ISSUED = 5
    }
}

