package com.wavesplatform.wallet.v2.ui.home.wallet

import android.os.Bundle
import android.support.v4.view.ViewPager
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.flyco.tablayout.listener.OnTabSelectListener
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.base.view.BaseFragment
import com.wavesplatform.wallet.v2.ui.home.wallet.assets.AssetsFragment
import com.wavesplatform.wallet.v2.ui.home.wallet.leasing.LeasingFragment
import kotlinx.android.synthetic.main.fragment_wallet.*
import javax.inject.Inject

class WalletFragment : BaseFragment(), WalletView, OnTabSelectListener, ViewPager.OnPageChangeListener {

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
        stl_wallet.setTabData(arrayOf(getString(R.string.assets), getString(R.string.leasing)))

        viewpager_wallet.adapter = WalletFragmentPageAdapter(childFragmentManager)

        stl_wallet.setOnTabSelectListener(this)
        viewpager_wallet.addOnPageChangeListener(this)

        stl_wallet.currentTab = 0
    }

    override fun onTabSelect(position: Int) {
        viewpager_wallet.currentItem = position
    }

    override fun onPageSelected(position: Int) {
        stl_wallet.currentTab = position
    }

    override fun onTabReselect(position: Int) {}

    override fun onPageScrollStateChanged(state: Int) {}

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

}
