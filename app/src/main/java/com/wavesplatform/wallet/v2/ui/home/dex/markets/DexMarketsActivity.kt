package com.wavesplatform.wallet.v2.ui.home.dex.markets

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.jakewharton.rxbinding2.widget.RxTextView
import com.mindorks.editdrawabletext.DrawablePosition
import com.mindorks.editdrawabletext.onDrawableClickListener
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.model.remote.response.MarketResponse
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.ui.home.dex.DexFragment.Companion.RESULT_NEED_UPDATE
import com.wavesplatform.wallet.v2.util.showError
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_dex_markets.*
import kotlinx.android.synthetic.main.layout_empty_data.view.*
import pers.victor.ext.gone
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

        presenter.getMarkets()

        edit_search.setDrawableClickListener(object : onDrawableClickListener {
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
                        edit_search.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_search_24_basic_500, 0, R.drawable.ic_clear_24_basic_500, 0)
                    } else {
                        edit_search.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_search_24_basic_500, 0, 0, 0)
                    }
                    return@map it.toString()
                }
                .debounce(500, TimeUnit.MILLISECONDS)
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    adapter.filter(it)
                })

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

            item.checked = !item.checked
            this.adapter.setData(position, item)
            this.adapter.allData[this.adapter.allData.indexOf(item)] = item
        }
    }

    override fun afterSuccessGetMarkets(markets: MutableList<MarketResponse>) {
        progress_bar.hide()

        adapter.allData = ArrayList(markets)
        adapter.setNewData(markets)

        if (markets.isEmpty()) {
            edit_search.gone()
        } else {
            edit_search.visiable()
        }

        adapter.emptyView = getEmptyView()
    }

    override fun afterFailGetMarkets() {
        progress_bar.hide()
        showError(R.string.common_server_error, R.id.root)
    }

    private fun getEmptyView(): View {
        val view = inflate(R.layout.address_book_empty_state)
        view.text_empty.text = getString(R.string.dex_market_empty)
        return view
    }

    override fun onBackPressed() {
        if (this.adapter.allData.isNotEmpty()) {
            presenter.saveSelectedMarkets(this.adapter.allData)
        }

        setResult(Constants.RESULT_OK, Intent().apply {
            putExtra(RESULT_NEED_UPDATE, presenter.needToUpdate)
        })
        finish()
        overridePendingTransition(R.anim.null_animation, R.anim.slide_out_right)
    }

    override fun onDestroy() {
        progress_bar?.hide()
        super.onDestroy()
    }

    override fun needToShowNetworkMessage() = true
}
