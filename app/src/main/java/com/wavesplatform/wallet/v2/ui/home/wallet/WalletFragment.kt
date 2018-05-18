package com.wavesplatform.wallet.v2.ui.home.wallet

import android.os.Bundle
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.base.view.BaseFragment
import kotlinx.android.synthetic.main.fragment_wallet.*
import javax.inject.Inject

class WalletFragment : BaseFragment(), WalletView {

    @Inject
    @InjectPresenter
    lateinit var presenter: WalletPresenter

    @ProvidePresenter
    fun providePresenter(): WalletPresenter = presenter

    override fun configLayoutRes(): Int = R.layout.fragment_wallet

    companion object {

        /**
         * @return WalletFragment instance
         * */
        fun newInstance(): WalletFragment {
            return WalletFragment()
        }
    }

    override fun onViewReady(savedInstanceState: Bundle?) {
        setupUI()
    }

    private fun setupUI(){
        stl_wallet.setTabData(arrayOf(getString(R.string.assets),getString(R.string.leasing)))
    }
}
