/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.welcome

import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.asksira.loopingviewpager.LoopingPagerAdapter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.local.WelcomeItem
import kotlinx.android.synthetic.main.item_welcome.view.*
import pers.victor.ext.inflate
import java.util.*

class WelcomeItemsPagerAdapter(context: Context, itemList: ArrayList<WelcomeItem>, isInfinite: Boolean) : LoopingPagerAdapter<WelcomeItem>(context, itemList, isInfinite) {

    override fun inflateView(viewType: Int, container: ViewGroup, listPosition: Int): View {
        return inflate(R.layout.item_welcome, container, false)
    }

    override fun bindView(convertView: View, listPosition: Int, viewType: Int) {
        convertView.image_welcome_photo.setImageResource(itemList[listPosition].image)
        convertView.text_title.text = itemList[listPosition].title
        convertView.text_descr.text = itemList[listPosition].description
    }
}