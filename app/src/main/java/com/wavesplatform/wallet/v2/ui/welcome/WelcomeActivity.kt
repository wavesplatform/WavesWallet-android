package com.wavesplatform.wallet.v2.ui.welcome

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.asksira.loopingviewpager.LoopingViewPager
import com.wavesplatform.wallet.BuildConfig
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v1.ui.auth.EnvironmentManager
import com.wavesplatform.wallet.v2.data.model.local.Language
import com.wavesplatform.wallet.v2.data.model.local.WelcomeItem
import com.wavesplatform.wallet.v2.ui.auth.choose_account.ChooseAccountActivity
import com.wavesplatform.wallet.v2.ui.auth.import_account.ImportAccountActivity
import com.wavesplatform.wallet.v2.ui.auth.new_account.NewAccountActivity
import com.wavesplatform.wallet.v2.ui.base.view.BaseDrawerActivity
import com.wavesplatform.wallet.v2.ui.language.change_welcome.ChangeLanguageBottomSheetFragment
import com.wavesplatform.wallet.v2.util.launchActivity
import com.wavesplatform.wallet.v2.util.notNull
import kotlinx.android.synthetic.main.activity_welcome.*
import pers.victor.ext.click
import pers.victor.ext.visiable
import java.util.*
import javax.inject.Inject

class WelcomeActivity : BaseDrawerActivity(), WelcomeView {

    @Inject
    @InjectPresenter
    lateinit var presenter: WelcomePresenter

    private var menu: Menu? = null

    @ProvidePresenter
    fun providePresenter(): WelcomePresenter = presenter

    override fun configLayoutRes() = R.layout.activity_welcome

    override fun askPassCode() = false

    private fun createDataBundle(): Bundle {
        val options = Bundle()
        options.putString("animation", "left_slide")
        return options
    }

    override fun onViewReady(savedInstanceState: Bundle?) {
        setStatusBarColor(R.color.basic50)
        setNavigationBarColor(R.color.basic50)
        setupToolbar(toolbar_view)

        button_create_account.click {
            launchActivity<NewAccountActivity>(requestCode = REQUEST_NEW_ACCOUNT, options = createDataBundle())
            overridePendingTransition(R.anim.slide_in_right, R.anim.null_animation)
        }

        relative_sign_in.click {
            launchActivity<ChooseAccountActivity>(requestCode = REQUEST_SIGN_IN)
            overridePendingTransition(R.anim.slide_in_right, R.anim.null_animation)
        }

        relative_import_acc.click {
            launchActivity<ImportAccountActivity>(REQUEST_IMPORT_ACC)
            overridePendingTransition(R.anim.slide_in_right, R.anim.null_animation)
        }

        view_pager.setPageTransformer(false) { page, position ->
            val root = page.findViewById<LinearLayout>(R.id.linear_root)
            if (position <= -1.0F || position >= 1.0F) {
                root.alpha = 0.0F
            } else if (position == 0.0F) {
                root.alpha = 1.0F
            } else {
                root.alpha = 1.0F - Math.abs(position)
            }
        }
        view_pager.adapter = WelcomeItemsPagerAdapter(this, populateList(), true)
        view_pager.offscreenPageLimit = 5
        view_pager_indicator.count = view_pager.indicatorCount
        view_pager.setIndicatorPageChangeListener(object : LoopingViewPager.IndicatorPageChangeListener {
            override fun onIndicatorProgress(selectingPosition: Int, progress: Float) {
                view_pager_indicator.setProgress(selectingPosition, progress)
            }

            override fun onIndicatorPageChange(newIndicatorPosition: Int) {
            }
        })
        setEnvButton()
    }

    private fun setEnvButton() {
        if (BuildConfig.DEBUG) {
            button_switch_net.visiable()
            val newEnvironment = when (EnvironmentManager.environmentName) {
                EnvironmentManager.KEY_ENV_MAIN_NET -> {
                    button_switch_net.text = getString(R.string.welcome_switch_to_test)
                    EnvironmentManager.Environment.TEST_NET
                }
                EnvironmentManager.KEY_ENV_TEST_NET -> {
                    button_switch_net.text = getString(R.string.welcome_switch_to_prod)
                    EnvironmentManager.Environment.MAIN_NET
                }
                else -> {
                    button_switch_net.text = getString(R.string.welcome_switch_to_test)
                    EnvironmentManager.Environment.TEST_NET
                }
            }
            button_switch_net.click {
                button_switch_net.isEnabled = false
                EnvironmentManager.setCurrentEnvironment(newEnvironment)
            }
        }
    }

    private fun populateList(): ArrayList<WelcomeItem> {
        return arrayListOf(
                WelcomeItem(R.drawable.userimg_blockchain_80,
                        getString(R.string.welcome_blockchain_title),
                        getString(R.string.welcome_blockchain_description)),
                WelcomeItem(R.drawable.userimg_wallet_80,
                        getString(R.string.welcome_wallet_title),
                        getString(R.string.welcome_wallet_description)),
                WelcomeItem(R.drawable.userimg_dex_80,
                        getString(R.string.welcome_dex_title),
                        getString(R.string.welcome_dex_description))
                /*, WelcomeItem(R.drawable.userimg_token_80,
                        getString(R.string.welcome_token_title),
                        getString(R.string.welcome_token_description))*/
        )
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
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        this.menu = menu
        menuInflater.inflate(R.menu.menu_language, menu)
        updateMenuTitle()
        return super.onCreateOptionsMenu(menu)
    }

    private fun updateMenuTitle() {
        val bedMenuItem = menu?.findItem(R.id.action_change_language)
        val langCode = preferencesHelper.getLanguage()
        if (langCode == Language.BRAZILIAN.code) {
            bedMenuItem?.title = "br"
        } else {
            bedMenuItem?.title = preferencesHelper.getLanguage()
        }
    }

    override fun onBackPressed() {
        if (slidingRootNav.isMenuOpened) {
            slidingRootNav.closeMenu(true)
        } else {
            exit()
        }
    }

    companion object {
        var REQUEST_NEW_ACCOUNT = 55
        var REQUEST_SIGN_IN = 56
        var REQUEST_IMPORT_ACC = 57
    }
}
