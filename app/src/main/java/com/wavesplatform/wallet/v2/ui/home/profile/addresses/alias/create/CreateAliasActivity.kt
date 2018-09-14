package com.wavesplatform.wallet.v2.ui.home.profile.addresses.alias.create

import android.content.Intent
import android.os.Bundle
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.jakewharton.rxbinding2.widget.RxTextView
import com.wavesplatform.wallet.BlockchainApplication
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v1.data.auth.WavesWallet
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.model.remote.response.Alias
import com.wavesplatform.wallet.v2.data.rules.AlphabetRule
import com.wavesplatform.wallet.v2.ui.auth.new_account.NewAccountActivity
import com.wavesplatform.wallet.v2.ui.auth.passcode.enter.EnterPassCodeActivity
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.util.launchActivity
import io.github.anderscheow.validator.Validation
import io.github.anderscheow.validator.Validator
import io.github.anderscheow.validator.constant.Mode
import io.github.anderscheow.validator.rules.common.MaxRule
import io.github.anderscheow.validator.rules.common.MinRule
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

    override fun configLayoutRes(): Int = R.layout.activity_create_alias

    override fun onViewReady(savedInstanceState: Bundle?) {
        setupToolbar(toolbar_view, true, getString(R.string.new_alias_toolbar_title), R.drawable.ic_toolbar_back_black)

        val aliasValidation = Validation(til_new_alias_symbol)
                .and(MinRule(4, R.string.new_alias_min_validation_error))
                .and(MaxRule(30, R.string.new_alias_max_validation_error))
                .and(AlphabetRule(R.string.new_alias_invalid_char_validation_error))

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
                .map({
                    button_create_alias.isEnabled = false
                    return@map it.toString()
                })
                .filter({ !it.isEmpty() })
                .distinctUntilChanged()
                .flatMap({
                    return@flatMap Observable.zip(validateObservable, Observable.just(it), BiFunction { t1: Boolean, t2: String ->
                        Pair(t1, t2)
                    })
                })
                .filter({ it.first == true })
                .debounce(500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    presenter.loadAlias(it.second)
                }))

        button_create_alias.click {
            launchActivity<EnterPassCodeActivity>(
                    requestCode = EnterPassCodeActivity.REQUEST_ENTER_PASS_CODE) { }
//            presenter.createAlias(edit_new_alias_symbol.text.toString(), wallet.privateKey)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            EnterPassCodeActivity.REQUEST_ENTER_PASS_CODE -> {
                if (resultCode == Constants.RESULT_OK) {
                    val password = data!!.extras.getString(NewAccountActivity.KEY_INTENT_PASSWORD)
                    val wallet = WavesWallet(BlockchainApplication.getAccessManager()
                            .getCurrentWavesWalletEncryptedData(), password)
                    presenter.createAlias(edit_new_alias_symbol.text.toString(), wallet.privateKey, wallet.publicKeyStr)
                }
            }
        }
    }

    override fun aliasIsAvailable() {
        button_create_alias.isEnabled = !edit_new_alias_symbol.text.toString().isEmpty()
        if (til_new_alias_symbol.error == getString(R.string.new_alias_already_use_validation_error))
            til_new_alias_symbol.error = ""
    }

    override fun aliasIsNotAvailable() {
        button_create_alias.isEnabled = false
        til_new_alias_symbol.error = getString(R.string.new_alias_already_use_validation_error)
    }

    override fun successCreateAlias(alias: Alias) {
        setResult(Constants.RESULT_OK, Intent().apply {
            putExtra(RESULT_ALIAS, alias)
        })
        finish()
    }

}