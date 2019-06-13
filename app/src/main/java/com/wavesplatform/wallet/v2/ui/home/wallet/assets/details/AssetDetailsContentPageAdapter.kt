/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.wallet.assets.details

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import com.wavesplatform.sdk.model.response.AssetBalanceResponse
import com.wavesplatform.wallet.v2.ui.home.wallet.assets.details.content.AssetDetailsContentFragment

class AssetDetailsContentPageAdapter(fm: FragmentManager?, var assets: List<AssetBalanceResponse>) : FragmentStatePagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        val bundle = Bundle()
        val assetDetailsContentFragment = AssetDetailsContentFragment()
        bundle.putParcelable(AssetDetailsContentFragment.BUNDLE_ASSET, assets[position])
        assetDetailsContentFragment.arguments = bundle
        return assetDetailsContentFragment
    }

    override fun getCount(): Int = assets.size
}
