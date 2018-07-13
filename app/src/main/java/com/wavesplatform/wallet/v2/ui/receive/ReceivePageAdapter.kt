package com.wavesplatform.wallet.v2.ui.receive

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import com.wavesplatform.wallet.v2.ui.receive.cryptocurrency.СryptocurrencyFragment

class ReceivePageAdapter(fm: FragmentManager?, var titles: Array<String>) : FragmentStatePagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> СryptocurrencyFragment.newInstance()
            1 -> СryptocurrencyFragment.newInstance()
            2 -> СryptocurrencyFragment.newInstance()
            3 -> СryptocurrencyFragment.newInstance()
            else -> {
                СryptocurrencyFragment.newInstance()
            }
        }
    }

    override fun getCount(): Int = 4

    override fun getPageTitle(position: Int): CharSequence? {
        return titles[position]
    }
}
