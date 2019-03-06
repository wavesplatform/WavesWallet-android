package com.wavesplatform.wallet.v2.ui.auth.import_account

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import com.wavesplatform.wallet.v2.ui.auth.import_account.manually.EnterSeedManuallyFragment
import com.wavesplatform.wallet.v2.ui.auth.import_account.scan.ScanSeedFragment

class ImportAccountFragmentPageAdapter(fm: FragmentManager?, var titles: Array<String>) : FragmentStatePagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        when (position) {
            0 -> {
                return ScanSeedFragment()
            }
            1 -> {
                return EnterSeedManuallyFragment()
            }
        }
        return ScanSeedFragment()
    }

    override fun getCount(): Int = titles.size

    override fun getPageTitle(position: Int): CharSequence? {
        return titles[position]
    }
}
