package com.wavesplatform.wallet.v2.ui.import_account

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.text.style.ClickableSpan
import android.view.View
import android.widget.Toast
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.novoda.simplechromecustomtabs.SimpleChromeCustomTabs
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.util.makeLinks
import kotlinx.android.synthetic.main.activity_import_account.*
import javax.inject.Inject
import android.text.TextPaint
import pers.victor.ext.click


class ImportAccountActivity : BaseActivity(), ImportAccountView {

    @Inject
    @InjectPresenter
    lateinit var presenter: ImportAccountPresenter

    @ProvidePresenter
    fun providePresenter(): ImportAccountPresenter = presenter

    override fun configLayoutRes() = R.layout.activity_import_account


    override fun onViewReady(savedInstanceState: Bundle?) {
        setupToolbar(toolbar_view, View.OnClickListener { onBackPressed() }, true, title = getString(R.string.import_account_toolbar_title), icon = R.drawable.ic_toolbar_back_black)

        val siteClick = object : ClickableSpan() {
            override fun onClick(p0: View?) {
                SimpleChromeCustomTabs.getInstance()
                        .withFallback({
                            openUrlWithIntent(getString(R.string.import_account_login_at_site_key))
                        }).withIntentCustomizer({
                            it.withToolbarColor(ContextCompat.getColor(this@ImportAccountActivity, R.color.submit400))
                        })
                        .navigateTo(Uri.parse(getString(R.string.import_account_login_at_site_key)), this@ImportAccountActivity)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = ContextCompat.getColor(this@ImportAccountActivity, R.color.black)
            }
        }

        text_first_title.makeLinks(arrayOf(getString(R.string.import_account_login_at_site_key)), arrayOf(siteClick))

        button_scan.click {

        }

        button_enter_manually.click {

        }
    }


    override fun onResume() {
        super.onResume()
        SimpleChromeCustomTabs.getInstance().connectTo(this)
    }

    override fun onPause() {
        SimpleChromeCustomTabs.getInstance().disconnectFrom(this)
        super.onPause()
    }

    fun openUrlWithIntent(url: String) {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(browserIntent)
    }
}
