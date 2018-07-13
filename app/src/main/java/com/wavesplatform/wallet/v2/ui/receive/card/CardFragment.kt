package com.wavesplatform.wallet.v2.ui.receive.card

import android.os.Bundle
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.base.view.BaseFragment
import kotlinx.android.synthetic.main.fragment_card.*
import pers.victor.ext.click
import pers.victor.ext.gone
import pers.victor.ext.visiable
import javax.inject.Inject

class CardFragment :BaseFragment(),CardView{
    @Inject
    @InjectPresenter
    lateinit var presenter: CardPresenter

    @ProvidePresenter
    fun providePresenter(): CardPresenter = presenter

    override fun configLayoutRes(): Int = R.layout.fragment_card

    companion object {

        /**
         * @return CardFragment instance
         * */
        fun newInstance(): CardFragment {
            return CardFragment()
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
