package com.wavesplatform.wallet.v2.ui.home.profile.addresses.alias.create

import android.content.Intent
import android.os.Bundle
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.jakewharton.rxbinding2.widget.RxTextView
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.model.remote.response.Alias
import com.wavesplatform.wallet.v2.data.rules.AliasRule
import com.wavesplatform.wallet.v2.data.rules.MinTrimRule
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.util.notNull
import com.wavesplatform.wallet.v2.util.showError
import io.github.anderscheow.validator.Validation
import io.github.anderscheow.validator.Validator
import io.github.anderscheow.validator.constant.Mode
import io.github.anderscheow.validator.rules.common.MaxRule
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import kotlinx.android.synthetic.main.activity_create_alias.*
import pers.victor.ext.app
import pers.victor.ext.click
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class CreateAliasActivity : BaseActivity(), CreateAliasView {

    @Inject
    @InjectPresenter
    lateinit var presenter: CreateAliasPresenter

    var validator = Validator.with(app).setMode(Mode.CONTINUOUS)

    @ProvidePresenter
    fun providePresenter(): CreateAliasPresenter = presenter

    companion object {
        var RESULT_ALIAS = "alias"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        overridePendingTransition(R.anim.slide_in_right, R.anim.null_animation)
        super.onCreate(savedInstanceState)
    }

    override fun configLayoutRes(): Int = R.layout.activity_create_alias

    override fun onViewReady(savedInstanceState: Bundle?) {
        setupToolbar(toolbar_view, true, getString(R.string.new_alias_toolbar_title), R.drawable.ic_toolbar_back_black, { onBackPressed() })

        val aliasValidation = Validation(til_new_alias_symbol)
                .and(MinTrimRule(4, R.string.new_alias_min_validation_error))
                .and(MaxRule(30, R.string.new_alias_max_validation_error))
                .and(AliasRule(R.string.new_alias_invalid_char_validation_error))

        val validateObservable = Observable.create<Boolean> { emitter ->
            try {
                validator.validate(object : Validator.OnValidateListener {
                    override fun onValidateSuccess(values: List<String>) {
                        emitter.onNext(true)
                    }

                    override fun onValidateFailed() {
                        emitter.onNext(false)
                    }
                }, aliasValidation)
            } catch (e: Exception) {
                emitter.onError(e)
            }
        }

        eventSubscriptions.add(RxTextView.textChanges(edit_new_alias_symbol)
                .skipInitialValue()
                .map {
                    button_create_alias.isEnabled = false
                    return@map it.toString()
                }
                .filter { !it.isEmpty() }
                .distinctUntilChanged()
                .flatMap {
                    return@flatMap Observable.zip(validateObservable, Observable.just(it), BiFunction { t1: Boolean, t2: String ->
                        Pair(t1, t2)
                    })
                }
                .filter { it.first }
                .map {
                    if (presenter.wavesBalance.getAvailableBalance() ?: 0 < Constants.WAVES_FEE){
                        button_create_alias.isEnabled = false
                        til_new_alias_symbol.error =  getString(R.string.buy_and_sell_not_enough, presenter.wavesBalance.getName())
                        return@map ""
                    }else{
                        return@map it.second
                    }
                }
                .filter { it.isNotEmpty() }
                .debounce(500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    presenter.loadAlias(it)
                })

        button_create_alias.click {
            presenter.createAlias(edit_new_alias_symbol.text?.trim()?.toString())
        }

        presenter.loadWavesBalance()
    }

    override fun aliasIsAvailable() {
        button_create_alias.isEnabled = !edit_new_alias_symbol.text.toString().trim().isEmpty()
        if (til_new_alias_symbol.error == getString(R.string.new_alias_already_use_validation_error))
            til_new_alias_symbol.error = ""
    }

    override fun aliasIsNotAvailable() {
        button_create_alias.isEnabled = false
        til_new_alias_symbol.error = getString(R.string.new_alias_already_use_validation_error)
    }

    override fun onBackPressed() {
        setResult(Constants.RESULT_CANCELED)
        finish()
        overridePendingTransition(R.anim.null_animation, R.anim.slide_out_right)
    }

    override fun successCreateAlias(alias: Alias) {
        setResult(Constants.RESULT_OK, Intent().apply {
            putExtra(RESULT_ALIAS, alias)
        })
        finish()
        overridePendingTransition(R.anim.null_animation, R.anim.slide_out_right)
    }

    override fun failedCreateAlias(message: String?) {
        message.notNull {
            showError(it, R.id.root)
        }
    }
}