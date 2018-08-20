package com.wavesplatform.wallet.v2.ui.home.profile.addresses.alias.create

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.jakewharton.rxbinding2.widget.RxTextView
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.model.remote.request.AliasRequest
import com.wavesplatform.wallet.v2.data.model.remote.response.Alias
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_create_alias.*
import pers.victor.ext.click
import pers.victor.ext.toast
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class CreateAliasActivity : BaseActivity(), CreateAliasView {

    @Inject
    @InjectPresenter
    lateinit var presenter: CreateAliasPresenter

    @ProvidePresenter
    fun providePresenter(): CreateAliasPresenter = presenter

    companion object {
        var RESULT_ALIAS = "alias"
    }

    override fun configLayoutRes(): Int = R.layout.activity_create_alias

    override fun onViewReady(savedInstanceState: Bundle?) {
        setupToolbar(toolbar_view, View.OnClickListener { onBackPressed() }, true, getString(R.string.new_alias_toolbar_title), R.drawable.ic_toolbar_back_black)

        eventSubscriptions.add(RxTextView.textChanges(edit_new_alias_symbol)
                .skipInitialValue()
                .map({
                    button_create_alias.isEnabled = false
                    return@map it.toString()
                })
                .filter({ !it.isEmpty() })
                .debounce(500, TimeUnit.MILLISECONDS)
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    presenter.loadAlias(it)
                }))


        button_create_alias.click {
            presenter.createAlias(edit_new_alias_symbol.text.toString())
        }
    }

    override fun aliasIsAvailable() {
        button_create_alias.isEnabled = !edit_new_alias_symbol.text.toString().isEmpty()
    }

    override fun aliasIsNotAvailable() {
        button_create_alias.isEnabled = false
    }

    override fun successCreateAlias(it: AliasRequest) {
        setResult(Constants.RESULT_OK, Intent().apply {
            putExtra(RESULT_ALIAS, Alias(publicKeyAccountHelper.publicKeyAccount?.address, it.alias))
        })
        finish()
    }

}