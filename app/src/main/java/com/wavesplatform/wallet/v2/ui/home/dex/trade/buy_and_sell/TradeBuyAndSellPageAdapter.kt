package com.wavesplatform.wallet.v2.ui.home.dex.trade.buy_and_sell

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter

class TradeBuyAndSellPageAdapter(fm: FragmentManager?, var fragments: ArrayList<Fragment>, var titles: Array<String>) : FragmentStatePagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return fragments[position]
    }

    override fun getCount(): Int = titles.size

    override fun getPageTitle(position: Int): CharSequence? {
        return titles[position]
    }
}
