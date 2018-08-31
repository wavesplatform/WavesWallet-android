package com.wavesplatform.wallet.v2.ui.auth.import_account.scan

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import javax.inject.Inject

import com.arellomobile.mvp.presenter.InjectPresenter

import com.wavesplatform.wallet.v2.ui.base.view.BaseFragment;

import com.arellomobile.mvp.presenter.ProvidePresenter;
import com.google.zxing.integration.android.IntentIntegrator
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.auth.import_account.protect_account.ProtectAccountActivity
import com.wavesplatform.wallet.v2.ui.auth.qr_scanner.QrCodeScannerActivity
import com.wavesplatform.wallet.v2.util.launchActivity
import kotlinx.android.synthetic.main.fragment_scan_seed.*
import pers.victor.ext.click
import pers.victor.ext.toast


class ScanSeedFragment : BaseFragment(), ScanSeedView {

    @Inject
    @InjectPresenter
    lateinit var presenter: ScanSeedPresenter

    @ProvidePresenter
    fun providePresenter(): ScanSeedPresenter = presenter


    companion object {
        var REQUEST_SCAN_QR_CODE = 55
    }

    override fun configLayoutRes() = R.layout.fragment_scan_seed


    override fun onViewReady(savedInstanceState: Bundle?) {
        button_scan.click {
            IntentIntegrator(baseActivity).setRequestCode(REQUEST_SCAN_QR_CODE).setOrientationLocked(true).setCaptureActivity(QrCodeScannerActivity::class.java).initiateScan();
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_SCAN_QR_CODE -> {
                val result = IntentIntegrator.parseActivityResult(resultCode, data)

                if (result.contents == null) {
                    Log.d("ScanSeedFragment", "Cancelled scan")
                } else {
                    toast("Scanned: " + result.contents)
                    // TODO: Change to real scanned address
                    launchActivity<ProtectAccountActivity> {
                        putExtra(ProtectAccountActivity.BUNDLE_ACCOUNT_ADDRESS, "MkSuckMydickmMak1593x1GrfYmFdsf83skS11")
                    }
                }
            }
        }

    }

}
