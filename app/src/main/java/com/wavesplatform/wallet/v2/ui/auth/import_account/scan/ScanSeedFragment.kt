package com.wavesplatform.wallet.v2.ui.auth.import_account.scan

import android.os.Bundle
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.google.zxing.integration.android.IntentIntegrator
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.auth.qr_scanner.QrCodeScannerActivity
import com.wavesplatform.wallet.v2.ui.base.view.BaseFragment
import kotlinx.android.synthetic.main.fragment_scan_seed.*
import pers.victor.ext.click
import javax.inject.Inject


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
            IntentIntegrator(baseActivity).setRequestCode(REQUEST_SCAN_QR_CODE)
                    .setOrientationLocked(true)
                    .setCaptureActivity(QrCodeScannerActivity::class.java)
                    .initiateScan()
        }
    }
}
