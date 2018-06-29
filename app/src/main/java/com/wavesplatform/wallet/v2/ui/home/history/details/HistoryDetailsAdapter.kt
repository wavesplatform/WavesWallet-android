package com.wavesplatform.wallet.v2.ui.home.history.details

import android.content.Context
import android.support.v4.view.PagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.home.history.TestObject
import com.wavesplatform.wallet.v2.ui.home.history.item.HistoryDateItemFragment.Companion.all
import com.wavesplatform.wallet.v2.ui.home.history.item.HistoryDateItemFragment.Companion.received
import com.wavesplatform.wallet.v2.ui.home.history.item.HistoryDateItemFragment.Companion.send
import com.wavesplatform.wallet.v2.util.makeTextHalfBold

class HistoryDetailsAdapter(private val mContext: Context, var mData: List<TestObject>, val historyType: String?) : PagerAdapter() {

    override fun instantiateItem(collection: ViewGroup, position: Int): Any {
        val historyItem = mData[position]
        val layout = LayoutInflater.from(mContext).inflate(R.layout.history_details_layout, collection, false) as ViewGroup

        val historyTypeImg = layout.findViewById<ImageView>(R.id.image_history_type)

        when (historyType) {
            all -> {
                historyTypeImg.setImageResource(R.drawable.ic_startlease)
            }
            send -> {
                historyTypeImg.setImageResource(R.drawable.ic_send)
            }
            received -> {
                historyTypeImg.setImageResource(R.drawable.ic_receive)
            }
        }

        val textLeasingValue = layout.findViewById<TextView>(R.id.text_leasing_value)
        textLeasingValue?.text = "${historyItem.assetValue}"
        textLeasingValue?.makeTextHalfBold()

        collection.addView(layout)
        return layout
    }

    override fun destroyItem(collection: ViewGroup, position: Int, view: Any) {
        collection.removeView(view as View)
    }

    override fun getCount(): Int {
        return mData.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }
}