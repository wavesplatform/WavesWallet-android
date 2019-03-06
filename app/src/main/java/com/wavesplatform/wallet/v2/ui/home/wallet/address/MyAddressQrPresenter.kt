package com.wavesplatform.wallet.v2.ui.home.wallet.address

import android.graphics.Bitmap
import android.support.v7.widget.AppCompatImageView
import com.arellomobile.mvp.InjectViewState
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.vicpin.krealmextensions.queryAllAsSingle
import com.wavesplatform.wallet.v1.ui.zxing.Contents
import com.wavesplatform.wallet.v1.ui.zxing.encode.QRCodeEncoder
import com.wavesplatform.wallet.v2.data.model.remote.response.Alias
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.ui.custom.Identicon
import com.wavesplatform.wallet.v2.util.RxUtil
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import pyxis.uzuki.live.richutilskt.utils.runAsync
import pyxis.uzuki.live.richutilskt.utils.runOnUiThread
import javax.inject.Inject

@InjectViewState
class MyAddressQrPresenter @Inject constructor() : BasePresenter<MyAddressQrView>() {

    private val identicon = Identicon()

    private fun generateQrCodeObservable(uri: String?, dimensions: Int): Observable<Bitmap> {
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

    fun generateAvatars(address: String?, image: AppCompatImageView) {
        Observable.fromCallable {
            return@fromCallable identicon.create(address)
        }.compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe {
                    viewState.afterSuccessGenerateAvatar(it, image)
                }
    }

    fun generateQRCode(text: String?, size: Int) {
        addSubscription(generateQrCodeObservable(text, size)
                .compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe { viewState.showQRCode(it) })
    }

    fun loadAliases() {
        runAsync {
            addSubscription(
                    queryAllAsSingle<Alias>().toObservable()
                            .observeOn(AndroidSchedulers.mainThread())
                            .map { aliases ->
                                val ownAliases = aliases.filter { it.own }
                                runOnUiThread { viewState.afterSuccessLoadAliases(ownAliases) }
                            }
                            .observeOn(Schedulers.io())
                            .flatMap {
                                apiDataManager.loadAliases()
                            }
                            .compose(RxUtil.applyObservableDefaultSchedulers())
                            .subscribe {
                                runOnUiThread { viewState.afterSuccessLoadAliases(it) }
                            })
        }
    }
}
