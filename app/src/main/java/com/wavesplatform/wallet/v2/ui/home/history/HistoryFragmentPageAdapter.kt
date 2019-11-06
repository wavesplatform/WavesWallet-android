/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.history

import android.annotation.SuppressLint
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.wavesplatform.wallet.v2.ui.home.history.tab.HistoryTabFragment

@SuppressLint("WrongConstant")
class HistoryFragmentPageAdapter(fm: FragmentManager, var data: MutableList<Pair<HistoryTabFragment, String>>) : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment {
        return data[position].first
    }

    override fun getCount(): Int = data.size

    override fun getPageTitle(position: Int): CharSequence? {
        return data[position].second
    }
}
