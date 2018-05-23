package com.wavesplatform.wallet.v2.ui.base.view

import android.graphics.Paint
import android.os.Bundle
import android.view.View
import javax.inject.Inject

import com.arellomobile.mvp.presenter.InjectPresenter
import com.wavesplatform.wallet.v2.ui.welcome.WelcomeView
import com.wavesplatform.wallet.v2.ui.welcome.WelcomePresenter

import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity;

import com.arellomobile.mvp.presenter.ProvidePresenter;
import com.wavesplatform.wallet.R
import com.yarolegovich.slidingrootnav.SlidingRootNav
import com.yarolegovich.slidingrootnav.SlidingRootNavBuilder
import kotlinx.android.synthetic.main.activity_welcome.*
import pers.victor.ext.dp2px
import pers.victor.ext.px2dp
import pers.victor.ext.screenWidth
import android.graphics.Paint.UNDERLINE_TEXT_FLAG
import com.wavesplatform.wallet.R.id.toolbar_view
import com.wavesplatform.wallet.v2.util.setSystemBarTheme
import com.yarolegovich.slidingrootnav.callback.DragStateListener
import kotlinx.android.synthetic.main.menu_left_drawer.*
import kotlinx.android.synthetic.main.menu_left_drawer.view.*
import android.content.Intent
import android.net.Uri
import com.wavesplatform.wallet.R.string.click
import com.wavesplatform.wallet.v2.data.Constants
import pers.victor.ext.click
import android.content.pm.PackageManager
import android.content.pm.ApplicationInfo




abstract class BaseDrawerActivity : BaseActivity(), WelcomeView {

    protected lateinit var slidingRootNav: SlidingRootNav

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        slidingRootNav = SlidingRootNavBuilder(this)
                .withDragDistance(px2dp(screenWidth) - dp2px(18))
                .withRootViewScale(0.87f)
                .withRootViewElevation(10)
                .withToolbarMenuToggle(toolbar)
                .addDragListener { progress ->
                    if (progress > 0.5) setSystemBarTheme(false)
                    else setSystemBarTheme(true)
                }
                .withMenuOpened(false)
                .withContentClickableWhenMenuOpened(false)
                .withSavedState(savedInstanceState)
                .withMenuLayout(R.layout.menu_left_drawer)
                .inject()


        slidingRootNav.layout.text_site.paintFlags = text_site.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        slidingRootNav.layout.text_site.click { openUrl(Constants.URL_WAVES_COMMUNITY) }
        slidingRootNav.layout.text_whitepaper.click { openUrl(Constants.URL_WHITEPAPER) }
//        slidingRootNav.layout.text_roadmap.click { openUrl(Constants.u) }
        slidingRootNav.layout.text_terms.click { openUrl(Constants.URL_TERMS) }
        slidingRootNav.layout.image_discord.click { openUrl(Constants.URL_DISCORD) }
        slidingRootNav.layout.image_facebook.click { openFacebook(Constants.URL_FACEBOOK) }
        slidingRootNav.layout.image_github.click { openUrl(Constants.URL_GITHUB) }
        slidingRootNav.layout.image_telegram.click { openUrl(Constants.URL_TELEGRAM) }
        slidingRootNav.layout.image_twitter.click { openTwitter(Constants.ACC_TWITTER) }
    }

    fun openUrl(url : String){
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(browserIntent)
    }

    fun openFacebook(url: String) {
        var uri = Uri.parse(url)
        try {
            val applicationInfo = packageManager.getApplicationInfo("com.facebook.katana", 0)
            if (applicationInfo.enabled) {
                // http://stackoverflow.com/a/24547437/1048340
                uri = Uri.parse("fb://facewebmodal/f?href=$url")
            }
        } catch (ignored: PackageManager.NameNotFoundException) {
        }

        startActivity(Intent(Intent.ACTION_VIEW, uri))
    }

    fun openTwitter(url: String) {
        var intent: Intent? = null
        try {
            // get the Twitter app if possible
            this.packageManager.getPackageInfo("com.twitter.android", 0)
            intent = Intent(Intent.ACTION_VIEW, Uri.parse("twitter://user?user_id=$url"))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        } catch (e: Exception) {
            // no Twitter app, revert to browser
            intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/$url"))
        }

        this.startActivity(intent)
    }
}
