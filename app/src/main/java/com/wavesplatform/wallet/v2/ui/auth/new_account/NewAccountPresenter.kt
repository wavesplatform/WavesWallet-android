package com.wavesplatform.wallet.v2.ui.auth.new_account

import android.annotation.SuppressLint
import android.support.v7.widget.AppCompatImageView
import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.sdk.crypto.WavesWallet
import com.wavesplatform.sdk.Wavesplatform
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.sdk.utils.Identicon
import com.wavesplatform.wallet.v2.util.RxUtil
import io.reactivex.Observable
import org.apache.commons.io.Charsets
import javax.inject.Inject

@InjectViewState
class NewAccountPresenter @Inject constructor() : BasePresenter<NewAccountView>() {
    var accountNameFieldValid = false
    var avatarValid = false
    var createPasswordFieldValid = false
    var confirmPasswordFieldValid = false
    private val identicon = Identicon()

    fun isAllFieldsValid(): Boolean {
        return accountNameFieldValid && createPasswordFieldValid && confirmPasswordFieldValid
    }

    @SuppressLint("CheckResult")
    fun generateSeeds(children: List<AppCompatImageView>) {
        Observable.fromIterable(children)
                .map {
                    val seed = Wavesplatform.generateSeed()
                    val wallet = WavesWallet(seed.toByteArray(Charsets.UTF_8))
                    return@map Triple(seed, identicon.create(wallet.address), it)
                }
                .compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe { t ->
                    viewState.afterSuccessGenerateAvatar(t.first, t.second, t.third)
                }
    }
}
