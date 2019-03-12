package com.wavesplatform.wallet.v2.ui.home.quick_action.receive.address_view

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.jakewharton.rxbinding2.view.RxView
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.ui.home.MainActivity
import com.wavesplatform.wallet.v2.ui.home.quick_action.receive.invoice.InvoiceFragment
import com.wavesplatform.wallet.v2.ui.home.wallet.your_assets.YourAssetsActivity
import com.wavesplatform.wallet.v2.util.copyToClipboard
import com.wavesplatform.wallet.v2.util.launchActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_receive_address_view.*
import pers.victor.ext.click
import pers.victor.ext.visiable
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ReceiveAddressViewActivity : BaseActivity(), ReceiveAddressView {

    @Inject
    @InjectPresenter
    lateinit var presenter: ReceiveAddressViewPresenter

    @ProvidePresenter
    fun providePresenter(): ReceiveAddressViewPresenter = presenter

    override fun configLayoutRes(): Int = R.layout.activity_receive_address_view

    override fun onCreate(savedInstanceState: Bundle?) {
        translucentStatusBar = true
        overridePendingTransition(R.anim.slide_in_right, R.anim.null_animation)
        super.onCreate(savedInstanceState)
    }

    override fun onViewReady(savedInstanceState: Bundle?) {
        val assetBalance = intent?.getParcelableExtra<AssetBalance>(
                YourAssetsActivity.BUNDLE_ASSET_ITEM)
        toolbar_view.title = getString(R.string.receive_address_view_toolbar,
                assetBalance?.getName()
                        ?: "")

        image_asset_icon.setAsset(assetBalance)

        image_close.click {
            launchActivity<MainActivity>(clear = true)
        }
        button_close.click {
            launchActivity<MainActivity>(clear = true)
        }
        frame_share.click {
            val sharingIntent = Intent(Intent.ACTION_SEND)
            sharingIntent.type = "text/plain"
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, text_address.text)
            startActivity(Intent.createChooser(sharingIntent, resources.getString(R.string.app_name)))
        }

        eventSubscriptions.add(RxView.clicks(frame_copy)
                .throttleFirst(1500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    frame_copy.copyToClipboard(text_address.text.toString(), text_copy,
                            copyIcon = R.drawable.ic_copy_18_submit_400,
                            copyColor = R.color.submit400)
                })

        eventSubscriptions.add(RxView.clicks(image_copy)
                .throttleFirst(1500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    image_copy.copyToClipboard(text_invoice_link.text.toString(),
                            copyIcon = R.drawable.ic_copy_18_submit_400)
                })

        eventSubscriptions.add(RxView.clicks(image_share)
                .throttleFirst(1500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    val sharingIntent = Intent(Intent.ACTION_SEND)
                    sharingIntent.type = "text/plain"
                    sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, text_invoice_link.text)
                    startActivity(Intent.createChooser(sharingIntent, resources.getString(R.string.app_name)))
                })

        if (intent.getBooleanExtra(InvoiceFragment.INVOICE_SCREEN, false)) {
            container_invoice_link.visiable()

            toolbar_view.title = getString(R.string.receive_address_waves_address,
                    assetBalance?.getName() ?: "")
            image_asset_icon.setAsset(assetBalance)
        }

        val text: String
        val address = if (intent.hasExtra(YourAssetsActivity.BUNDLE_ADDRESS)) {
            intent.getStringExtra(YourAssetsActivity.BUNDLE_ADDRESS)
        } else {
            App.getAccessManager().getWallet()?.address ?: ""
        }
        text_address.text = address

        if (intent.hasExtra(KEY_INTENT_QR_DATA)) {
            text = intent.getStringExtra(KEY_INTENT_QR_DATA)
            text_invoice_link.text = text
        } else {
            text = address
        }
        presenter.generateQRCode(text, resources.getDimension(R.dimen._200sdp).toInt())
    }

    override fun showQRCode(qrCode: Bitmap?) {
        image_view_recipient_action.setImageBitmap(qrCode)
    }

    companion object {
        const val KEY_INTENT_QR_DATA = "intent_qr_data"
    }

    override fun onBackPressed() {
        finish()
        overridePendingTransition(R.anim.null_animation, R.anim.slide_out_right)
    }
}
