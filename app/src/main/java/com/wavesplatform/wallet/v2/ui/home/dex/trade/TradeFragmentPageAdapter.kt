package com.wavesplatform.wallet.v2.ui.home.dex.trade

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import com.wavesplatform.wallet.v2.ui.home.dex.trade.my_orders.TradeMyOrdersFragment

class TradeFragmentPageAdapter(fm: FragmentManager?, var titles: Array<String>) : FragmentStatePagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return TradeMyOrdersFragment()
    }

    override fun getCount(): Int = titles.size

    override fun getPageTitle(position: Int): CharSequence? {
        return titles[position]
    }
}

