/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.wallet

import android.content.Intent
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.support.v4.view.ViewCompat
import android.support.v4.view.ViewPager
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.R // todo check
import com.wavesplatform.wallet.v2.ui.base.view.BaseFragment
import com.wavesplatform.wallet.v2.ui.home.MainActivity
import com.wavesplatform.wallet.v2.ui.home.wallet.assets.AssetsFragment
import com.wavesplatform.wallet.v2.ui.home.wallet.leasing.LeasingFragment
import com.wavesplatform.wallet.v2.util.PrefsUtil
import kotlinx.android.synthetic.main.fragment_wallet.*
import javax.inject.Inject


class WalletFragment : BaseFragment(), WalletView {

    @Inject
    @InjectPresenter
    lateinit var presenter: WalletPresenter
    private lateinit var adapter: WalletFragmentPageAdapter

    @ProvidePresenter
    fun providePresenter(): WalletPresenter = presenter

    override fun configLayoutRes(): Int = R.layout.fragment_wallet

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val assetsFragment = AssetsFragment.newInstance()
        val leasingFragment = LeasingFragment.newInstance()

        val elevationAppBarChangeListener = object : MainActivity.OnElevationAppBarChangeListener {
            override fun onChange(elevateEnable: Boolean) {
                enableElevation(elevateEnable)
            }
        }

        assetsFragment.elevationAppBarChangeListener = elevationAppBarChangeListener
        leasingFragment.elevationAppBarChangeListener = elevationAppBarChangeListener

        adapter = WalletFragmentPageAdapter(
                childFragmentManager,
                arrayListOf(assetsFragment, leasingFragment),
                arrayOf(getString(R.string.wallet_assets), getString(R.string.wallet_leasing)))
    }

    companion object {
        fun newInstance(): WalletFragment {
            return WalletFragment()
        }
    }

    override fun onViewReady(savedInstanceState: Bundle?) {
        setupUI()
        presenter.showTopBannerIfNeed()
    }

    private fun setupUI() {
        viewpager_wallet.adapter = adapter
        stl_wallet.setViewPager(viewpager_wallet)

        viewpager_wallet.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(p0: Int) {
                // do nothing
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                // do nothing
            }

            override fun onPageSelected(position: Int) {
                val enable = if (position == 0) {
                    (adapter.fragments[position] as AssetsFragment).presenter.enableElevation
                } else {
                    (adapter.fragments[position] as LeasingFragment).presenter.enableElevation
                }
                enableElevation(enable)
            }
        })

        stl_wallet.setCurrentTab(0, false)
    }

    private fun enableElevation(enable: Boolean) {
        if (enable) {
            ViewCompat.setZ(wallet_appbar_layout, 8F)
        } else {
            ViewCompat.setZ(wallet_appbar_layout, 0F)
        }
    }

    override fun afterCheckNewAppUpdates(needUpdate: Boolean) {
        if (needUpdate) {
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

    override fun afterCheckClearedWallet() {
        info_alert.apply {
            setTitle(R.string.clean_banner_title)
            setDescription(R.string.clean_banner_description)
            setActionIcon(R.drawable.ic_clear_14_basic_300)
            onAlertClick {
                info_alert.hide()
                presenter.prefsUtil.setValue(PrefsUtil.KEY_IS_CLEARED_ALERT_ALREADY_SHOWN, true)
                presenter.showTopBannerIfNeed()
            }
        }.show()
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
