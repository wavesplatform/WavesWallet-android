package com.wavesplatform.wallet.v2.ui.receive.cryptocurrency

import android.os.Bundle
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.base.view.BaseFragment
import kotlinx.android.synthetic.main.fragment_cryptocurrency.*
import pers.victor.ext.click
import pers.victor.ext.gone
import pers.victor.ext.visiable
import javax.inject.Inject

class СryptocurrencyFragment : BaseFragment(), СryptocurrencyView {

    @Inject
    @InjectPresenter
    lateinit var presenter: СryptocurrencyPresenter

    @ProvidePresenter
    fun providePresenter(): СryptocurrencyPresenter = presenter

    override fun configLayoutRes(): Int = R.layout.fragment_cryptocurrency

    companion object {

        /**
         * @return СryptocurrencyFragment instance
         * */
        fun newInstance(): СryptocurrencyFragment {
            return СryptocurrencyFragment()
        }
    }

    override fun onViewReady(savedInstanceState: Bundle?) {

        edit_asset.click {
            edit_asset.gone()
            container_asset.visiable()
            container_info.visiable()
            button_continue.isEnabled = true
//            TODO open asset list screen
        }
    }


}
