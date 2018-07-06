package com.wavesplatform.wallet.v2.ui.address

import android.content.ClipData
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.AppCompatImageView
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.helpers.PublicKeyAccountHelper
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.util.notNull
import kotlinx.android.synthetic.main.activity_my_address_qr.*
import pers.victor.ext.click
import pers.victor.ext.clipboardManager
import pers.victor.ext.toast
import pyxis.uzuki.live.richutilskt.utils.runDelayed
import javax.inject.Inject
import com.google.zxing.BarcodeFormat
import android.graphics.Bitmap
import android.widget.ImageView
import com.wavesplatform.wallet.v1.ui.zxing.Contents
import com.wavesplatform.wallet.v1.ui.zxing.encode.QRCodeEncoder
import com.wavesplatform.wallet.v2.util.copyToClipboard
import pers.victor.ext.dp2px
import pyxis.uzuki.live.richutilskt.utils.runAsync


class MyAddressQRActivity : BaseActivity(), MyAddressQrView {
    @Inject
    @InjectPresenter
    lateinit var presenter: MyAddressQrPresenter

    @ProvidePresenter
    fun providePresenter(): MyAddressQrPresenter = presenter

    override fun configLayoutRes() = R.layout.activity_my_address_qr

    @Inject
    lateinit var publicKeyAccountHelper: PublicKeyAccountHelper

    override fun onViewReady(savedInstanceState: Bundle?) {
        setupToolbar(toolbar_view, View.OnClickListener { onBackPressed() }, true, getString(R.string.my_address_qr_toolbar_title), R.drawable.ic_toolbar_back_black)

        text_address.text = publicKeyAccountHelper.publicKeyAccount?.address
        frame_share.click {
            shareAddress()
        }

        frame_copy.click {
            text_address.copyToClipboard()
        }

        presenter.generateQRCode(text_address.text.toString(), dp2px(240))
    }

    override fun showQRCode(qrCode: Bitmap?) {
        image_view_qr_code.setImageBitmap(qrCode)
    }

    private fun shareAddress() {
        val sharingIntent = Intent(android.content.Intent.ACTION_SEND)
        sharingIntent.type = "text/plain"
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, text_address.text)
        startActivity(Intent.createChooser(sharingIntent, resources.getString(R.string.app_name)))
    }

}
