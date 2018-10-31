package com.wavesplatform.wallet.v2.ui.home.dex.trade

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import com.wavesplatform.wallet.v2.data.model.local.WatchMarket
import com.wavesplatform.wallet.v2.ui.home.dex.trade.chart.TradeChartFragment
import com.wavesplatform.wallet.v2.ui.home.dex.trade.last_trades.TradeLastTradesFragment
import com.wavesplatform.wallet.v2.ui.home.dex.trade.my_orders.TradeMyOrdersFragment
import com.wavesplatform.wallet.v2.ui.home.dex.trade.orderbook.TradeOrderbookFragment

class TradeFragmentPageAdapter(fm: FragmentManager?, private var titles: Array<String>, var watchMarket: WatchMarket?) : FragmentStatePagerAdapter(fm) {

    private var fragments = arrayListOf<Fragment>(
            TradeOrderbookFragment.newInstance(watchMarket),
            TradeChartFragment.newInstance(watchMarket),
            TradeLastTradesFragment.newInstance(watchMarket),
            TradeMyOrdersFragment.newInstance(watchMarket)
            )

    override fun getItem(position: Int): Fragment {
       return fragments[position]
    }

    override fun getCount(): Int = titles.size

    override fun getPageTitle(position: Int): CharSequence? {
        return titles[position]
    }
}

