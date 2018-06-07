package com.wavesplatform.wallet.v2.ui.home

import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.ui.base.view.BaseDrawerActivity
import com.wavesplatform.wallet.v2.ui.home.dex.DexFragment
import com.wavesplatform.wallet.v2.ui.home.history.HistoryFragment
import com.wavesplatform.wallet.v2.ui.home.profile.ProfileFragment
import com.wavesplatform.wallet.v2.ui.home.wallet.WalletFragment
import kotlinx.android.synthetic.main.activity_main_v2.*
import javax.inject.Inject


class MainActivity : BaseDrawerActivity(), MainView, TabLayout.OnTabSelectedListener {

    //
    private val TAG_NOT_CENTRAL_TAB = "not_central_tab"
    private val TAG_CENTRAL_TAB = "central_tab"

    @Inject
    @InjectPresenter
    lateinit var presenter: MainPresenter


    @ProvidePresenter
    fun providePresenter(): MainPresenter = presenter

    override fun onViewReady(savedInstanceState: Bundle?) {
        setupToolbar(toolbar_general)
        needChangeStatusBarColorOnMenuOpen(false)
//        presenter.loadBalancesAndTransactions()

        setupBottomNavigation()

//        select the first tab
        onTabSelected(tab_navigation.getTabAt(0))
    }

    override fun configLayoutRes() = R.layout.activity_main_v2

    /**
     * Setup bottom navigation with custom tabs
     * **/
    private fun setupBottomNavigation() {
        tab_navigation.addTab(tab_navigation.newTab().setCustomView(getCustomView(R.drawable.ic_tabbar_wallet)).setTag(TAG_NOT_CENTRAL_TAB))
        tab_navigation.addTab(tab_navigation.newTab().setCustomView(getCustomView(R.drawable.ic_tabbar_dex)).setTag(TAG_NOT_CENTRAL_TAB))
        tab_navigation.addTab(tab_navigation.newTab().setCustomView(getCenterTabLayout(R.drawable.ic_tabbar_waves)).setTag(TAG_CENTRAL_TAB))
        tab_navigation.addTab(tab_navigation.newTab().setCustomView(getCustomView(R.drawable.ic_tabbar_history)).setTag(TAG_NOT_CENTRAL_TAB))
        tab_navigation.addTab(tab_navigation.newTab().setCustomView(getCustomView(R.drawable.ic_tabbar_profile)).setTag(TAG_NOT_CENTRAL_TAB))

        tab_navigation.addOnTabSelectedListener(this)
    }

    override fun onTabReselected(tab: TabLayout.Tab?) {

    }

    override fun onTabUnselected(tab: TabLayout.Tab?) {

    }

    override fun onTabSelected(tab: TabLayout.Tab?) {
        when (tab?.position) {
//            Wallet screen
            0 -> {
                openFragment(R.id.frame_fragment_container, WalletFragment.newInstance())
                toolbar_general.title = getString(R.string.wallet_toolbar_title)
            }
//            DEX screen
            1 -> {
                openFragment(R.id.frame_fragment_container, DexFragment.newInstance())
                toolbar_general.title = getString(R.string.dex_toolbar_title)
            }
//            History screen
            3 -> {
                openFragment(R.id.frame_fragment_container, HistoryFragment.newInstance())
                toolbar_general.title = getString(R.string.history_toolbar_title)
            }
//            Profile screen
            4 -> {
                openFragment(R.id.frame_fragment_container, ProfileFragment.newInstance())
                toolbar_general.title = getString(R.string.profile_toolbar_title)
            }
        }

//        tab?.position = 2 where center tab
        if (tab?.position != 2) {
            selectedTabs(tab)
        }
    }

    /**
     * Here we change tabs except for the central
     * @param selectedTab - the tab which we selected, other tabs will be default
     * **/
    private fun selectedTabs(selectedTab: TabLayout.Tab?) {
        for (i in 0 until tab_navigation.tabCount) {
//            if i == 2 need continue iteration because this is central tab
            if (i == 2) continue

            val tab = tab_navigation.getTabAt(i)
            if (tab == selectedTab) {
                changeTabIcon(selectedTab!!, true)
            } else {
                changeTabIcon(tab!!, false)
            }
        }
    }

    /**
     * Method that changes the color of the tab icon
     * @param selectedTab -  tab with layout and ImageView with id == R.id.image_tab_icon
     * @param isSelected - if true -> the color of the icon will be {R.color.home_tab_active} otherwise {R.color.home_tab_inactive}
     * **/
    private fun changeTabIcon(selectedTab: TabLayout.Tab, isSelected: Boolean) {
        val imageTabIcon = selectedTab.customView?.findViewById<ImageView>(R.id.image_tab_icon)
        if (isSelected) {
            imageTabIcon?.setColorFilter(ContextCompat.getColor(this, R.color.home_tab_active))
        } else {
            imageTabIcon?.setColorFilter(ContextCompat.getColor(this, R.color.home_tab_inactive))
        }
    }

    /**
     * Returns custom tab layout
     * @param tabIcon
     * **/
    private fun getCustomView(tabIcon: Int): View? {
        val customTab = LayoutInflater.from(this).inflate(R.layout.home_navigation_tab, null)
        val imageTabIcon = customTab.findViewById<ImageView>(R.id.image_tab_icon)

        imageTabIcon.setImageResource(tabIcon)
        return customTab
    }

    /**
     * Returns central tab layout
     * @param tabIcon
     * **/
    private fun getCenterTabLayout(tabIcon: Int): View? {
        var customTab = LayoutInflater.from(this).inflate(R.layout.home_navigation_center_tab, null)
        val imageTabIcon = customTab.findViewById<ImageView>(R.id.image_tab_icon)

        imageTabIcon.setImageResource(tabIcon)
        return customTab
    }
}
