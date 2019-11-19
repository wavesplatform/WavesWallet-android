/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.auth.import_account

import android.annotation.SuppressLint
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.wavesplatform.wallet.v2.ui.auth.import_account.manually.EnterSeedManuallyFragment
import com.wavesplatform.wallet.v2.ui.auth.import_account.scan.ScanSeedFragment

@SuppressLint("WrongConstant")
class ImportAccountFragmentPageAdapter(fm: FragmentManager, var titles: Array<String>) : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

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
