package com.wavesplatform.wallet.v2.ui.address

import android.graphics.Bitmap
import com.wavesplatform.wallet.v2.ui.base.view.BaseMvpView

interface MyAddressQrView : BaseMvpView {
    fun showQRCode(qrCode: Bitmap?)
}
