/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.wallet

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.google.android.material.appbar.AppBarLayout
import androidx.core.view.ViewCompat
import androidx.viewpager.widget.ViewPager
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import com.wavesplatform.sdk.utils.notNull
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.analytics.AnalyticEvents
import com.wavesplatform.wallet.v2.data.analytics.analytics
import com.wavesplatform.wallet.v2.ui.base.view.BaseFragment
import com.wavesplatform.wallet.v2.ui.home.MainActivity
import com.wavesplatform.wallet.v2.ui.home.wallet.assets.AssetsFragment
import com.wavesplatform.wallet.v2.ui.home.wallet.leasing.LeasingFragment
import com.wavesplatform.wallet.v2.util.PrefsUtil
import kotlinx.android.synthetic.main.fragment_wallet.*
import kotlinx.android.synthetic.main.fragment_wallet.info_alert
import kotlinx.android.synthetic.main.fragment_wallet.view.*
import javax.inject.Inject


class WalletFragment : BaseFragment(), WalletView {

    @Inject
    @InjectPresenter
    lateinit var presenter: WalletPresenter
    private lateinit var adapter: WalletFragmentPageAdapter
    private var onElevationAppBarChangeListener: MainActivity.OnElevationAppBarChangeListener? = null

    @ProvidePresenter
    fun providePresenter(): WalletPresenter = presenter

    override fun configLayoutRes(): Int = R.layout.fragment_wallet

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val assetsFragment = AssetsFragment.newInstance()
        val leasingFragment = LeasingFragment.newInstance()

        val elevationAppBarChangeListener = object : MainActivity.OnElevationAppBarChangeListener {
            override fun onChange(elevateEnable: Boolean) {
                presenter.shadowEnable = elevateEnable
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
                presenter.notNull {
                    presenter.shadowEnable = (adapter.fragments[position] as WalletTabShadowListener).isShadowEnable()
                    enableElevation(presenter.shadowEnable)
                }
            }
        })

        stl_wallet.setCurrentTab(0, false)
    }

    fun setOnElevationChangeListener(listener: MainActivity.OnElevationAppBarChangeListener) {
        this.onElevationAppBarChangeListener = listener
    }

    private fun enableElevation(enable: Boolean) {
        onElevationAppBarChangeListener?.onChange(true)
        wallet_appbar_layout.notNull {
            if (enable) {
                ViewCompat.setZ(wallet_appbar_layout, 8F)
            } else {
                ViewCompat.setZ(wallet_appbar_layout, 0F)
            }
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
                    analytics.trackEvent(AnalyticEvents.WalletUpdateBannerEvent)
                    openAppInPlayMarket()
                }
            }.show()
            setScrollAlert(true)
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            enableElevation(presenter.shadowEnable)
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
                setScrollAlert(false)
            }
        }.show()
        setScrollAlert(true)
    }

    private fun setScrollAlert(scroll: Boolean) {
        val params = info_alert?.layoutParams as AppBarLayout.LayoutParams
        params.scrollFlags = if (scroll) {
            AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP
        } else {
            AppBarLayout.LayoutParams.SCROLL_FLAG_SNAP
        }
        info_alert.layoutParams = params
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
