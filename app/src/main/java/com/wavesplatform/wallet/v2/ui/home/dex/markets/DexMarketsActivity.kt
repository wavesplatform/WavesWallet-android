/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.dex.markets

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.ethanhua.skeleton.RecyclerViewSkeletonScreen
import com.ethanhua.skeleton.Skeleton
import com.jakewharton.rxbinding2.widget.RxTextView
import com.mindorks.editdrawabletext.DrawablePosition
import com.mindorks.editdrawabletext.OnDrawableClickListener
import com.vicpin.krealmextensions.delete
import com.vicpin.krealmextensions.save
import com.wavesplatform.sdk.model.response.matcher.MarketResponse
import com.wavesplatform.sdk.utils.notNull
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.model.db.userdb.MarketResponseDb
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.ui.home.dex.DexFragment.Companion.RESULT_NEED_UPDATE
import com.wavesplatform.wallet.v2.util.showError
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_dex_markets.*
import kotlinx.android.synthetic.main.content_empty_data.view.*
import pers.victor.ext.inflate
import pers.victor.ext.visiable
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class DexMarketsActivity : BaseActivity(), DexMarketsView {

    @Inject
    @InjectPresenter
    lateinit var presenter: DexMarketsPresenter

    @Inject
    lateinit var adapter: DexMarketsAdapter

    private var skeletonScreen: RecyclerViewSkeletonScreen? = null

    @ProvidePresenter
    fun providePresenter(): DexMarketsPresenter = presenter

    override fun configLayoutRes() = R.layout.activity_dex_markets

    override fun onCreate(savedInstanceState: Bundle?) {
        overridePendingTransition(R.anim.slide_in_right, R.anim.null_animation)
        super.onCreate(savedInstanceState)
    }

    override fun onViewReady(savedInstanceState: Bundle?) {
        setupToolbar(toolbar_view, true, getString(R.string.dex_markets_list_toolbar_title), R.drawable.ic_toolbar_back_black)

        recycle_markets.layoutManager = LinearLayoutManager(this)
        adapter.bindToRecyclerView(recycle_markets)

        edit_search.setDrawableClickListener(object : OnDrawableClickListener {
            override fun onClick(target: DrawablePosition) {
                when (target) {
                    DrawablePosition.RIGHT -> {
                        edit_search.text = null
                    }
                }
            }
        })

        eventSubscriptions.add(RxTextView.textChanges(edit_search)
                .skipInitialValue()
                .map {
                    if (it.isNotEmpty()) {
                        edit_search.setCompoundDrawablesWithIntrinsicBounds(
                                R.drawable.ic_search_24_basic_500, 0,
                                R.drawable.ic_clear_24_basic_500, 0)
                    } else {
                        edit_search.setCompoundDrawablesWithIntrinsicBounds(
                                R.drawable.ic_search_24_basic_500, 0,
                                0, 0)
                    }
                    return@map it.toString()
                }
                .debounce(500, TimeUnit.MILLISECONDS)
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { query ->
                    skeletonScreen?.show()
                    setSkeletonGradient()
                    presenter.search(query.trim())
                })

        edit_search.visiable()

        adapter.onItemChildClickListener = BaseQuickAdapter.OnItemChildClickListener { adapter, view, position ->
            val item = this.adapter.getItem(position) as MarketResponse

            when (view.id) {
                R.id.image_info -> {
                    val infoDialog = DexMarketInformationBottomSheetFragment()
                    infoDialog.withMarketInformation(item)
                    infoDialog.show(supportFragmentManager, infoDialog::class.java.simpleName)
                }
            }
        }

        adapter.onItemClickListener = BaseQuickAdapter.OnItemClickListener { adapter, view, position ->
            presenter.needToUpdate = true

            val item = this.adapter.getItem(position) as MarketResponse

            if (item.checked) {
                item.checked = false
                delete<MarketResponseDb> { equalTo("id", item.id) }
            } else {
                item.checked = true
                MarketResponseDb(item).save()
            }

            this.adapter.setData(position, item)
            this.adapter.allData[this.adapter.allData.indexOf(item)] = item
        }

        skeletonScreen = Skeleton.bind(recycle_markets)
                .adapter(recycle_markets.adapter)
                .shimmer(true)
                .count(5)
                .color(R.color.basic50)
                .load(R.layout.item_skeleton_markets)
                .frozen(false)
                .show()
        setSkeletonGradient()
        presenter.initLoad()
    }

    override fun afterSuccessGetMarkets(markets: MutableList<MarketResponse>) {
        skeletonScreen?.hide()
        adapter.allData = ArrayList(markets)
        adapter.setNewData(markets)
        adapter.emptyView = getEmptyView()
    }

    override fun afterFailGetMarkets() {
        skeletonScreen?.hide()
        showError(R.string.common_server_error, R.id.root)
    }

    private fun getEmptyView(): View {
        val view = inflate(R.layout.content_address_book_empty_state)
        view.text_empty.text = getString(R.string.dex_market_empty)
        return view
    }

    override fun onBackPressed() {
        setResult(Constants.RESULT_OK, Intent().apply {
            putExtra(RESULT_NEED_UPDATE, presenter.needToUpdate)
        })
        finish()
        overridePendingTransition(R.anim.null_animation, R.anim.slide_out_right)
    }

    override fun onDestroy() {
        skeletonScreen?.hide()
        super.onDestroy()
    }

    override fun needToShowNetworkMessage() = true

    private fun setSkeletonGradient() {
        recycle_markets?.post {
            recycle_markets?.layoutManager?.findViewByPosition(1)?.alpha = 0.7f
            recycle_markets?.layoutManager?.findViewByPosition(2)?.alpha = 0.5f
            recycle_markets?.layoutManager?.findViewByPosition(3)?.alpha = 0.4f
            recycle_markets?.layoutManager?.findViewByPosition(4)?.alpha = 0.2f
        }
    }
}
