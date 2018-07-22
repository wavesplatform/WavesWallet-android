package com.wavesplatform.wallet.v2.ui.home.wallet.assets.details

import android.content.Context
import android.support.v4.view.PagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.injection.qualifier.ApplicationContext
import com.wavesplatform.wallet.v2.ui.home.history.adapter.HistoryItem
import java.util.*
import javax.inject.Inject

class HistoryTransactionPagerAdapter @Inject constructor(@ApplicationContext var mContext: Context) : PagerAdapter() {
    var items: ArrayList<HistoryItem> = arrayListOf()

    override fun instantiateItem(collection: ViewGroup, position: Int): Any {
        val layout = LayoutInflater.from(mContext).inflate(R.layout.assets_detailed_history_item, collection, false)

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