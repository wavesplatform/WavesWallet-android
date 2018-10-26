package com.wavesplatform.wallet.v2.ui.home.dex.markets

import android.app.Activity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.jakewharton.rxbinding2.widget.RxTextView
import com.mindorks.editdrawabletext.DrawablePosition
import com.mindorks.editdrawabletext.onDrawableClickListener
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.remote.response.MarketResponse
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_dex_markets.*
import kotlinx.android.synthetic.main.header_dex_markets_layout.view.*
import pers.victor.ext.inflate
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

        recycle_markets.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                appbar_layout.isSelected = recycle_markets.canScrollVertically(-1)
            }
        })

        recycle_markets.layoutManager = LinearLayoutManager(this)
        adapter.bindToRecyclerView(recycle_markets)

        presenter.getMarkets()

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
            val item = this.adapter.getItem(position) as MarketResponse

            item.checked = !item.checked
            this.adapter.setData(position, item)
            this.adapter.allData[position] = item
        }
    }

    override fun afterSuccessGetMarkets(markets: MutableList<MarketResponse>) {
        progress_bar.hide()

        if (markets.isEmpty()) {
            adapter.removeAllHeaderView()
        } else {
            adapter.setHeaderView(getHeaderView())
        }

        adapter.allData = ArrayList(markets)
        adapter.setNewData(markets)
        adapter.setEmptyView(R.layout.address_book_empty_state)
    }

    private fun getHeaderView(): View? {
        val view = inflate(R.layout.header_dex_markets_layout)
        view.edit_search.setDrawableClickListener(object : onDrawableClickListener {
            override fun onClick(target: DrawablePosition) {
                when (target) {
                    DrawablePosition.RIGHT -> {
                        view.edit_search.text = null
                    }
                }
            }
        })

        eventSubscriptions.add(RxTextView.textChanges(view.edit_search)
                .skipInitialValue()
                .map {
                    if (it.isNotEmpty()) {
                        view.edit_search.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_search_24_basic_500, 0, R.drawable.ic_clear_24_basic_500, 0)
                    } else {
                        view.edit_search.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_search_24_basic_500, 0, 0, 0)
                    }
                    return@map it.toString()
                }
                .debounce(300, TimeUnit.MILLISECONDS)
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    adapter.filter(it)
                })

        return view
    }

    override fun onBackPressed() {
        presenter.saveSelectedMarkets(adapter.data)

        setResult(Activity.RESULT_OK)
        finish()
    }
}
