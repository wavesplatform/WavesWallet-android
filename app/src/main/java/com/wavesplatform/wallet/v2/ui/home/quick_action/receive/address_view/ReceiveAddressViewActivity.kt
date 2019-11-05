/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.quick_action.receive.address_view

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import com.jakewharton.rxbinding3.view.clicks
import com.wavesplatform.sdk.model.response.node.AssetBalanceResponse
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.ui.home.quick_action.receive.invoice.InvoiceFragment
import com.wavesplatform.wallet.v2.ui.home.wallet.your_assets.YourAssetsActivity
import com.wavesplatform.wallet.v2.util.copyToClipboard
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_receive_address_view.*
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
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
        val assetBalance = intent?.getParcelableExtra<AssetBalanceResponse>(YourAssetsActivity.BUNDLE_ASSET_ITEM)
        toolbar_view.title = getString(R.string.receive_address_view_toolbar, assetBalance?.getName())

        image_asset_icon.setAsset(assetBalance)

        image_close.click {
            onBackPressed()
        }
        button_close.click {
            onBackPressed()
        }
        frame_share.click {
            val sharingIntent = Intent(Intent.ACTION_SEND)
            sharingIntent.type = "text/plain"
            sharingIntent.putExtra(Intent.EXTRA_TEXT, text_address.text)
            startActivity(Intent.createChooser(sharingIntent, resources.getString(R.string.app_name)))
        }

        eventSubscriptions.add(frame_copy.clicks()
                .throttleFirst(1500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    frame_copy.copyToClipboard(text_address.text.toString(), text_copy,
                            copyIcon = R.drawable.ic_copy_18_submit_400,
                            copyColor = R.color.submit400)
                })

        eventSubscriptions.add(image_copy.clicks()
                .throttleFirst(1500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    image_copy.copyToClipboard(text_invoice_link.text.toString(),
                            copyIcon = R.drawable.ic_copy_18_submit_400)
                })

        eventSubscriptions.add(image_share.clicks()
                .throttleFirst(1500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    val sharingIntent = Intent(Intent.ACTION_SEND)
                    sharingIntent.type = "text/plain"
                    sharingIntent.putExtra(Intent.EXTRA_TEXT, text_invoice_link.text)
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
        setResult(Activity.RESULT_OK)
        finish()
        overridePendingTransition(R.anim.null_animation, R.anim.slide_out_right)
    }
}
