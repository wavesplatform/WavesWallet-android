package com.wavesplatform.wallet.v2.ui.home.wallet

import android.os.Bundle
import android.support.v4.view.ViewPager
import android.view.Menu
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.flyco.tablayout.listener.OnTabSelectListener
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.base.view.BaseFragment
import kotlinx.android.synthetic.main.fragment_wallet.*
import javax.inject.Inject
import android.view.MenuInflater
import android.view.MenuItem
import pers.victor.ext.toast


class WalletFragment : BaseFragment(), WalletView{

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

    private fun setupUI() {
        viewpager_wallet.adapter = WalletFragmentPageAdapter(childFragmentManager, arrayOf(getString(R.string.wallet_assets), getString(R.string.wallet_leasing)))
        stl_wallet.setViewPager(viewpager_wallet)

        stl_wallet.currentTab = 0
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_wallet, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.action_sorting -> {
                toast(item.title)
            }
            R.id.action_your_address -> {
                toast(item.title)
            }
        }

        return super.onOptionsItemSelected(item)
    }
}
