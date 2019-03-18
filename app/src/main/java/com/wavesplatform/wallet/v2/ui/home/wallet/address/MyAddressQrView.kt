package com.wavesplatform.wallet.v2.ui.home.wallet.address

import android.graphics.Bitmap
import android.support.v7.widget.AppCompatImageView
import com.wavesplatform.sdk.net.model.response.Alias
import com.wavesplatform.wallet.v2.ui.base.view.BaseMvpView

interface MyAddressQrView : BaseMvpView {
    fun showQRCode(qrCode: Bitmap?)
    fun afterSuccessGenerateAvatar(bitmap: Bitmap, imageView: AppCompatImageView)
    fun afterSuccessLoadAliases(ownAliases: List<Alias>)
}
