package com.wavesplatform.wallet.v2.ui.home.wallet.leasing

import android.os.Bundle
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.base.view.BaseFragment
import javax.inject.Inject

class LeasingFragment : BaseFragment(), LeasingView {

    @Inject
    @InjectPresenter
    lateinit var presenter: LeasingPresenter

    @ProvidePresenter
    fun providePresenter(): LeasingPresenter = presenter

    companion object {

        /**
         * @return LeasingFragment instance
         * */
        fun newInstance(): LeasingFragment {
            return LeasingFragment()
        }
    }

    override fun configLayoutRes(): Int = R.layout.fragment_leasing

    override fun onViewReady(savedInstanceState: Bundle?) {
    }
}
