package com.wavesplatform.wallet.v2.ui.home.wallet.assets

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.ui.base.view.BaseFragment
import com.wavesplatform.wallet.v2.ui.home.wallet.assets.adapter.AssetsAdapter
import kotlinx.android.synthetic.main.fragment_assets.*
import pers.victor.ext.click
import pers.victor.ext.goneIf
import pers.victor.ext.visiableIf
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
            runDelayed(3000, {
                swipe_container?.isRefreshing = false
            })
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
//
//        adapter.setNewData(arrayListOf(TestObject("Waves", Random().nextBoolean(), Random().nextBoolean(), Random().nextDouble(), Random().nextDouble()),
//                TestObject("Bitcoin", Random().nextBoolean(), Random().nextBoolean(), Random().nextDouble(), Random().nextDouble()),
//                TestObject("Ethereum", Random().nextBoolean(), Random().nextBoolean(), Random().nextDouble(), Random().nextDouble()),
//                TestObject("Euro", Random().nextBoolean(), Random().nextBoolean(), Random().nextDouble(), Random().nextDouble()),
//                TestObject("Dollar", Random().nextBoolean(), Random().nextBoolean(), Random().nextDouble(), Random().nextDouble()),
//                TestObject("Litecoin", Random().nextBoolean(), Random().nextBoolean(), Random().nextDouble(), Random().nextDouble()),
//                TestObject("Dash", Random().nextBoolean(), Random().nextBoolean(), Random().nextDouble(), Random().nextDouble()),
//                TestObject("Monero", Random().nextBoolean(), Random().nextBoolean(), Random().nextDouble(), Random().nextDouble())))
//
//        adapterHiddenAssets.setNewData(arrayListOf(TestObject("Waves", Random().nextBoolean(), Random().nextBoolean(), Random().nextDouble(), Random().nextDouble()),
//                TestObject("Waves", Random().nextBoolean(), Random().nextBoolean(), Random().nextDouble(), Random().nextDouble()),
//                TestObject("Waves", Random().nextBoolean(), Random().nextBoolean(), Random().nextDouble(), Random().nextDouble()),
//                TestObject("Waves", Random().nextBoolean(), Random().nextBoolean(), Random().nextDouble(), Random().nextDouble()),
//                TestObject("Waves", Random().nextBoolean(), Random().nextBoolean(), Random().nextDouble(), Random().nextDouble()),
//                TestObject("Waves", Random().nextBoolean(), Random().nextBoolean(), Random().nextDouble(), Random().nextDouble()),
//                TestObject("Waves", Random().nextBoolean(), Random().nextBoolean(), Random().nextDouble(), Random().nextDouble()),
//                TestObject("Waves", Random().nextBoolean(), Random().nextBoolean(), Random().nextDouble(), Random().nextDouble())))
//
//        spamAssetsAdapter.setNewData(arrayListOf(TestObject("Waves", Random().nextBoolean(), Random().nextBoolean(), Random().nextDouble(), Random().nextDouble(), true),
//                TestObject("Waves", Random().nextBoolean(), Random().nextBoolean(), Random().nextDouble(), Random().nextDouble(), true),
//                TestObject("Waves", Random().nextBoolean(), Random().nextBoolean(), Random().nextDouble(), Random().nextDouble(), true),
//                TestObject("Waves", Random().nextBoolean(), Random().nextBoolean(), Random().nextDouble(), Random().nextDouble(), true),
//                TestObject("Waves", Random().nextBoolean(), Random().nextBoolean(), Random().nextDouble(), Random().nextDouble(), true)))

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

    override fun afterSuccessLoadAssets(assets: List<AssetBalance>) {
        adapter.setNewData(assets)
    }

    override fun afterSuccessLoadHiddenAssets(assets: List<AssetBalance>) {
        expandable_layout_hidden.visiableIf { assets.isNotEmpty() }
        relative_hidden_block.visiableIf { assets.isNotEmpty() }
        adapterHiddenAssets.setNewData(assets)
    }

    override fun afterSuccessLoadSpamAssets(assets: List<AssetBalance>) {
        expandable_layout_spam.visiableIf { assets.isNotEmpty() }
        relative_spam_block.visiableIf { assets.isNotEmpty() }
        spamAssetsAdapter.setNewData(assets)
    }


}
