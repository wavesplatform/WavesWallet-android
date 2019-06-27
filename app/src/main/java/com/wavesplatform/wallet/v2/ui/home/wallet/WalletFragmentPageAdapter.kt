/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.wallet

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.app.FragmentStatePagerAdapter

class WalletFragmentPageAdapter(
    fm: FragmentManager?,
    var fragments: ArrayList<Fragment>,
    var titles: Array<String>
) : FragmentStatePagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        when (position) {
            ASSETS -> {
                return fragments[ASSETS]
            }
            LEASING -> {
                return fragments[LEASING]
            }
        }
        return fragments[ASSETS]
    }

    override fun getCount(): Int = fragments.size

    override fun getPageTitle(position: Int): CharSequence? {
        return titles[position]
    }

    companion object {
        const val ASSETS = 0
        private const val LEASING = 1
    }
}
