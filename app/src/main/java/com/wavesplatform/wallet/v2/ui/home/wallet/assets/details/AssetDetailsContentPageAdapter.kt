/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.wallet.assets.details

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.wavesplatform.sdk.model.response.node.AssetBalanceResponse
import com.wavesplatform.wallet.v2.ui.home.wallet.assets.details.content.AssetDetailsContentFragment

@SuppressLint("WrongConstant")
class AssetDetailsContentPageAdapter(fm: FragmentManager, var assets: List<AssetBalanceResponse>) : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment {
        val bundle = Bundle()
        val assetDetailsContentFragment = AssetDetailsContentFragment()
        bundle.putParcelable(AssetDetailsContentFragment.BUNDLE_ASSET, assets[position])
        assetDetailsContentFragment.arguments = bundle
        return assetDetailsContentFragment
    }

    override fun getCount(): Int = assets.size
}
