package com.wavesplatform.wallet.v2.ui.home.wallet.address

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.helpers.PublicKeyAccountHelper
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.util.copyToClipboard
import com.wavesplatform.wallet.v2.util.notNull
import kotlinx.android.synthetic.main.activity_my_address_qr.*
import pers.victor.ext.click
import pers.victor.ext.findColor
import pyxis.uzuki.live.richutilskt.utils.runDelayed
import javax.inject.Inject


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
            text_copy.text = getString(R.string.common_copied)
            text_copy.setTextColor(findColor(R.color.success400))
            text_copy.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check_18_success_400, 0, 0, 0);
            runDelayed(1500, {
                this.text_copy.notNull {
                    text_copy.text = getString(R.string.my_address_qr_copy)
                    text_copy.setTextColor(findColor(R.color.black))
                    text_copy.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_copy_18_submit_400, 0, 0, 0);
                }
                text_address.copyToClipboard()
            })
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

    private fun copyToClipboard(view: TextView, text: Int) {

    }
}
