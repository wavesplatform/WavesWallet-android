package com.wavesplatform.wallet.v2.ui.home.wallet

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.app.FragmentStatePagerAdapter
import com.wavesplatform.wallet.v2.ui.home.wallet.assets.AssetsFragment
import com.wavesplatform.wallet.v2.ui.home.wallet.leasing.LeasingFragment

class WalletFragmentPageAdapter(
        fm: FragmentManager?, var titles: Array<String>) : FragmentPagerAdapter(fm) {

    private val fragments = arrayListOf<Fragment>()

    init {
        fragments.add(AssetsFragment.newInstance())
        fragments.add(LeasingFragment.newInstance())
    }

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
