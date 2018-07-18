package com.wavesplatform.wallet.v2.ui.address

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.helpers.PublicKeyAccountHelper
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import kotlinx.android.synthetic.main.activity_my_address_qr.*
import pers.victor.ext.click
import javax.inject.Inject
import android.graphics.Bitmap
import com.wavesplatform.wallet.v2.util.copyToClipboard


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

        presenter.generateQRCode(text_address.text.toString(), resources.getDimension(R.dimen._200sdp).toInt())
    }

    override fun showQRCode(qrCode: Bitmap?) {
        image_view_recipient_action.setImageBitmap(qrCode)
    }

    private fun shareAddress() {
        val sharingIntent = Intent(android.content.Intent.ACTION_SEND)
        sharingIntent.type = "text/plain"
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, text_address.text)
        startActivity(Intent.createChooser(sharingIntent, resources.getString(R.string.app_name)))
    }

}
