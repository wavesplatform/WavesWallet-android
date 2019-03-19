package com.wavesplatform.wallet.v2.ui.web

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.webkit.WebViewClient
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import kotlinx.android.synthetic.main.activity_web.*
import javax.inject.Inject

class WebActivity : BaseActivity(), WebView {

    @Inject
    @InjectPresenter
    lateinit var presenter: WebPresenter

    @ProvidePresenter
    fun providePresenter(): WebPresenter = presenter

    override fun configLayoutRes() = R.layout.activity_web

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewReady(savedInstanceState: Bundle?) {
        setStatusBarColor(R.color.basic50)
        setNavigationBarColor(R.color.basic50)

        if (!intent.hasExtra(KEY_INTENT_LINK)) {
            finish()
        }

        val link = intent.extras.getString(KEY_INTENT_LINK, "")
        val title = if (intent.hasExtra(KEY_INTENT_TITLE)) {
            intent.extras.getString(KEY_INTENT_TITLE, "")
        } else {
            Uri.parse(link).host
        }

        setupToolbar(toolbar_view, true, title, R.drawable.ic_toolbar_back_black)

        webView.settings.javaScriptEnabled = true
        webView.webViewClient = WebViewClient()
        webView.loadUrl(link)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    companion object {
        const val KEY_INTENT_LINK = "intent_link"
        const val KEY_INTENT_TITLE = "intent_title"
    }
}