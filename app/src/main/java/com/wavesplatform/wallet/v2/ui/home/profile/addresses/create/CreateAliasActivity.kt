package com.wavesplatform.wallet.v2.ui.home.profile.addresses.create

import android.os.Bundle
import android.view.View
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import kotlinx.android.synthetic.main.activity_create_alias.*

class CreateAliasActivity :BaseActivity(),CreateAliasView{


    override fun configLayoutRes(): Int = R.layout.activity_create_alias

    override fun onViewReady(savedInstanceState: Bundle?) {
        setupToolbar(toolbar_view, View.OnClickListener { onBackPressed() }, true, getString(R.string.new_alias), R.drawable.ic_toolbar_back_black)

    }

}