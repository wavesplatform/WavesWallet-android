package com.wavesplatform.wallet.v2.ui.tutorial

import android.content.Context
import android.support.v4.view.PagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.injection.qualifier.ApplicationContext
import com.wavesplatform.wallet.v2.util.notNull
import javax.inject.Inject


class TutorialAdapter @Inject constructor(@ApplicationContext var mContext: Context) : PagerAdapter() {
    var items: ArrayList<Int> = arrayListOf()
    var listener: EndOfScrollListener? = null

    override fun instantiateItem(collection: ViewGroup, position: Int): Any {
        val inflater = LayoutInflater.from(mContext)
        val view = when (position) {
            0 -> inflater.inflate(R.layout.item_tutorial_1_card, null, false)
            1 -> inflater.inflate(R.layout.item_tutorial_2_card, null, false)
            2 -> inflater.inflate(R.layout.item_tutorial_3_card, null, false)
            3 -> inflater.inflate(R.layout.item_tutorial_4_card, null, false)
            4 -> inflater.inflate(R.layout.item_tutorial_5_card, null, false)
            else -> inflater.inflate(R.layout.item_tutorial_1_card, null, false)
        }

        val scrollView = view.findViewById<ScrollView>(R.id.scroll_root)
        // TODO: rewrite to nested scroll and fix leak
        scrollView.notNull {
            scrollView.viewTreeObserver.addOnScrollChangedListener {
                if (!scrollView.canScrollVertically(1)) {
                    listener?.onEndOfScroll(position)
                } else {
                    listener?.onNotEndOfScroll(position)
                }
            }
        }

        collection.addView(view)
        return view
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

    interface EndOfScrollListener {
        fun onEndOfScroll(position: Int)
        fun onNotEndOfScroll(position: Int)
    }

}
