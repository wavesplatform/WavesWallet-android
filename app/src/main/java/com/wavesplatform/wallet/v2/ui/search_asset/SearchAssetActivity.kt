/*
 * Created by Ershov Aleksandr on 9/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.search_asset

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.jakewharton.rxbinding2.widget.RxTextView
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.ui.home.wallet.assets.AssetsFragment
import com.wavesplatform.wallet.v2.ui.home.wallet.assets.details.AssetDetailsActivity
import com.wavesplatform.wallet.v2.util.launchActivity
import kotlinx.android.synthetic.main.activity_search_asset.*
import pers.victor.ext.click
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

    override fun onViewReady(savedInstanceState: Bundle?) {
        cancel_button.click { finish() }
        recycle_assets.layoutManager = LinearLayoutManager(this)
        adapter.bindToRecyclerView(recycle_assets)
        adapter.onItemClickListener = BaseQuickAdapter.OnItemClickListener { adapter, view, position ->
            val item = this.adapter.getItem(position) as AssetBalance
            launchActivity<AssetDetailsActivity>(AssetsFragment.REQUEST_ASSET_DETAILS) {
                putExtra(AssetDetailsActivity.BUNDLE_ASSET_TYPE, item.itemType)
                putExtra(AssetDetailsActivity.BUNDLE_ASSET_POSITION, position)
            }
        }

        eventSubscriptions.add(RxTextView.textChanges(search_view)
                .subscribe { search ->
                    val searchEmpty = !TextUtils.isEmpty(search)
                    if (searchEmpty) {
                        search(search.toString())
                    }
                })

        search("")
    }

    private fun search(query: String) {
        val list = if (TextUtils.isEmpty(query)) {
            presenter.queryAllAssetBalance()
        } else {
            val queryLower = query.toLowerCase()
            presenter.queryAllAssetBalance().filter {
                it.assetId.toLowerCase().contains(queryLower)
                        || it.getName().toLowerCase().contains(queryLower)
                        || it.issueTransaction?.name?.toLowerCase()?.contains(queryLower) ?: false
                        || it.issueTransaction?.assetId?.toLowerCase()?.contains(queryLower) ?: false
                        || it.assetId == Constants.findByGatewayId(query.toUpperCase())?.assetId
            }
        }
        adapter.setNewData(list)
    }
}
