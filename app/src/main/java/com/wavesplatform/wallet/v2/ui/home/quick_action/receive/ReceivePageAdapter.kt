package com.wavesplatform.wallet.v2.ui.home.quick_action.receive

import android.content.Context
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.ui.base.view.BaseFragment
import com.wavesplatform.wallet.v2.ui.home.quick_action.receive.card.CardFragment
import com.wavesplatform.wallet.v2.ui.home.quick_action.receive.cryptocurrency.CryptoCurrencyFragment
import com.wavesplatform.wallet.v2.ui.home.quick_action.receive.invoice.InvoiceFragment

class ReceivePageAdapter(
    fm: FragmentManager?,
    var context: Context,
    var assetBalance: AssetBalance?
) : FragmentStatePagerAdapter(fm) {

    var data: MutableList<BaseFragment>
    private var titles: Array<String>

    init {
        when {
            assetBalance == null -> {
                data = arrayListOf(
                        CryptoCurrencyFragment.newInstance(assetBalance),
                        InvoiceFragment.newInstance(assetBalance),
                        CardFragment.newInstance())
                titles = arrayOf(
                        context.getString(R.string.receive_cryptocurrency),
                        context.getString(R.string.receive_invoice),
                        context.getString(R.string.receive_card))
            }
            assetBalance!!.isWaves() -> {
                data = arrayListOf(
                        InvoiceFragment.newInstance(assetBalance),
                        CardFragment.newInstance())
                titles = arrayOf(
                        context.getString(R.string.receive_invoice),
                        context.getString(R.string.receive_card))
            }
            AssetBalance.isFiat(assetBalance!!.assetId!!) -> {
                data = arrayListOf(
                        InvoiceFragment.newInstance(assetBalance))
                titles = arrayOf(
                        context.getString(R.string.receive_invoice))
            }
            AssetBalance.isGateway(assetBalance!!.assetId!!) -> {
                data = arrayListOf(
                        CryptoCurrencyFragment.newInstance(assetBalance),
                        InvoiceFragment.newInstance(assetBalance))
                titles = arrayOf(
                        context.getString(R.string.receive_cryptocurrency),
                        context.getString(R.string.receive_invoice))
            }
            else -> {
                data = arrayListOf(
                        InvoiceFragment.newInstance(assetBalance))
                titles = arrayOf(
                        context.getString(R.string.receive_invoice))
            }
        }
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return titles[position]
    }

    override fun getItem(position: Int): Fragment {
        return data[position]
    }

    override fun getCount(): Int = data.size
}
