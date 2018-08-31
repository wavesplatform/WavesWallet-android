package com.wavesplatform.wallet.v2.ui.auth.import_account

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.google.zxing.integration.android.IntentIntegrator
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.auth.import_account.protect_account.ProtectAccountActivity
import com.wavesplatform.wallet.v2.ui.auth.import_account.scan.ScanSeedFragment.Companion.REQUEST_SCAN_QR_CODE
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.util.launchActivity
import kotlinx.android.synthetic.main.activity_import_account.*
import javax.inject.Inject


class ImportAccountActivity : BaseActivity(), ImportAccountView {

    @Inject
    @InjectPresenter
    lateinit var presenter: ImportAccountPresenter

    @ProvidePresenter
    fun providePresenter(): ImportAccountPresenter = presenter

    override fun configLayoutRes() = R.layout.activity_import_account


    override fun onViewReady(savedInstanceState: Bundle?) {
        setupToolbar(toolbar_view, View.OnClickListener { onBackPressed() }, true, title = getString(R.string.import_account_toolbar_title), icon = R.drawable.ic_toolbar_back_black)

        viewpager_import.adapter = ImportAccountFragmentPageAdapter(supportFragmentManager, arrayOf(getString(R.string.import_account_tab_scan),
                getString(R.string.import_account_tab_manually)))

        stl_history.setViewPager(viewpager_import)
        stl_history.currentTab = 0

//        val siteClick = object : ClickableSpan() {
//            override fun onClick(p0: View?) {
//                SimpleChromeCustomTabs.getInstance()
//                        .withFallback({
//                            openUrlWithIntent(getString(R.string.import_account_login_at_site_key))
//                        }).withIntentCustomizer({
//                            it.withToolbarColor(ContextCompat.getColor(this@ImportAccountActivity, R.color.submit400))
//                        })
//                        .navigateTo(Uri.parse(getString(R.string.import_account_login_at_site_key)), this@ImportAccountActivity)
//            }
//
//            override fun updateDrawState(ds: TextPaint) {
//                super.updateDrawState(ds)
//                ds.color = findColor(R.color.black)
//            }
//        }
//
//        text_first_title.makeLinks(arrayOf(getString(R.string.import_account_login_at_site_key)), arrayOf(siteClick))
//
//        button_scan.click {
//            IntentIntegrator(this).setRequestCode(REQUEST_SCAN_QR_CODE).setOrientationLocked(true).setCaptureActivity(QrCodeScannerActivity::class.java).initiateScan();
//        }
//
//        button_enter_manually.click {
//            launchActivity<EnterSeedManuallyActivity> { }
//        }
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_CANCELED)
        finish()
        overridePendingTransition(0, android.R.anim.fade_out)
    }
}
