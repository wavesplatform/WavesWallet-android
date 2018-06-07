package com.wavesplatform.wallet.v2.ui.welcome

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.view.ViewPager
import android.support.v4.view.ViewPager.SCROLL_STATE_DRAGGING
import android.support.v4.view.ViewPager.SCROLL_STATE_IDLE
import android.view.Menu
import android.view.MenuItem
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.R.id.*
import com.wavesplatform.wallet.v2.data.model.local.Language
import com.wavesplatform.wallet.v2.data.model.local.WelcomeItem
import com.wavesplatform.wallet.v2.ui.base.view.BaseDrawerActivity
import com.wavesplatform.wallet.v2.ui.home.MainActivity
import com.wavesplatform.wallet.v2.ui.language.change.ChangeLanguageActivity
import com.wavesplatform.wallet.v2.ui.new_account.NewAccountActivity
import com.wavesplatform.wallet.v2.util.launchActivity
import com.wavesplatform.wallet.v2.util.notAvailable
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
    }


    override fun onViewReady(savedInstanceState: Bundle?) {
        setupToolbar(toolbar_view)

        button_create_account.click {
            white_block.visiable()
            white_block.post({
                white_block.animate()
                        .scaleX(1.5f)
                        .scaleY(4.5f)
                        .setDuration(500)
                        .setStartDelay(0)
                        .withEndAction {
                            launchActivity<NewAccountActivity>(REQUEST_NEW_ACCOUNT)
                            overridePendingTransition(0,0)
                        }
                        .start()
            })
        }

        adapter.items = populateList()
        view_pager.adapter = adapter
        view_pager.offscreenPageLimit = 5
        view_pager.clipToPadding = false
        view_pager.setPadding(screenWidth / 2 - dp2px(50), 0, screenWidth / 2 - dp2px(50), 0)
        view_pager.setPageTransformer(false, AlphaScalePageTransformer())
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

                val item = adapter.items[position]
                text_title.setText(item.title)
                text_descr.setText(item.description)
            }
        })

        fixedRateTimer(initialDelay = 5000, period = 5000, action = {
            if (presenter.state == SCROLL_STATE_IDLE){
                runOnUiThread { view_pager.setCurrentItem(presenter.nextItemPosition, true) }
            }
        })

        enterAnimation()

        relative_sign_in.click {
            launchActivity<MainActivity> {  }
        }

        relative_import_acc.click {
            notAvailable()

        }
    }

    private fun enterAnimation() {
        card_view_welcome.animate()
                .scaleY(1f)
                .scaleX(1f)
                .setStartDelay(500)
                .setDuration(250)
                .withEndAction {
                    linear_sign_in.animate()
                            .alpha(1f)
                            .setDuration(350)
                            .withEndAction {
                                relative_top_block.animate()
                                        .translationY(0f)
                                        .alpha(1f)
                                        .setDuration(500)
                                        .start()
                            }
                            .start()
                }
                .start()
    }

    private fun populateList(): ArrayList<WelcomeItem> {
        return arrayListOf(WelcomeItem(R.drawable.ic_userimg_blockchain_80_white, R.string.welcome_blockchain_title, R.string.welcome_blockchain_description),
                WelcomeItem(R.drawable.ic_userimg_wallet_80_white, R.string.welcome_wallet_title, R.string.welcome_wallet_description),
                WelcomeItem(R.drawable.ic_userimg_dex_80_white, R.string.welcome_dex_title, R.string.welcome_dex_description),
                WelcomeItem(R.drawable.ic_userimg_token_80_white, R.string.welcome_token_title, R.string.welcome_token_description))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_NEW_ACCOUNT) {
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
                launchActivity<ChangeLanguageActivity>()
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
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
