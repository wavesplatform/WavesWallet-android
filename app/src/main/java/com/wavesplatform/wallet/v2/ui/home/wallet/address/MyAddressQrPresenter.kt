package com.wavesplatform.wallet.v2.ui.home.wallet.address

import android.graphics.Bitmap
import android.graphics.Color
import android.support.v7.widget.AppCompatImageView
import com.arellomobile.mvp.InjectViewState
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.wavesplatform.wallet.v1.ui.zxing.Contents
import com.wavesplatform.wallet.v1.ui.zxing.encode.QRCodeEncoder
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.ui.custom.Identicon
import com.wavesplatform.wallet.v2.util.RxUtil
import io.reactivex.Observable
import java.util.*
import javax.inject.Inject

@InjectViewState
class MyAddressQrPresenter @Inject constructor() : BasePresenter<MyAddressQrView>() {

    private fun generateQrCodeObservable(uri: String, dimensions: Int): Observable<Bitmap> {
        return Observable.defer {
            var bitmap: Bitmap? = null
            val qrCodeEncoder = QRCodeEncoder(uri, null, Contents.Type.TEXT, BarcodeFormat.QR_CODE.toString(), dimensions)
            try {
                bitmap = qrCodeEncoder.encodeAsBitmap()
            } catch (e: WriterException) {
                return@defer Observable.error<Bitmap>(e)
            }

            if (bitmap == null) {
                return@defer Observable.error<Bitmap>(Throwable("Bitmap was null"))
            } else {
                return@defer Observable.just<Bitmap>(bitmap)
            }
        }
    }


    fun generateAvatars(address: String, image: AppCompatImageView) {
        Observable.fromCallable({
            val rnd = Random()
            val color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256))
            return@fromCallable Identicon.create(address,
                    Identicon.Options.Builder()
                            .setBlankColor(color)
                            .create())
        }).compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe({
                    viewState.afterSuccessGenerateAvatar(it, image)
                })
    }

    fun generateQRCode(text: String, size: Int) {
        addSubscription(generateQrCodeObservable(text, size)
                .compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe({
                    viewState.showQRCode(it)
                }))
    }
}
