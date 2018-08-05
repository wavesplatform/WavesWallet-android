package com.wavesplatform.wallet.v2.ui.home.dex.trade.buy_and_sell

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import com.wavesplatform.wallet.v2.ui.home.dex.trade.buy_and_sell.buy.TradeBuyFragment
import com.wavesplatform.wallet.v2.ui.home.dex.trade.last_trades.TradeLastTradesFragment
import com.wavesplatform.wallet.v2.ui.home.dex.trade.my_orders.TradeMyOrdersFragment
import com.wavesplatform.wallet.v2.ui.home.dex.trade.orderbook.TradeOrderbookFragment

class BuyAndSellPageAdapter(fm: FragmentManager?, var titles: Array<String>) : FragmentStatePagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        when(position){
            0 -> return TradeBuyFragment()
            1 -> return TradeBuyFragment()
            else -> return TradeBuyFragment()
        }
    }

    override fun getCount(): Int = titles.size

    override fun getPageTitle(position: Int): CharSequence? {
        return titles[position]
    }
}

