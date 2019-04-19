/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.wallet

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.base.view.BaseFragment
import com.wavesplatform.wallet.v2.ui.home.MainActivity
import com.wavesplatform.wallet.v2.ui.home.history.tab.HistoryTabFragment
import com.wavesplatform.wallet.v2.ui.home.wallet.assets.AssetsFragment
import com.wavesplatform.wallet.v2.ui.home.wallet.leasing.LeasingFragment
import com.wavesplatform.sdk.utils.notNull
import kotlinx.android.synthetic.main.fragment_wallet.*
import pers.victor.ext.gone
import pers.victor.ext.visiable
import pyxis.uzuki.live.richutilskt.utils.runDelayed
import javax.inject.Inject


class WalletFragment : BaseFragment(), WalletView, HistoryTabFragment.ChangeTabBarVisibilityListener {
    @Inject
    @InjectPresenter
    lateinit var presenter: WalletPresenter
    private lateinit var adapter: WalletFragmentPageAdapter
    private var onElevationAppBarChangeListener: MainActivity.OnElevationAppBarChangeListener? = null

    @ProvidePresenter
    fun providePresenter(): WalletPresenter = presenter

    override fun configLayoutRes(): Int = com.wavesplatform.wallet.R.layout.fragment_wallet

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val assetsFragment = AssetsFragment.newInstance()
        val leasingFragment = LeasingFragment.newInstance()

        assetsFragment.changeTabBarVisibilityListener = this
        leasingFragment.changeTabBarVisibilityListener = this

        adapter = WalletFragmentPageAdapter(
                childFragmentManager,
                arrayListOf(assetsFragment, leasingFragment),
                arrayOf(getString(com.wavesplatform.wallet.R.string.wallet_assets), getString(com.wavesplatform.wallet.R.string.wallet_leasing)))
    }

    companion object {
        fun newInstance(): WalletFragment {
            return WalletFragment()
        }
    }

    override fun onViewReady(savedInstanceState: Bundle?) {
        setupUI()

        presenter.checkNewAppUpdates()
    }

    fun setOnElevationChangeListener(listener: MainActivity.OnElevationAppBarChangeListener) {
        this.onElevationAppBarChangeListener = listener
    }

    private fun setupUI() {
        viewpager_wallet.adapter = adapter
        stl_wallet.setViewPager(viewpager_wallet)
        stl_wallet.currentTab = 0
        appbar_layout.addOnOffsetChangedListener(
                AppBarLayout.OnOffsetChangedListener { _, verticalOffset ->
                    onElevationAppBarChangeListener.notNull {
                        presenter.hideShadow = verticalOffset == 0
                        onElevationAppBarChangeListener?.onChange(presenter.hideShadow)
                        viewpager_wallet.setPagingEnabled(presenter.hideShadow)
                    }
                })
    }

    override fun changeTabBarVisibility(show: Boolean, onlyExpand: Boolean) {
        if (show) {
            appbar_layout.setExpanded(true, false)
            if (!onlyExpand) {
                appbar_layout.visiable()
            }
        } else {
            if (appbar_layout.visibility != View.GONE) {
                appbar_layout.setExpanded(false, false)
                if (!onlyExpand) {
                    runDelayed(100) {
                        appbar_layout.gone()
                    }
                }
            }
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            applyElevation()
        }
    }

    private fun applyElevation() {
        onElevationAppBarChangeListener?.let {
            onElevationAppBarChangeListener?.onChange(presenter.hideShadow)
        }
    }

    override fun afterCheckNewAppUpdates(needUpdate: Boolean) {
        if (needUpdate){
            info_alert.apply {
                setIcon(R.drawable.userimg_rocket_48)
                setTitle(R.string.need_update_alert_title)
                setDescription(R.string.need_update_alert_description)
                setActionIcon(R.drawable.ic_arrowright_14_basic_200)
                onAlertClick {
                    openAppInPlayMarket()
                }
            }.show()
        }
    }

    private fun openAppInPlayMarket() {
        val appPackageName = activity?.packageName
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName")))
        } catch (anfe: android.content.ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")))
        }


    }
}
