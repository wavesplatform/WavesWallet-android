package com.wavesplatform.wallet.v2.ui.home.quick_action.receive

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import com.wavesplatform.wallet.v2.ui.home.quick_action.receive.bank.BankFragment
import com.wavesplatform.wallet.v2.ui.home.quick_action.receive.card.CardFragment
import com.wavesplatform.wallet.v2.ui.home.quick_action.receive.cryptocurrency.СryptocurrencyFragment
import com.wavesplatform.wallet.v2.ui.home.quick_action.receive.invoice.InvoiceFragment

class ReceivePageAdapter(fm: FragmentManager?, var titles: Array<String>) : FragmentStatePagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> СryptocurrencyFragment.newInstance()
            1 -> InvoiceFragment.newInstance()
            2 -> CardFragment.newInstance()
            3 -> BankFragment.newInstance()
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
