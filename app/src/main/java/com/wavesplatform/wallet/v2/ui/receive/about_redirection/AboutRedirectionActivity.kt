package com.wavesplatform.wallet.v2.ui.receive.about_redirection

import android.os.Bundle
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import kotlinx.android.synthetic.main.activity_about_redirection.*
import pers.victor.ext.click

class AboutRedirectionActivity : BaseActivity(), AboutRedirectionView {
    override fun onViewReady(savedInstanceState: Bundle?) {
        button_okay.click {
            setResult(Constants.RESULT_OK)
            finish()
        }
    }

    override fun configLayoutRes(): Int = R.layout.activity_about_redirection
}
