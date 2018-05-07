package com.wavesplatform.wallet.v2.ui.welcome

import android.content.Context
import android.support.v4.view.PagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.local.WelcomeItem
import com.wavesplatform.wallet.v2.injection.qualifier.ApplicationContext
import kotlinx.android.synthetic.main.item_welcome.view.*
import java.util.*
import javax.inject.Inject

class WelcomeItemsPagerAdapter @Inject constructor(@ApplicationContext var mContext: Context) : PagerAdapter() {
    var items: ArrayList<WelcomeItem> = arrayListOf()

    override fun instantiateItem(collection: ViewGroup, position: Int): Any {
        val layout = LayoutInflater.from(mContext).inflate(R.layout.item_welcome, collection, false) as ViewGroup

        layout.image_welcome_photo.setImageResource(items[position].image)

        collection.addView(layout)
        return layout
    }

    override fun destroyItem(collection: ViewGroup, position: Int, view: Any) {
        collection.removeView(view as View)
    }

    override fun getPageWidth(position: Int): Float {
        return 1f
    }

    override fun getCount(): Int {
        return items.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }
}