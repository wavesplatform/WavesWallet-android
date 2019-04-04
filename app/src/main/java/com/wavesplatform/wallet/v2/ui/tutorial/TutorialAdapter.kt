/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.tutorial

import android.content.Context
import android.support.v4.view.PagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ScrollView
import com.jakewharton.rxbinding2.view.RxView
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.injection.qualifier.ApplicationContext
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.item_tutorial_6_card_confirm.view.*
import java.util.concurrent.TimeUnit
import com.wavesplatform.sdk.utils.notNull
import javax.inject.Inject


class TutorialAdapter @Inject constructor(@ApplicationContext var mContext: Context, var items: ArrayList<Int>) : PagerAdapter() {
    var listener: TutorialListener? = null
    private var scrollView: ScrollView? = null
    private var scrollListener: ViewTreeObserver.OnScrollChangedListener? = null
    private var subscriptions = CompositeDisposable()
    private var checkedAboutFundsOnDevice = false
    private var checkedAboutBackup = false
    private var checkedAboutTerms = false

    override fun instantiateItem(collection: ViewGroup, position: Int): Any {
        val view = LayoutInflater.from(mContext).inflate(items[position], null, false)

        view.scroll_root?.let {
            scrollListener = ViewTreeObserver.OnScrollChangedListener {
                if (!it.canScrollVertically(1)) {
                    listener?.onEndOfScroll(position)
                } else {
                    listener?.onNotEndOfScroll(position)
                }
            }
            it.viewTreeObserver.addOnScrollChangedListener(scrollListener)
        }

        if (position == items.size - 1) { // confirm and begin screen
            view.checkbox_funds_on_device.setOnCheckedChangeListener { _, isChecked ->
                checkedAboutBackup = isChecked
                listener?.canBegin(isAllCheckedToStart())
            }

            view.checkbox_backup.setOnCheckedChangeListener { _, isChecked ->
                checkedAboutFundsOnDevice = isChecked
                listener?.canBegin(isAllCheckedToStart())
            }

            view.checkbox_terms_of_use.setOnCheckedChangeListener { _, isChecked ->
                checkedAboutTerms = isChecked
                listener?.canBegin(isAllCheckedToStart())
            }

            subscriptions.add(RxView.clicks(view.text_terms_of_use)
                    .throttleFirst(1500, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        listener?.onSiteClicked(Constants.URL_TERMS)
                    })

            subscriptions.add(RxView.clicks(view.text_terms_and_conditions)
                    .throttleFirst(1500, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        listener?.onSiteClicked(Constants.URL_TERMS_AND_CONDITIONS)
                    })
        }

        collection.addView(view)
        return view
    }

    fun isAllCheckedToStart(): Boolean {
        return checkedAboutBackup && checkedAboutFundsOnDevice && checkedAboutTerms
    }

    override fun destroyItem(collection: ViewGroup, position: Int, view: Any) {
        scrollView?.viewTreeObserver?.removeOnScrollChangedListener(scrollListener)

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

    interface TutorialListener {
        fun onEndOfScroll(position: Int)
        fun onNotEndOfScroll(position: Int)
        fun onSiteClicked(site: String)
        fun canBegin(allCheckedToStart: Boolean)
    }
}
