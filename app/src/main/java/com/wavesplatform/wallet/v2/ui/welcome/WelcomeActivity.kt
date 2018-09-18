package com.wavesplatform.wallet.v2.ui.welcome

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.view.ViewPager
import android.support.v4.view.ViewPager.SCROLL_STATE_IDLE
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.R
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
import pers.victor.ext.*
import java.util.*
import javax.inject.Inject
import kotlin.concurrent.fixedRateTimer


class WelcomeActivity : BaseDrawerActivity(), WelcomeView {

    @Inject
    @InjectPresenter
    lateinit var presenter: WelcomePresenter

    @Inject
    lateinit var adapter: WelcomeItemsPagerAdapter

    private var menu: Menu? = null

    @ProvidePresenter
    fun providePresenter(): WelcomePresenter = presenter

    override fun configLayoutRes() = R.layout.activity_welcome

    companion object {
        var REQUEST_NEW_ACCOUNT = 55
        var REQUEST_SIGN_IN = 56
        var REQUEST_IMPORT_ACC = 57
    }


    override fun onViewReady(savedInstanceState: Bundle?) {
        setupToolbar(toolbar_view)
//        val icon = findDrawable(R.drawable.avd_anim)
//        icon.notNull {
//            changeDrawerMenuIcon(it)
//            if (it is Animatable) {
//                (it as Animatable).start()
//            }
//        }

        button_create_account.click {
            animateWhiteBlock(it) {
                launchActivity<NewAccountActivity>(REQUEST_NEW_ACCOUNT)
                overridePendingTransition(0, 0)
            }
        }

        relative_sign_in.click {
            animateWhiteBlock(it) {
                launchActivity<ChooseAccountActivity>(REQUEST_SIGN_IN)
                overridePendingTransition(0, 0)
            }
        }

        relative_import_acc.click {
            animateWhiteBlock(it) {
                launchActivity<ImportAccountActivity>(REQUEST_IMPORT_ACC)
                overridePendingTransition(0, 0)
            }
        }

        adapter.items = populateList()
        view_pager.setPageTransformer(false, object : ViewPager.PageTransformer {
            override fun transformPage(page: View, position: Float) {
                val root = page.findViewById<LinearLayout>(R.id.linear_root)
                if (position <= -1.0F || position >= 1.0F) {
                    root.alpha = 0.0F;
                } else if (position == 0.0F) {
                    root.alpha = 1.0F;
                } else {
                    root.alpha = 1.0F - Math.abs(position);
                }
            }

        })
        view_pager.adapter = adapter
        view_pager.offscreenPageLimit = 5
        view_pager_indicator.setupWithViewPager(view_pager);
        view_pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
                presenter.state = state
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                presenter.nextItemPosition = position
                presenter.nextItemPosition++
                if (presenter.nextItemPosition == 4) presenter.nextItemPosition = 0
            }
        })

        fixedRateTimer(initialDelay = 5000, period = 5000, action = {
            if (presenter.state == SCROLL_STATE_IDLE) {
                runOnUiThread { view_pager.setCurrentItem(presenter.nextItemPosition, true) }
            }
        })

        enterAnimation()
    }

    private fun animateWhiteBlock(it: View, endAction: () -> Unit) {
        white_block.setHeight(it.height)
        white_block.setWidth(it.width)
        val originalPos = IntArray(2)
        it.getLocationOnScreen(originalPos)
        white_block.y = originalPos[1].toFloat()
        white_block.visiable()

        white_block.post({
            white_block.animate()
                    .scaleX(2f)
                    .scaleY(((screenHeight + white_block.y) / white_block.height).toFloat() + 0.5f)
                    .setDuration(500)
                    .setStartDelay(0)
                    .withEndAction {
                        endAction()
                    }
                    .start()
        })
    }

    private fun enterAnimation() {
//        card_view_welcome.animate()
//                .scaleY(1f)
//                .scaleX(1f)
//                .setStartDelay(500)
//                .setDuration(250)
//                .withEndAction {
//                    linear_sign_in.animate()
//                            .alpha(1f)
//                            .setDuration(350)
//                            .withEndAction {
//                                relative_top_block.animate()
//                                        .translationY(0f)
//                                        .alpha(1f)
//                                        .setDuration(500)
//                                        .start()
//                            }
//                            .start()
//                }
//                .start()
    }

    private fun populateList(): ArrayList<WelcomeItem> {
        return arrayListOf(WelcomeItem(R.drawable.ic_userimg_blockchain_80, R.string.welcome_blockchain_title, R.string.welcome_blockchain_description),
                WelcomeItem(R.drawable.ic_userimg_wallet_80, R.string.welcome_wallet_title, R.string.welcome_wallet_description),
                WelcomeItem(R.drawable.ic_userimg_dex_80, R.string.welcome_dex_title, R.string.welcome_dex_description),
                WelcomeItem(R.drawable.ic_userimg_token_80, R.string.welcome_token_title, R.string.welcome_token_description))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_NEW_ACCOUNT || requestCode == REQUEST_IMPORT_ACC || requestCode == REQUEST_SIGN_IN) {
            if (resultCode == Activity.RESULT_CANCELED) {
                white_block.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setStartDelay(150)
                        .setDuration(500)
                        .withEndAction {
                            white_block.gone()
                        }
                        .start()
            }
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_change_language -> {
                val dialog = ChangeLanguageBottomSheetFragment()
                dialog.show(supportFragmentManager, dialog::class.java.simpleName)
//                launchActivity<ChangeWelcomeLanguageActivity>()
//                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
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
        bedMenuItem?.title = getString(Language.getLanguageByCode(preferencesHelper.getLanguage()).code)
    }

    override fun onResume() {
        super.onResume()
        menu.notNull { updateMenuTitle() }
    }

}
