package com.wavesplatform.wallet.v2.ui.home.wallet.assets

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.ui.base.view.BaseFragment
import com.wavesplatform.wallet.v2.ui.home.wallet.assets.details.AssetDetailsActivity
import com.wavesplatform.wallet.v2.util.launchActivity
import com.wavesplatform.wallet.v2.util.notNull
import kotlinx.android.synthetic.main.fragment_assets.*
import pers.victor.ext.click
import pers.victor.ext.gone
import pers.victor.ext.visiable
import pyxis.uzuki.live.richutilskt.utils.runDelayed
import javax.inject.Inject

class AssetsFragment : BaseFragment(), AssetsView {

    @Inject
    @InjectPresenter
    lateinit var presenter: AssetsPresenter

    @ProvidePresenter
    fun providePresenter(): AssetsPresenter = presenter

    @Inject
    lateinit var adapter: AssetsAdapter

    @Inject
    lateinit var adapterHiddenAssets: AssetsAdapter

    @Inject
    lateinit var spamAssetsAdapter: AssetsAdapter

    companion object {

        /**
         * @return AssetsFragment instance
         * */
        fun newInstance(): AssetsFragment {
            return AssetsFragment()
        }
    }

    override fun configLayoutRes(): Int = R.layout.fragment_assets

    override fun onViewReady(savedInstanceState: Bundle?) {
        presenter.loadAssetsBalance()

        setupUI()
    }

    private fun setupUI() {
        swipe_container.setColorSchemeResources(R.color.submit400)
        swipe_container.setOnRefreshListener {
            presenter.loadAssetsBalance()
        }

        recycle_assets_not_hidden.layoutManager = LinearLayoutManager(baseActivity)
        recycle_assets_not_hidden.adapter = adapter
        recycle_assets_not_hidden.isNestedScrollingEnabled = false

        recycle_assets_hidden.layoutManager = LinearLayoutManager(baseActivity)
        recycle_assets_hidden.adapter = adapterHiddenAssets
        recycle_assets_hidden.isNestedScrollingEnabled = false

        recycle_spam_assets.layoutManager = LinearLayoutManager(baseActivity)
        recycle_spam_assets.adapter = spamAssetsAdapter
        recycle_spam_assets.isNestedScrollingEnabled = false

        adapter.onItemClickListener = BaseQuickAdapter.OnItemClickListener { adapter, view, position ->
            val item = adapter.getItem(position) as AssetBalance
            launchActivity<AssetDetailsActivity> {
                putExtra(AssetDetailsActivity.BUNDLE_ASSET_BALANCE_ITEM, item)
                putExtra(AssetDetailsActivity.BUNDLE_ASSET_POSITION, position)
            }
        }

        adapterHiddenAssets.onItemClickListener = BaseQuickAdapter.OnItemClickListener { adapter, view, position ->
            val item = adapter.getItem(position) as AssetBalance
            launchActivity<AssetDetailsActivity> {
                putExtra(AssetDetailsActivity.BUNDLE_ASSET_BALANCE_ITEM, item)
                putExtra(AssetDetailsActivity.BUNDLE_ASSET_POSITION, position + this@AssetsFragment.adapter.data.size)
            }
        }

        spamAssetsAdapter.onItemClickListener = BaseQuickAdapter.OnItemClickListener { adapter, view, position ->
            val item = adapter.getItem(position) as AssetBalance
            launchActivity<AssetDetailsActivity> {
                putExtra(AssetDetailsActivity.BUNDLE_ASSET_BALANCE_ITEM, item)
            }
        }

        text_hidden_assets.click {
            if (expandable_layout_hidden.isExpanded) {
                expandable_layout_hidden.collapse()
                image_arrowup.animate()
                        .rotation(180f)
                        .setDuration(500)
                        .start()
            } else {
                expandable_layout_hidden.expand()
                image_arrowup.animate()
                        .rotation(0f)
                        .setDuration(500)
                        .start()
            }
        }

        text_spam_assets.click {
            if (expandable_layout_spam.isExpanded) {
                expandable_layout_spam.collapse()
                image_arrowup_spam.animate()
                        .rotation(180f)
                        .setDuration(500)
                        .start()
            } else {
                expandable_layout_spam.expand()
                image_arrowup_spam.animate()
                        .rotation(0f)
                        .setDuration(500)
                        .start()
            }
        }

        text_hidden_assets.text = getString(R.string.wallet_assets_hidden_category, adapterHiddenAssets.data.size.toString())
        text_spam_assets.text = getString(R.string.wallet_assets_spam_category, spamAssetsAdapter.data.size.toString())
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        val item = menu.findItem(R.id.action_sorting)
        item.isVisible = true
    }

    override fun afterSuccessLoadAssets(assets: List<AssetBalance>) {
        runDelayed(1000, {
            swipe_container.notNull { swipe_container.isRefreshing = false }
        })
        adapter.setNewData(assets)
    }

    override fun afterSuccessLoadHiddenAssets(assets: List<AssetBalance>) {
        if (assets.isNotEmpty()) {
            expandable_layout_hidden.visiable()
            relative_hidden_block.visiable()
        } else {
            expandable_layout_hidden.gone()
            relative_hidden_block.gone()
        }

        adapterHiddenAssets.setNewData(assets)
        text_hidden_assets.text = getString(R.string.wallet_assets_hidden_category, assets.size.toString())
    }

    override fun afterSuccessLoadSpamAssets(assets: List<AssetBalance>) {
        if (assets.isNotEmpty()) {
            expandable_layout_spam.visiable()
            relative_spam_block.visiable()
        } else {
            expandable_layout_spam.gone()
            relative_spam_block.gone()
        }
        spamAssetsAdapter.setNewData(assets)
        text_spam_assets.text = getString(R.string.wallet_assets_spam_category, assets.size.toString())
    }


}
