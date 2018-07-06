package com.wavesplatform.wallet.v2.ui.home.profile.addresses.create

import android.os.Bundle
import android.view.View
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import kotlinx.android.synthetic.main.activity_create_alias.*
import pers.victor.ext.addTextChangedListener

class CreateAliasActivity :BaseActivity(),CreateAliasView{


    override fun configLayoutRes(): Int = R.layout.activity_create_alias

    override fun onViewReady(savedInstanceState: Bundle?) {
        setupToolbar(toolbar_view, View.OnClickListener { onBackPressed() }, true, getString(R.string.new_alias), R.drawable.ic_toolbar_back_black)

        edit_new_alias_symbol.addTextChangedListener {
            on { s, start, before, count ->
                button_create_alias.isEnabled = !edit_new_alias_symbol.text.trim().isEmpty()
            }
        }
    }

}