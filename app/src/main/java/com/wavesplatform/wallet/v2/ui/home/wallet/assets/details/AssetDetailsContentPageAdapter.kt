package com.wavesplatform.wallet.v2.ui.home.wallet.assets.details

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.ui.home.wallet.assets.details.content.AssetDetailsContentFragment
import pyxis.uzuki.live.richutilskt.utils.put

class AssetDetailsContentPageAdapter(fm: FragmentManager?, var assets: ArrayList<AssetBalance>) : FragmentStatePagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
           return AssetDetailsContentFragment().apply {
               val bundle = Bundle()
               bundle.put(AssetDetailsContentFragment.BUNDLE_ASSET, assets[position])
               arguments = bundle
           }
    }

    override fun getCount(): Int = assets.size
}

