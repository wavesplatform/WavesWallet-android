package com.wavesplatform.wallet.v2.ui.home.quick_action.receive

import android.content.Context
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.base.view.BaseFragment
import com.wavesplatform.wallet.v2.ui.home.quick_action.receive.card.CardFragment
import com.wavesplatform.wallet.v2.ui.home.quick_action.receive.cryptocurrency.СryptocurrencyFragment
import com.wavesplatform.wallet.v2.ui.home.quick_action.receive.invoice.InvoiceFragment

class ReceivePageAdapter(fm: FragmentManager?, var context: Context) : FragmentStatePagerAdapter(fm) {

    private var data: MutableList<BaseFragment> = arrayListOf(
            СryptocurrencyFragment.newInstance(),
            InvoiceFragment.newInstance(),
            CardFragment.newInstance())

    private var titles: Array<String> = arrayOf(
            context.getString(R.string.receive_cryptocurrency),
            context.getString(R.string.receive_invoice),
            context.getString(R.string.receive_card))

    override fun getPageTitle(position: Int): CharSequence? {
        return titles[position]
    }

    override fun getItem(position: Int): Fragment {
        return data[position]
    }

    override fun getCount(): Int = data.size
}
