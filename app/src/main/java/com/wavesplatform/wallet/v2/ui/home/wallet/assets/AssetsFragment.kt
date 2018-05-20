package com.wavesplatform.wallet.v2.ui.home.wallet.assets

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.base.view.BaseFragment
import com.wavesplatform.wallet.v2.ui.home.wallet.assets.adapter.AssetsAdapter
import com.wavesplatform.wallet.v2.ui.home.wallet.assets.adapter.TestObject
import kotlinx.android.synthetic.main.fragment_assets.*
import pers.victor.ext.click
import pers.victor.ext.hideInputMethod
import java.util.*
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
        setupUI()
    }

    private fun setupUI() {
        recycle_assets_not_hidden.layoutManager = LinearLayoutManager(baseActivity)
        recycle_assets_not_hidden.adapter = adapter
        recycle_assets_not_hidden.isNestedScrollingEnabled = false

        recycle_assets_hidden.layoutManager = LinearLayoutManager(baseActivity)
        recycle_assets_hidden.adapter = adapterHiddenAssets
        recycle_assets_hidden.isNestedScrollingEnabled = false

        adapter.setNewData(arrayListOf(TestObject("Waves", Random().nextBoolean(), Random().nextBoolean(), Random().nextDouble(), Random().nextDouble()),
                TestObject("Waves", Random().nextBoolean(), Random().nextBoolean(), Random().nextDouble(), Random().nextDouble()),
                TestObject("Waves", Random().nextBoolean(), Random().nextBoolean(), Random().nextDouble(), Random().nextDouble()),
                TestObject("Waves", Random().nextBoolean(), Random().nextBoolean(), Random().nextDouble(), Random().nextDouble()),
                TestObject("Waves", Random().nextBoolean(), Random().nextBoolean(), Random().nextDouble(), Random().nextDouble()),
                TestObject("Waves", Random().nextBoolean(), Random().nextBoolean(), Random().nextDouble(), Random().nextDouble()),
                TestObject("Waves", Random().nextBoolean(), Random().nextBoolean(), Random().nextDouble(), Random().nextDouble()),
                TestObject("Waves", Random().nextBoolean(), Random().nextBoolean(), Random().nextDouble(), Random().nextDouble())))

        adapterHiddenAssets.setNewData(arrayListOf(TestObject("Waves", Random().nextBoolean(), Random().nextBoolean(), Random().nextDouble(), Random().nextDouble()),
                TestObject("Waves", Random().nextBoolean(), Random().nextBoolean(), Random().nextDouble(), Random().nextDouble()),
                TestObject("Waves", Random().nextBoolean(), Random().nextBoolean(), Random().nextDouble(), Random().nextDouble()),
                TestObject("Waves", Random().nextBoolean(), Random().nextBoolean(), Random().nextDouble(), Random().nextDouble()),
                TestObject("Waves", Random().nextBoolean(), Random().nextBoolean(), Random().nextDouble(), Random().nextDouble()),
                TestObject("Waves", Random().nextBoolean(), Random().nextBoolean(), Random().nextDouble(), Random().nextDouble()),
                TestObject("Waves", Random().nextBoolean(), Random().nextBoolean(), Random().nextDouble(), Random().nextDouble()),
                TestObject("Waves", Random().nextBoolean(), Random().nextBoolean(), Random().nextDouble(), Random().nextDouble())))

        text_hidden_assets.click {
            if (expandable_layout.isExpanded) {
                expandable_layout.collapse()
                image_arrowup.animate()
                        .rotation(180f)
                        .setDuration(500)
                        .start()
            } else {
                expandable_layout.expand()
                image_arrowup.animate()
                        .rotation(0f)
                        .setDuration(500)
                        .start()
            }
        }

        text_hidden_assets.text = getString(R.string.hidden_assets,adapterHiddenAssets.data.size.toString())
    }

}
