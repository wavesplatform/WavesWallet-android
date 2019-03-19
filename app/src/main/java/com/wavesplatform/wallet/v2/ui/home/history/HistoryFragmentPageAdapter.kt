package com.wavesplatform.wallet.v2.ui.home.history

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import com.wavesplatform.wallet.v2.ui.home.history.tab.HistoryTabFragment

class HistoryFragmentPageAdapter(fm: FragmentManager?, var data: MutableList<Pair<HistoryTabFragment, String>>) : FragmentStatePagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return data[position].first
    }

    override fun getCount(): Int = data.size

    override fun getPageTitle(position: Int): CharSequence? {
        return data[position].second
    }
}
