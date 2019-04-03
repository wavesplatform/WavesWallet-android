/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.wallet.assets.details

import android.support.v4.view.PagerAdapter
import android.view.View
import android.view.ViewGroup
import com.wavesplatform.wallet.R
import com.wavesplatform.sdk.net.model.response.AssetBalance
import kotlinx.android.synthetic.main.item_asset_details_avatar.view.*
import pers.victor.ext.inflate
import javax.inject.Inject

class AssetDetailsAvatarPagerAdapter @Inject constructor() : PagerAdapter() {
    var items: List<AssetBalance> = arrayListOf()

    override fun instantiateItem(collection: ViewGroup, position: Int): Any {
        val layout = inflate(R.layout.item_asset_details_avatar, collection, false) as ViewGroup

        layout.image_welcome_photo.setAsset(items[position])

        collection.addView(layout)
        return layout
    }

    override fun destroyItem(collection: ViewGroup, position: Int, view: Any) {
        collection.removeView(view as View)
    }

    override fun getPageWidth(position: Int): Float {
        return 1f
    }

    override fun getItemPosition(`object`: Any): Int {
        return PagerAdapter.POSITION_NONE
    }

    override fun getCount(): Int {
        return items.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }
}