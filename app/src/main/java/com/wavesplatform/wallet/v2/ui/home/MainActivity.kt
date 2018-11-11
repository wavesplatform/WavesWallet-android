package com.wavesplatform.wallet.v2.ui.home

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewCompat
import android.support.v7.app.AlertDialog
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.R.id.tab_navigation
import com.wavesplatform.wallet.v1.util.PrefsUtil
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.Events
import com.wavesplatform.wallet.v2.data.model.local.HistoryTab
import com.wavesplatform.wallet.v2.ui.base.view.BaseDrawerActivity
import com.wavesplatform.wallet.v2.ui.home.dex.DexFragment
import com.wavesplatform.wallet.v2.ui.home.history.HistoryFragment
import com.wavesplatform.wallet.v2.ui.home.history.tab.HistoryTabFragment
import com.wavesplatform.wallet.v2.ui.home.profile.ProfileFragment
import com.wavesplatform.wallet.v2.ui.home.profile.backup.BackupPhraseActivity
import com.wavesplatform.wallet.v2.ui.home.quick_action.QuickActionBottomSheetFragment
import com.wavesplatform.wallet.v2.ui.home.wallet.WalletFragment
import com.wavesplatform.wallet.v2.util.launchActivity
import com.wavesplatform.wallet.v2.util.makeLinks
import com.wavesplatform.wallet.v2.util.notNull
import kotlinx.android.synthetic.main.activity_main_v2.*
import kotlinx.android.synthetic.main.dialog_account_first_open.view.*
import pers.victor.ext.click
import pers.victor.ext.findColor
import pers.victor.ext.gone
import pers.victor.ext.visiable
import javax.inject.Inject


class MainActivity : BaseDrawerActivity(), MainView, TabLayout.OnTabSelectedListener {

    @Inject
    @InjectPresenter
    lateinit var presenter: MainPresenter
    private var accountFirstOpenDialog: AlertDialog? = null
    private val fragments = arrayListOf<Fragment>()
    private var activeFragment = Fragment()

    @ProvidePresenter
    fun providePresenter(): MainPresenter = presenter

    override fun configLayoutRes() = R.layout.activity_main_v2

    override fun onViewReady(savedInstanceState: Bundle?) {
        setStatusBarColor(R.color.basic50)
        setNavigationBarColor(R.color.white)
        setupToolbar(toolbar_general)

        showFirstOpenAlert(prefsUtil.getValue(PrefsUtil.KEY_ACCOUNT_FIRST_OPEN, true))

        setupBottomNavigation()


        if (savedInstanceState == null) {
            onTabSelected(tab_navigation.getTabAt(WALLET_SCREEN))
        }

        eventSubscriptions.add(mRxEventBus.filteredObservable(Events.SpamFilterStateChanged::class.java)
                .subscribe {
                    presenter.reloadTransactionsAfterSpamSettingsChanged()
                })

        eventSubscriptions.add(mRxEventBus.filteredObservable(Events.SpamFilterUrlChanged::class.java)
                .subscribe {
                    if (it.updateTransaction) {
                        presenter.reloadTransactionsAfterSpamSettingsChanged(true)
                    }
                })
    }

    override fun onResume() {
        super.onResume()
        showBackUpSeedWarning()
    }

    private fun showFirstOpenAlert(firstOpen: Boolean) {
        if (firstOpen) {
            val alertDialogBuilder = AlertDialog.Builder(this)
            accountFirstOpenDialog = alertDialogBuilder
                    .setCancelable(false)
                    .setView(getFirstOpenAlertView())
                    .create()

            accountFirstOpenDialog?.show()
        }
    }

    private fun getFirstOpenAlertView(): View? {
        val view = LayoutInflater.from(this)
                .inflate(R.layout.dialog_account_first_open, null)

        view.checkbox_terms_of_use
                .setOnCheckedChangeListener { _, isChecked ->
                    presenter.checkedAboutTerms = isChecked
                    view.button_confirm.isEnabled = presenter.isAllCheckedToStart()
                }
        view.checkbox_backup
                .setOnCheckedChangeListener { _, isChecked ->
                    presenter.checkedAboutBackup = isChecked
                    view.button_confirm.isEnabled = presenter.isAllCheckedToStart()
                }
        view.checkbox_funds_on_device
                .setOnCheckedChangeListener { _, isChecked ->
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

        view.text_about_terms.makeLinks(
                arrayOf(getString(R.string.dialog_account_first_open_about_terms_key)),
                arrayOf(siteClick))

        view.button_confirm.click {
            prefsUtil.setValue(PrefsUtil.KEY_ACCOUNT_FIRST_OPEN, false)
            accountFirstOpenDialog?.cancel()
            showBackUpSeedWarning()
        }

        return view
    }

    fun enableElevation(enable: Boolean) {
        if (enable) {
            ViewCompat.setElevation(appbar_layout, 0F)
        } else {
            ViewCompat.setElevation(appbar_layout, 8F)
        }
    }


    /**
     * Setup bottom navigation with custom tabs
     * **/
    private fun setupBottomNavigation() {
        tab_navigation.addTab(tab_navigation.newTab().setCustomView(
                getCustomView(R.drawable.ic_tabbar_wallet_default)).setTag(TAG_NOT_CENTRAL_TAB))
        tab_navigation.addTab(tab_navigation.newTab().setCustomView(
                getCustomView(R.drawable.ic_tabbar_dex_default)).setTag(TAG_NOT_CENTRAL_TAB))
        tab_navigation.addTab(tab_navigation.newTab().setCustomView(
                getCenterTabLayout(R.drawable.ic_tabbar_waves_default)).setTag(TAG_CENTRAL_TAB))
        tab_navigation.addTab(tab_navigation.newTab().setCustomView(
                getCustomView(R.drawable.ic_tabbar_history_default)).setTag(TAG_NOT_CENTRAL_TAB))
        tab_navigation.addTab(tab_navigation.newTab().setCustomView(
                getCustomView(R.drawable.ic_tabbar_profile_default)).setTag(TAG_NOT_CENTRAL_TAB))

        tab_navigation.addOnTabSelectedListener(this)

        setupBottomNavigationFragments()
    }

    private fun setupBottomNavigationFragments() {
        val bundle = Bundle().apply {
            val tabs = arrayListOf(
                    HistoryTab(HistoryTabFragment.all, getString(R.string.history_all)),
                    HistoryTab(HistoryTabFragment.send, getString(R.string.history_sent)),
                    HistoryTab(HistoryTabFragment.received, getString(R.string.history_received)),
                    HistoryTab(HistoryTabFragment.exchanged, getString(R.string.history_exchanged)),
                    HistoryTab(HistoryTabFragment.leased, getString(R.string.history_leased)),
                    HistoryTab(HistoryTabFragment.issued, getString(R.string.history_issued)))
            putParcelableArrayList(HistoryFragment.BUNDLE_TABS, tabs)
        }

        val walletFragment = WalletFragment.newInstance()
        val dexFragment = DexFragment.newInstance()
        val historyFragment = HistoryFragment.newInstance().apply {
            arguments = bundle
        }
        val profileFragment = ProfileFragment.newInstance()

        val elevationListener = object : OnElevationAppBarChangeListener {
            override fun onChange(elevateEnable: Boolean) {
                enableElevation(elevateEnable)
            }
        }

        walletFragment.setOnElevationChangeListener(elevationListener)
        dexFragment.setOnElevationChangeListener(elevationListener)
        historyFragment.setOnElevationChangeListener(elevationListener)
        profileFragment.setOnElevationChangeListener(elevationListener)


        fragments.add(walletFragment)
        fragments.add(dexFragment)
        fragments.add(QuickActionBottomSheetFragment.newInstance())
        fragments.add(historyFragment)
        fragments.add(profileFragment)

        activeFragment = fragments[WALLET_SCREEN]
    }

    override fun onTabReselected(tab: TabLayout.Tab?) {
        tab?.position.notNull { position ->
            mRxEventBus.post(Events.ScrollToTopEvent(position))
        }
        when (tab?.position) {
            QUICK_ACTION_SCREEN -> {
                val quickActionDialog = QuickActionBottomSheetFragment()
                quickActionDialog.show(supportFragmentManager,
                        quickActionDialog::class.java.simpleName)
            }
        }
    }

    override fun onTabUnselected(tab: TabLayout.Tab?) {

    }

    override fun onTabSelected(tab: TabLayout.Tab?) {
        when (tab?.position) {
            WALLET_SCREEN -> {
                showNewTabFragment(fragments[WALLET_SCREEN])
                toolbar_general.title = getString(R.string.wallet_toolbar_title)
            }
            DEX_SCREEN -> {
                showNewTabFragment(fragments[DEX_SCREEN])
                toolbar_general.title = getString(R.string.dex_toolbar_title)
            }
            QUICK_ACTION_SCREEN -> {
                val quickActionDialog = fragments[QUICK_ACTION_SCREEN]
                if (quickActionDialog is QuickActionBottomSheetFragment) {
                    quickActionDialog.show(supportFragmentManager,
                            quickActionDialog::class.java.simpleName)
                }
            }
            HISTORY_SCREEN -> {
                showNewTabFragment(fragments[HISTORY_SCREEN])
                toolbar_general.title = getString(R.string.history_toolbar_title)
            }
            PROFILE_SCREEN -> {
                showNewTabFragment(fragments[PROFILE_SCREEN])
                toolbar_general.title = getString(R.string.profile_toolbar_title)
            }
        }

        if (tab?.position != QUICK_ACTION_SCREEN) {
            selectedTabs(tab)
        }
    }

    private fun showNewTabFragment(fragment: Fragment) {
        if (!supportFragmentManager.fragments.contains(fragment)) {
            supportFragmentManager.beginTransaction().hide(activeFragment).add(R.id.frame_fragment_container, fragment, fragment::class.java.simpleName).show(fragment).commitAllowingStateLoss()
        } else {
            supportFragmentManager.beginTransaction().hide(activeFragment).show(fragment).commitAllowingStateLoss()
        }
        activeFragment = fragment
    }

    /**
     * Here we change tabs except for the central
     * @param selectedTab - the tab which we selected, other tabs will be default
     * **/
    private fun selectedTabs(selectedTab: TabLayout.Tab?) {
        for (i in 0 until tab_navigation.tabCount) {
            if (i == QUICK_ACTION_SCREEN) {
                continue
            }

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
     * @param isSelected - if true -> the color of the icon will be {R.color.home_tab_active}
     * otherwise {R.color.home_tab_inactive}
     * **/
    private fun changeTabIcon(selectedTab: TabLayout.Tab, isSelected: Boolean) {
        val imageTabIcon = selectedTab.customView?.findViewById<ImageView>(R.id.image_tab_icon)
        if (isSelected) {
            imageTabIcon?.setColorFilter(ContextCompat
                    .getColor(this, R.color.home_tab_active))
        } else {
            imageTabIcon?.setColorFilter(ContextCompat
                    .getColor(this, R.color.home_tab_inactive))
        }
    }

    /**
     * Returns custom tab layout
     * @param tabIcon
     * **/
    private fun getCustomView(tabIcon: Int): View? {
        val customTab = LayoutInflater.from(this)
                .inflate(R.layout.home_navigation_tab, null)
        val imageTabIcon = customTab.findViewById<ImageView>(R.id.image_tab_icon)

        imageTabIcon.setImageResource(tabIcon)
        return customTab
    }

    /**
     * Returns central tab layout
     * @param tabIcon
     * **/
    private fun getCenterTabLayout(tabIcon: Int): View? {
        val customTab = LayoutInflater.from(this)
                .inflate(R.layout.home_navigation_center_tab, null)
        val imageTabIcon = customTab.findViewById<ImageView>(R.id.image_tab_icon)

        imageTabIcon.setImageResource(tabIcon)
        return customTab
    }

    private fun showBackUpSeedWarning() {
        if (!prefsUtil.getValue(PrefsUtil.KEY_ACCOUNT_FIRST_OPEN, true)
                && App.getAccessManager().isCurrentAccountBackupSkipped()) {
            val currentGuid = App.getAccessManager().getLastLoggedInGuid()
            val lastTime = preferencesHelper.getShowSaveSeedWarningTime(currentGuid)
            val now = System.currentTimeMillis()
            if (now > lastTime + MIN_15) {
                warning_container.visiable()
                warning_container.click {
                    it.gone()
                    launchActivity<BackupPhraseActivity> {
                        putExtra(ProfileFragment.KEY_INTENT_SET_BACKUP, true)
                    }
                }
                preferencesHelper.setShowSaveSeedWarningTime(currentGuid, now)
            }
        }
    }

    companion object {
        const val WALLET_SCREEN = 0
        const val DEX_SCREEN = 1
        const val QUICK_ACTION_SCREEN = 2
        const val HISTORY_SCREEN = 3
        const val PROFILE_SCREEN = 4

        private const val TAG_NOT_CENTRAL_TAB = "not_central_tab"
        private const val TAG_CENTRAL_TAB = "central_tab"

        private const val MIN_15 = 54_000_000L
    }

    interface OnElevationAppBarChangeListener {
        fun onChange(elevateEnable: Boolean)
    }
}
