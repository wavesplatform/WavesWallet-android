package com.wavesplatform.wallet.v2.ui.auth.qr_scanner

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.journeyapps.barcodescanner.CaptureManager
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.journeyapps.barcodescanner.ViewfinderView
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import kotlinx.android.synthetic.main.activity_qr_code_scanner.*
import pers.victor.ext.click
import javax.inject.Inject

class QrCodeScannerActivity : BaseActivity(), QrCodeScannerView,
        DecoratedBarcodeView.TorchListener {

    private lateinit var capture: CaptureManager
    @Inject
    @InjectPresenter
    lateinit var presenter: QrCodeScannerPresenter

    @ProvidePresenter
    fun providePresenter(): QrCodeScannerPresenter = presenter

    override fun configLayoutRes() = R.layout.activity_qr_code_scanner

    override fun onCreate(savedInstanceState: Bundle?) {
        overridePendingTransition(R.anim.slide_in_up, R.anim.null_animation)
        super.onCreate(savedInstanceState)
        setStatusBarColor(R.color.black)
        setNavigationBarColor(R.color.black)
    }

    override fun onViewReady(savedInstanceState: Bundle?) {
        zxing_barcode_scanner.setTorchListener(this)

        disableLaser(zxing_barcode_scanner)

        // if the device does not have flashlight in its camera,
        // then remove the switch flashlight button...
        if (!hasFlash()) {
            image_flash.visibility = View.GONE
        }

        image_flash.click {
            switchFlashlight()
        }
        button_cancel.click {
            onBackPressed()
        }

        capture = CaptureManager(this, zxing_barcode_scanner)
        capture.initializeFromIntent(intent, savedInstanceState)
        capture.decode()
    }

    private fun disableLaser(decoratedBarcodeView: DecoratedBarcodeView) {
        val scannerAlphaField = ViewfinderView::class.java.getDeclaredField("SCANNER_ALPHA")
        scannerAlphaField.isAccessible = true
        scannerAlphaField.set(decoratedBarcodeView.viewFinder, intArrayOf(0))
    }

    override fun onResume() {
        super.onResume()
        capture.onResume()
    }

    override fun onPause() {
        super.onPause()
        capture.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        capture.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        capture.onSaveInstanceState(outState)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return zxing_barcode_scanner.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event)
    }

    /**
     * Check if the device's camera has a Flashlight.
     * @return true if there is Flashlight, otherwise false.
     */
    private fun hasFlash(): Boolean {
        return applicationContext.packageManager
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)
    }

    private fun switchFlashlight() {
        if (image_flash.tag == getString(R.string.qr_scanner_flash_off)) {
            zxing_barcode_scanner.setTorchOn()
        } else {
            zxing_barcode_scanner.setTorchOff()
        }
    }

    override fun onTorchOn() {
        image_flash.tag = getString(R.string.qr_scanner_flash_on)
        image_flash.setImageResource(R.drawable.ic_topbar_flash_off)
    }

    override fun onTorchOff() {
        image_flash.tag = getString(R.string.qr_scanner_flash_off)
        image_flash.setImageResource(R.drawable.ic_topbar_flash_on)
    }

    override fun onBackPressed() {
        finish()
        overridePendingTransition(R.anim.null_animation, R.anim.slide_out_down)
    }
}
