package com.wavesplatform.wallet.v2.ui.home.profile.addresses.alias.create

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.ui.home.profile.addresses.alias.AliasModel
import kotlinx.android.synthetic.main.activity_create_alias.*
import pers.victor.ext.addTextChangedListener
import pers.victor.ext.click

class CreateAliasActivity :BaseActivity(),CreateAliasView{

    companion object {
        var RESULT_ALIAS = "alias"
    }

    override fun configLayoutRes(): Int = R.layout.activity_create_alias

    override fun onViewReady(savedInstanceState: Bundle?) {
        setupToolbar(toolbar_view, View.OnClickListener { onBackPressed() }, true, getString(R.string.new_alias_toolbar_title), R.drawable.ic_toolbar_back_black)

        edit_new_alias_symbol.addTextChangedListener {
            on { s, start, before, count ->
                button_create_alias.isEnabled = !edit_new_alias_symbol.text.trim().isEmpty()
            }
        }

        button_create_alias.click {
            setResult(Constants.RESULT_OK, Intent().apply {
                putExtra(RESULT_ALIAS, AliasModel(edit_new_alias_symbol.text.toString()))
            })
            finish()
        }
    }

}