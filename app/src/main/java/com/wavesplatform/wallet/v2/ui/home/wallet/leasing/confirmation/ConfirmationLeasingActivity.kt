package com.wavesplatform.wallet.v2.ui.home.wallet.leasing.confirmation

import android.os.Bundle
import android.view.View
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import kotlinx.android.synthetic.main.activity_confirm_leasing.*
import pers.victor.ext.click
import pers.victor.ext.gone
import pers.victor.ext.visiable

class ConfirmationLeasingActivity : BaseActivity(), ConfirmationLeasingView {


    override fun configLayoutRes(): Int = R.layout.activity_confirm_leasing

    override fun onViewReady(savedInstanceState: Bundle?) {
        setupToolbar(toolbar_view,true, getString(R.string.confirm_leasing), R.drawable.ic_toolbar_back_white)

        button_confirm.click {
            supportActionBar?.setDisplayShowTitleEnabled(false)
            supportActionBar?.setHomeButtonEnabled(false)
            supportActionBar?.setDisplayHomeAsUpEnabled(false)

            card_leasing_preview_info.gone()
            card_success.visiable()
        }
        button_okay.click { finish() }
    }

}
