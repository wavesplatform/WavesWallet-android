package com.wavesplatform.wallet.v2.ui.whats_new

import android.content.Context
import android.support.v4.view.PagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.local.WhatsNewItem
import com.wavesplatform.wallet.v2.injection.qualifier.ApplicationContext
import kotlinx.android.synthetic.main.pager_whats_new_item.view.*
import javax.inject.Inject

class WhatsNewAdapter @Inject constructor(@ApplicationContext var mContext: Context) : PagerAdapter() {
    var items: ArrayList<WhatsNewItem> = arrayListOf()

    override fun instantiateItem(collection: ViewGroup, position: Int): Any {
        val inflater = LayoutInflater.from(mContext)
        val view = inflater.inflate(R.layout.pager_whats_new_item, null, false)

        // TODO: Load photo
        view.image_new_icon.setImageResource(items[position].image)
//        view.text_new_title.text = mContext.getString(items[position].title)
//        view.text_new_description.text = mContext.getString(items[position].description)

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
}
