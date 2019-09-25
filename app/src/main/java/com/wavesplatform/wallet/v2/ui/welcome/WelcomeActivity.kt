/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.welcome

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.asksira.loopingviewpager.LoopingViewPager
import com.wavesplatform.sdk.utils.notNull
import com.wavesplatform.wallet.BuildConfig
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.local.Language
import com.wavesplatform.wallet.v2.ui.auth.choose_account.ChooseAccountActivity
import com.wavesplatform.wallet.v2.ui.auth.import_account.ImportAccountActivity
import com.wavesplatform.wallet.v2.ui.auth.new_account.NewAccountActivity
import com.wavesplatform.wallet.v2.ui.base.view.BaseDrawerActivity
import com.wavesplatform.wallet.v2.ui.language.change_welcome.ChangeLanguageBottomSheetFragment
import com.wavesplatform.wallet.v2.util.ClientEnvironment
import com.wavesplatform.wallet.v2.util.EnvironmentManager
import com.wavesplatform.wallet.v2.util.launchActivity
import com.wavesplatform.wallet.v2.util.makeStyled
import kotlinx.android.synthetic.main.activity_welcome.*
import pers.victor.ext.click
import pers.victor.ext.visiable
import javax.inject.Inject
import kotlin.math.abs

class WelcomeActivity : BaseDrawerActivity(), WelcomeView {

    @Inject
    @InjectPresenter
    lateinit var presenter: WelcomePresenter

    private var menu: Menu? = null

    @ProvidePresenter
    fun providePresenter(): WelcomePresenter = presenter

    override fun configLayoutRes() = R.layout.activity_welcome

    override fun askPassCode() = false

    override fun onViewReady(savedInstanceState: Bundle?) {
        setStatusBarColor(R.color.basic50)
        setNavigationBarColor(R.color.basic50)
        setupToolbar(toolbar_view)

        button_create_account.click {
            launchActivity<NewAccountActivity>(requestCode = REQUEST_NEW_ACCOUNT, options = createDataBundle())
            overridePendingTransition(R.anim.slide_in_right, R.anim.null_animation)
        }

        relative_import_acc.click {
            launchActivity<ImportAccountActivity>(REQUEST_IMPORT_ACC)
            overridePendingTransition(R.anim.slide_in_right, R.anim.null_animation)
        }

        view_pager_tutorial.setPageTransformer(false) { page, position ->
            val root = page.findViewById<LinearLayout>(R.id.linear_root)
            if (position <= -1.0F || position >= 1.0F) {
                root.alpha = 0.0F
            } else if (position == 0.0F) {
                root.alpha = 1.0F
            } else {
                root.alpha = 1.0F - abs(position)
            }
        }

        view_pager_tutorial.adapter = WelcomeItemsPagerAdapter(this, presenter.getTutorialSliderData(), true)
        view_pager_tutorial.offscreenPageLimit = 5
        view_pager_indicator.count = view_pager_tutorial.indicatorCount
        view_pager_tutorial.setIndicatorPageChangeListener(object : LoopingViewPager.IndicatorPageChangeListener {
            override fun onIndicatorProgress(selectingPosition: Int, progress: Float) {
                view_pager_indicator.setProgress(selectingPosition, progress)
            }

            override fun onIndicatorPageChange(newIndicatorPosition: Int) {
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_change_language -> {
                val dialog = ChangeLanguageBottomSheetFragment()
                dialog.languageChooseListener = object : ChangeLanguageBottomSheetFragment.LanguageSelectListener {
                    override fun onLanguageSelected(lang: String) {
                        menu.notNull {
                            presenter.saveLanguage(lang)
                            setLanguage(Language.getLocale(lang))
                        }
                    }
                }
                dialog.show(supportFragmentManager, dialog::class.java.simpleName)
                return true
            }
            R.id.action_change_env -> {
                showChooseEnvironmentDialog()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showChooseEnvironmentDialog() {
        val servers = ClientEnvironment.environments
        val serversTitles = servers.map { getString(it.externalProperties.environmentDisplayName) }.toTypedArray()

        var selectedServer = EnvironmentManager.environment
        val selectedServerPosition = servers.indexOfFirst { it.name == selectedServer.name }

        val chooseEnvironmentDialog = AlertDialog.Builder(this)
                .setTitle(getString(R.string.choose_environment_dialog_title))
                .setSingleChoiceItems(serversTitles, selectedServerPosition) { dialog, which ->
                    selectedServer = servers[which]
                }
                .setPositiveButton(getString(R.string.choose_environment_dialog_positive_txt)) { dialog, which ->
                    EnvironmentManager.setCurrentEnvironment(selectedServer)
                }
                .setNegativeButton(getString(R.string.choose_environment_dialog_negative_txt), null)
                .create()

        chooseEnvironmentDialog.show()
        chooseEnvironmentDialog.makeStyled()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        this.menu = menu
        menuInflater.inflate(R.menu.menu_language, menu)
        menu.findItem(R.id.action_change_env).isVisible = BuildConfig.DEBUG || preferencesHelper.isDeveloper()
        updateMenuTitle()
        return super.onCreateOptionsMenu(menu)
    }

    private fun updateMenuTitle() {
        menu?.findItem(R.id.action_change_language)?.title = Language.getLanguageByCode(preferencesHelper.getLanguage()).iso
    }

    override fun onBackPressed() {
        if (slidingRootNav.isMenuOpened) {
            slidingRootNav.closeMenu(true)
        } else {
            exit()
        }
    }

    private fun createDataBundle(): Bundle {
        val options = Bundle()
        options.putString("animation", "left_slide")
        return options
    }

    companion object {
        var REQUEST_NEW_ACCOUNT = 55
        var REQUEST_SIGN_IN = 56
        var REQUEST_IMPORT_ACC = 57
    }
}
