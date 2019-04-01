/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.dex.trade

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter

class TradeFragmentPageAdapter(fm: FragmentManager?, private var pages: ArrayList<Pair<Fragment, String>>) : FragmentStatePagerAdapter(fm) {
    override fun getItem(position: Int): Fragment {
        return pages[position].first
    }

    override fun getCount(): Int = pages.size

    override fun getPageTitle(position: Int): CharSequence? {
        return pages[position].second
    }
}
