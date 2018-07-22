package com.wavesplatform.wallet.v2.ui.home.wallet

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.base.view.BaseFragment
import com.wavesplatform.wallet.v2.ui.home.wallet.address.MyAddressQRActivity
import com.wavesplatform.wallet.v2.ui.home.wallet.assets.sorting.AssetsSortingActivity
import com.wavesplatform.wallet.v2.util.launchActivity
import kotlinx.android.synthetic.main.fragment_wallet.*
import pers.victor.ext.dp2px
import pers.victor.ext.gone
import pers.victor.ext.visiable
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

    private fun setupUI() {
        viewpager_wallet.adapter = WalletFragmentPageAdapter(childFragmentManager, arrayOf(getString(R.string.wallet_assets), getString(R.string.wallet_leasing)))
        stl_wallet.setViewPager(viewpager_wallet)
        appbar_layout.addOnOffsetChangedListener({ appBarLayout, verticalOffset ->
            val offsetForShowShadow = appbar_layout.totalScrollRange - dp2px(9)
            if (-verticalOffset > offsetForShowShadow) {
                viewpager_wallet.setPagingEnabled(false)
                view_shadow.visiable()
            } else {
                viewpager_wallet.setPagingEnabled(true)
                view_shadow.gone()
            }
        })
        stl_wallet.currentTab = 0
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_wallet, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_sorting -> {
                launchActivity<AssetsSortingActivity>()
            }
            R.id.action_your_address -> {
                launchActivity<MyAddressQRActivity>()
            }
        }

        return super.onOptionsItemSelected(item)
    }
}
