/*
 * Created by Ershov Aleksandr on 9/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.wallet.assets.search_asset

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import androidx.core.view.ViewCompat
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.entity.MultiItemEntity
import com.jakewharton.rxbinding3.widget.textChanges
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.local.AssetBalanceMultiItemEntity
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.ui.home.wallet.assets.AssetsFragment
import com.wavesplatform.wallet.v2.ui.home.wallet.assets.details.AssetDetailsActivity
import com.wavesplatform.wallet.v2.util.launchActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_search_asset.*
import kotlinx.android.synthetic.main.content_empty_data.view.*
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import pers.victor.ext.click
import pers.victor.ext.gone
import pers.victor.ext.visiable
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class SearchAssetActivity : BaseActivity(), SearchAssetView {

    @Inject
    @InjectPresenter
    lateinit var presenter: SearchAssetPresenter

    @Inject
    lateinit var adapter: SearchAssetAdapter

    @ProvidePresenter
    fun providePresenter(): SearchAssetPresenter = presenter

    override fun configLayoutRes() = R.layout.activity_search_asset

    override fun onBackPressed() {
        finish()
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }

    override fun onViewReady(savedInstanceState: Bundle?) {
        setStatusBarColor(R.color.basic50)
        setNavigationBarColor(R.color.basic50)
        ViewCompat.setElevation(appbar_layout, 8F)
        clear_button.click {
            search_view.setText("")
            presenter.search("")
        }
        cancel_button.click { onBackPressed() }
        recycle_assets.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        adapter.bindToRecyclerView(recycle_assets)

        val emptyView = LayoutInflater.from(this)
                .inflate(R.layout.content_address_book_empty_state, null)
        emptyView.text_empty.text = getString(R.string.search_asset_empty)
        adapter.emptyView = emptyView

        adapter.onItemClickListener = BaseQuickAdapter.OnItemClickListener { _, _, position ->
            if (this.adapter.getItem(position) is AssetBalanceMultiItemEntity) {
                val item = this.adapter.getItem(position) as AssetBalanceMultiItemEntity
                launchActivity<AssetDetailsActivity>(AssetsFragment.REQUEST_ASSET_DETAILS) {
                    putExtra(AssetDetailsActivity.BUNDLE_ASSET_TYPE, item.itemType)
                    if (item.isHidden) {
                        putExtra(AssetDetailsActivity.BUNDLE_ASSET_POSITION,
                                position - HIDDEN_HEADER_SIZE)
                    } else {
                        putExtra(AssetDetailsActivity.BUNDLE_ASSET_POSITION, position)
                    }
                    putExtra(AssetDetailsActivity.BUNDLE_ASSET_SEARCH, search_view.text.toString())
                }
            }
        }

        eventSubscriptions.add(search_view.textChanges()
                .debounce(300, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
                .subscribe { search ->
                    if (TextUtils.isEmpty(search)) {
                        clear_button.gone()
                    } else {
                        clear_button.visiable()
                    }
                    presenter.search(search.toString())
                })

        search_view.requestFocus()
    }

    override fun onResume() {
        super.onResume()
        presenter.queryAllAssets()
        presenter.search(presenter.lastQuery)
    }

    override fun setSearchResult(list: List<MultiItemEntity>) {
        adapter.setNewData(list)
    }

    companion object {
        private const val HIDDEN_HEADER_SIZE = 1
    }
}
