package com.wavesplatform.wallet.v2.ui.home.dex.markets

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.jakewharton.rxbinding2.widget.RxTextView
import com.mindorks.editdrawabletext.DrawablePosition
import com.mindorks.editdrawabletext.onDrawableClickListener
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.remote.response.Market
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_dex_markets.*
import pers.victor.ext.gone
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class DexMarketsActivity : BaseActivity(), DexMarketsView {

    @Inject
    @InjectPresenter
    lateinit var presenter: DexMarketsPresenter

    @Inject
    lateinit var adapter: DexMarketsAdapter


    @ProvidePresenter
    fun providePresenter(): DexMarketsPresenter = presenter

    override fun configLayoutRes() = R.layout.activity_dex_markets


    override fun onViewReady(savedInstanceState: Bundle?) {
        setupToolbar(toolbar_view, true, getString(R.string.dex_markets_list_toolbar_title), R.drawable.ic_toolbar_back_black)


        eventSubscriptions.add(RxTextView.textChanges(edit_search)
                .skipInitialValue()
                .map {
                    if (it.isNotEmpty()) {
                        edit_search.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_search_24_basic_500, 0, R.drawable.ic_clear_24_basic_500, 0)
                    } else {
                        edit_search.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_search_24_basic_500, 0, 0, 0)
                    }
                    return@map it.toString()
                }
                .debounce(300, TimeUnit.MILLISECONDS)
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    adapter.filter(it)
                })

        recycle_markets.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                appbar_layout.isSelected = recycle_markets.canScrollVertically(-1)
            }
        })


        edit_search.setDrawableClickListener(object : onDrawableClickListener {
            override fun onClick(target: DrawablePosition) {
                when (target) {
                    DrawablePosition.RIGHT -> {
                        edit_search.text = null
                    }
                }
            }
        })

        recycle_markets.layoutManager = LinearLayoutManager(this)
        recycle_markets.adapter = adapter
        adapter.bindToRecyclerView(recycle_markets)

        presenter.getMarkets()

        adapter.onItemChildClickListener = BaseQuickAdapter.OnItemChildClickListener { adapter, view, position ->
            val item = this.adapter.getItem(position) as Market

            when (view.id) {
                R.id.image_info -> {
                    val infoDialog = DexMarketInformationBottomSheetFragment()
                    infoDialog.withMarketInformation(item)
                    infoDialog.show(supportFragmentManager, infoDialog::class.java.simpleName)
                }
            }
        }

        adapter.onItemClickListener = BaseQuickAdapter.OnItemClickListener { adapter, view, position ->
            val item = this.adapter.getItem(position) as Market

            item.checked = !item.checked
            this.adapter.setData(position, item)
            this.adapter.allData[position] = item
        }
    }

    override fun afterSuccessGetMarkets(markets: MutableList<Market>) {
        progress_bar.hide()
        if (markets.isEmpty()) {
            edit_search.gone()
        }
        adapter.allData = ArrayList(markets)
        adapter.setNewData(markets)
        adapter.setEmptyView(R.layout.address_book_empty_state)
    }
}
