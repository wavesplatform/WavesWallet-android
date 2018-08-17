package com.wavesplatform.wallet.v2.ui.home

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.novoda.simplechromecustomtabs.SimpleChromeCustomTabs
import com.wavesplatform.wallet.BuildConfig
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v1.util.PrefsUtil
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.service.UpdateHistoryService
import com.wavesplatform.wallet.v2.ui.base.view.BaseDrawerActivity
import com.wavesplatform.wallet.v2.ui.home.dex.DexFragment
import com.wavesplatform.wallet.v2.ui.home.history.HistoryFragment
import com.wavesplatform.wallet.v2.ui.home.profile.ProfileFragment
import com.wavesplatform.wallet.v2.ui.home.quick_action.QuickActionBottomSheetFragment
import com.wavesplatform.wallet.v2.ui.home.wallet.WalletFragment
import com.wavesplatform.wallet.v2.util.makeLinks
import kotlinx.android.synthetic.main.activity_main_v2.*
import kotlinx.android.synthetic.main.dialog_account_first_open.view.*
import kotlinx.android.synthetic.main.fragment_bank.*
import pers.victor.ext.click
import pers.victor.ext.findColor
import javax.inject.Inject


class MainActivity : BaseDrawerActivity(), MainView, TabLayout.OnTabSelectedListener {

    //
    private val TAG_NOT_CENTRAL_TAB = "not_central_tab"
    private val TAG_CENTRAL_TAB = "central_tab"

    @Inject
    @InjectPresenter
    lateinit var presenter: MainPresenter
    var accountFirstOpenDialog: AlertDialog? = null



    @ProvidePresenter
    fun providePresenter(): MainPresenter = presenter

    override fun onViewReady(savedInstanceState: Bundle?) {
        setupToolbar(toolbar_general)
        needChangeStatusBarColorOnMenuOpen(false)

        //TODO: clear this
        authHelper.startMainActivityAndCreateNewDBIfKeyValid(this, BuildConfig.PUBLIC_KEY)

        showFirstOpenAlert(preferencesHelper.isAccountFirstOpen())

        setupBottomNavigation()

//        select the first tab
        onTabSelected(tab_navigation.getTabAt(0))
    }

    private fun showFirstOpenAlert(firstOpen: Boolean) {
        if (!firstOpen) {
            val alertDialogBuilder = AlertDialog.Builder(this)
            accountFirstOpenDialog = alertDialogBuilder
                    .setCancelable(false)
                    .setView(getFirstOpenAlertView())
                    .create()

            accountFirstOpenDialog?.show()
        }
    }

    private fun getFirstOpenAlertView(): View? {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_account_first_open, null)

        view.checkbox_terms_of_use.setOnCheckedChangeListener { buttonView, isChecked ->
            presenter.checkedAboutTerms = isChecked
            view.button_confirm.isEnabled = presenter.isAllCheckedToStart()
        }
        view.checkbox_backup.setOnCheckedChangeListener { buttonView, isChecked ->
            presenter.checkedAboutBackup = isChecked
            view.button_confirm.isEnabled = presenter.isAllCheckedToStart()
        }
        view.checkbox_funds_on_device.setOnCheckedChangeListener { buttonView, isChecked ->
            presenter.checkedAboutFundsOnDevice = isChecked
            view.button_confirm.isEnabled = presenter.isAllCheckedToStart()
        }

        val siteClick = object : ClickableSpan() {
            override fun onClick(p0: View?) {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(Constants.URL_TERMS))
                startActivity(browserIntent)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = findColor(R.color.black)
            }
        }

        view.text_about_terms.makeLinks(arrayOf(getString(R.string.dialog_account_first_open_about_terms_key)), arrayOf(siteClick))

        view.button_confirm.click {
            preferencesHelper.setAccountFirstOpen(true)
            accountFirstOpenDialog?.cancel()
        }

        return view
    }

    override fun configLayoutRes() = R.layout.activity_main_v2

    /**
     * Setup bottom navigation with custom tabs
     * **/
    private fun setupBottomNavigation() {
        tab_navigation.addTab(tab_navigation.newTab().setCustomView(getCustomView(R.drawable.ic_tabbar_wallet_default)).setTag(TAG_NOT_CENTRAL_TAB))
        tab_navigation.addTab(tab_navigation.newTab().setCustomView(getCustomView(R.drawable.ic_tabbar_dex_default)).setTag(TAG_NOT_CENTRAL_TAB))
        tab_navigation.addTab(tab_navigation.newTab().setCustomView(getCenterTabLayout(R.drawable.ic_tabbar_waves_default)).setTag(TAG_CENTRAL_TAB))
        tab_navigation.addTab(tab_navigation.newTab().setCustomView(getCustomView(R.drawable.ic_tabbar_history_default)).setTag(TAG_NOT_CENTRAL_TAB))
        tab_navigation.addTab(tab_navigation.newTab().setCustomView(getCustomView(R.drawable.ic_tabbar_profile_default)).setTag(TAG_NOT_CENTRAL_TAB))

        tab_navigation.addOnTabSelectedListener(this)
    }

    override fun onTabReselected(tab: TabLayout.Tab?) {
        when (tab?.position) {
        // Quick action
            2 -> {
                val quickActionDialog = QuickActionBottomSheetFragment()
                quickActionDialog.show(supportFragmentManager, quickActionDialog::class.java.simpleName)
            }
        }
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
//            Quick action
            2 -> {
                val quickActionDialog = QuickActionBottomSheetFragment()
                quickActionDialog.show(supportFragmentManager, quickActionDialog::class.java.simpleName)
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
