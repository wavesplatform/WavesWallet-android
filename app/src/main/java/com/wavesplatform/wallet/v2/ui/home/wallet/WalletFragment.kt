package com.wavesplatform.wallet.v2.ui.home.wallet

import android.os.Bundle
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.base.view.BaseFragment
import com.wavesplatform.wallet.v2.ui.home.MainActivity
import com.wavesplatform.wallet.v2.util.notNull
import kotlinx.android.synthetic.main.fragment_wallet.*
import javax.inject.Inject


class WalletFragment : BaseFragment(), WalletView {

    @Inject
    @InjectPresenter
    lateinit var presenter: WalletPresenter
    private lateinit var adapter: WalletFragmentPageAdapter
    private var onElevationChangeListener: MainActivity.OnElevationChangeListener? = null

    @ProvidePresenter
    fun providePresenter(): WalletPresenter = presenter

    override fun configLayoutRes(): Int = R.layout.fragment_wallet

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter = WalletFragmentPageAdapter(
                childFragmentManager,
                arrayOf(getString(R.string.wallet_assets), getString(R.string.wallet_leasing)))
    }

    companion object {
        fun newInstance(): WalletFragment {
            return WalletFragment()
        }
    }

    override fun onViewReady(savedInstanceState: Bundle?) {
        setupUI()
    }

    fun setOnElevationChangeListener(listener: MainActivity.OnElevationChangeListener) {
        this.onElevationChangeListener = listener
    }

    private fun setupUI() {
        viewpager_wallet.adapter = adapter
        stl_wallet.setViewPager(viewpager_wallet)
        stl_wallet.currentTab = 0
        appbar_layout.addOnOffsetChangedListener { _, verticalOffset ->
            onElevationChangeListener.notNull {
                onElevationChangeListener?.onChange(verticalOffset == 0)
            }
        }
    }
}
