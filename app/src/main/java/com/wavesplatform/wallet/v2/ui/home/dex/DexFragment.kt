package com.wavesplatform.wallet.v2.ui.home.dex

import android.os.Bundle
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.base.view.BaseFragment
import javax.inject.Inject

class DexFragment :BaseFragment(),DexView{

    @Inject
    @InjectPresenter
    lateinit var presenter: DexPresenter

    @ProvidePresenter
    fun providePresenter(): DexPresenter = presenter

    override fun configLayoutRes(): Int = R.layout.fragment_dex

    companion object {

        /**
         * @return DexFragment instance
         * */
        fun newInstance(): DexFragment {
            return DexFragment()
        }
    }

    override fun onViewReady(savedInstanceState: Bundle?) {
    }
}
