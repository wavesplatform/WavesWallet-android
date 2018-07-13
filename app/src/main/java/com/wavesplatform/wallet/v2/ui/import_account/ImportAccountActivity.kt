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
import android.util.Log
import com.google.zxing.integration.android.IntentIntegrator
import com.wavesplatform.wallet.v2.ui.import_account.enter_seed.EnterSeedManuallyActivity
import com.wavesplatform.wallet.v2.ui.import_account.protect_account.ProtectAccountActivity
import com.wavesplatform.wallet.v2.ui.qr_scanner.QrCodeScannerActivity
import com.wavesplatform.wallet.v2.util.launchActivity
import pers.victor.ext.click


class ImportAccountActivity : BaseActivity(), ImportAccountView {

    @Inject
    @InjectPresenter
    lateinit var presenter: ImportAccountPresenter

    @ProvidePresenter
    fun providePresenter(): ImportAccountPresenter = presenter

    companion object {
        var REQUEST_SCAN_QR_CODE = 55
    }

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
            IntentIntegrator(this).setRequestCode(REQUEST_SCAN_QR_CODE).setOrientationLocked(true).setCaptureActivity(QrCodeScannerActivity::class.java).initiateScan();
        }

        button_enter_manually.click {
            launchActivity<EnterSeedManuallyActivity> {  }
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (requestCode != REQUEST_SCAN_QR_CODE && requestCode != IntentIntegrator.REQUEST_CODE) {
            // This is important, otherwise the result will not be passed to the fragment
            super.onActivityResult(requestCode, resultCode, data)
            return
        }
        when (requestCode) {
            REQUEST_SCAN_QR_CODE -> {
                val result = IntentIntegrator.parseActivityResult(resultCode, data)

                if (result.contents == null) {
                    Log.d("MainActivity", "Cancelled scan")
                    Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show()
                } else {
                    Log.d("MainActivity", "Scanned")
                    Toast.makeText(this, "Scanned: " + result.contents, Toast.LENGTH_LONG).show()

                    // TODO: Change to real scanned address
                    launchActivity<ProtectAccountActivity> {
                        putExtra(ProtectAccountActivity.BUNDLE_ACCOUNT_ADDRESS, "MkSuckMydickmMak1593x1GrfYmFdsf83skS11")
                    }
                }
            }
        }


    }
}
