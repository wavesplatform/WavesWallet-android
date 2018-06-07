package com.wavesplatform.wallet.v2.ui.home.dex

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.base.view.BaseFragment
import com.wavesplatform.wallet.v2.ui.home.dex.adapter.DexAdapter
import com.wavesplatform.wallet.v2.ui.home.wallet.assets.adapter.TestObject
import kotlinx.android.synthetic.main.fragment_dex_new.*
import java.util.*
import javax.inject.Inject

class DexFragment :BaseFragment(),DexView{

    @Inject
    @InjectPresenter
    lateinit var presenter: DexPresenter

    @ProvidePresenter
    fun providePresenter(): DexPresenter = presenter

    @Inject
    lateinit var adapter: DexAdapter

    override fun configLayoutRes(): Int = R.layout.fragment_dex_new

    companion object {

        /**
         * @return DexFragment instance
         * */
        fun newInstance(): DexFragment {
            return DexFragment()
        }
    }

    override fun onViewReady(savedInstanceState: Bundle?) {

        recycle_dex.layoutManager = LinearLayoutManager(baseActivity)
        recycle_dex.adapter = adapter
        recycle_dex.isNestedScrollingEnabled = false

        adapter.setNewData(arrayListOf(TestObject("Waves", Random().nextBoolean(), Random().nextBoolean(), Random().nextDouble(), Random().nextDouble()),
                TestObject("Waves", Random().nextBoolean(), Random().nextBoolean(), Random().nextDouble(), Random().nextDouble()),
                TestObject("Waves", Random().nextBoolean(), Random().nextBoolean(), Random().nextDouble(), Random().nextDouble()),
                TestObject("Waves", Random().nextBoolean(), Random().nextBoolean(), Random().nextDouble(), Random().nextDouble()),
                TestObject("Waves", Random().nextBoolean(), Random().nextBoolean(), Random().nextDouble(), Random().nextDouble()),
                TestObject("Waves", Random().nextBoolean(), Random().nextBoolean(), Random().nextDouble(), Random().nextDouble()),
                TestObject("Waves", Random().nextBoolean(), Random().nextBoolean(), Random().nextDouble(), Random().nextDouble()),
                TestObject("Waves", Random().nextBoolean(), Random().nextBoolean(), Random().nextDouble(), Random().nextDouble())))
    }
}
