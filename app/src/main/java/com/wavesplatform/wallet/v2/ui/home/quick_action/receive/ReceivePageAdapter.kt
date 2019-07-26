/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.quick_action.receive

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import com.wavesplatform.wallet.R
import com.wavesplatform.sdk.model.response.node.AssetBalanceResponse
import com.wavesplatform.wallet.v2.ui.base.view.BaseFragment
import com.wavesplatform.wallet.v2.ui.home.quick_action.receive.card.CardFragment
import com.wavesplatform.wallet.v2.ui.home.quick_action.receive.cryptocurrency.CryptoCurrencyFragment
import com.wavesplatform.wallet.v2.ui.home.quick_action.receive.invoice.InvoiceFragment
import com.wavesplatform.wallet.v2.util.isFiat
import com.wavesplatform.wallet.v2.util.isGateway
import pers.victor.ext.app

class ReceivePageAdapter(
    fm: FragmentManager?,
    var assetBalance: AssetBalanceResponse?
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
                        app.getString(R.string.receive_cryptocurrency),
                        app.getString(R.string.receive_invoice),
                        app.getString(R.string.receive_card))
            }
            assetBalance!!.isWaves() -> {
                data = arrayListOf(
                        InvoiceFragment.newInstance(assetBalance),
                        CardFragment.newInstance())
                titles = arrayOf(
                        app.getString(R.string.receive_invoice),
                        app.getString(R.string.receive_card))
            }
            isFiat(assetBalance!!.assetId) -> {
                data = arrayListOf(
                        InvoiceFragment.newInstance(assetBalance))
                titles = arrayOf(
                        app.getString(R.string.receive_invoice))
            }
            isGateway(assetBalance!!.assetId) -> {
                data = arrayListOf(
                        CryptoCurrencyFragment.newInstance(assetBalance),
                        InvoiceFragment.newInstance(assetBalance))
                titles = arrayOf(
                        app.getString(R.string.receive_cryptocurrency),
                        app.getString(R.string.receive_invoice))
            }
            else -> {
                data = arrayListOf(
                        InvoiceFragment.newInstance(assetBalance))
                titles = arrayOf(
                        app.getString(R.string.receive_invoice))
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
