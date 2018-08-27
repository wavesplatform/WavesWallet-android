package com.wavesplatform.wallet.v2.ui.auth.new_account

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Color
import android.support.v7.widget.AppCompatImageView
import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import com.wavesplatform.wallet.v2.ui.custom.Identicon
import com.wavesplatform.wallet.v2.util.RxUtil
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import java.util.*
import javax.inject.Inject

@InjectViewState
class NewAccountPresenter @Inject constructor() : BasePresenter<NewAccountView>() {
    var accountNameFieldValid = false
    var avatarValid = false
    var createPasswordFieldValid = false
    var confirmPasswordFieldValid = false

    fun isAllFieldsValid(): Boolean {
        return accountNameFieldValid && createPasswordFieldValid && confirmPasswordFieldValid && avatarValid
    }

    @SuppressLint("CheckResult")
    fun generateAvatars(children: List<AppCompatImageView>) {
        Observable.fromIterable(children)
                .flatMap {
                    return@flatMap Observable.zip(
                            Observable.fromCallable {
                                val rnd = Random()
                                val color = Color.argb(255,
                                        rnd.nextInt(256),
                                        rnd.nextInt(256),
                                        rnd.nextInt(256))
                                return@fromCallable Identicon.create((1..999).shuffled().last().toString(),
                                        Identicon.Options.Builder()
                                                .setBlankColor(color)
                                                .create())
                            },
                            Observable.just(it),
                            BiFunction<Bitmap, AppCompatImageView, Pair<Bitmap, AppCompatImageView>> {
                                t1, t2 -> Pair(t1, t2)
                            }
                    )
                }
                .compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe {
                    viewState.afterSuccessGenerateAvatar(it.first, it.second)
                }
    }
}
