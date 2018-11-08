package com.wavesplatform.wallet.v2.ui.no_internet

import android.os.Bundle
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.ui.splash.SplashActivity
import com.wavesplatform.wallet.v2.util.isNetworkConnection
import com.wavesplatform.wallet.v2.util.launchActivity
import com.wavesplatform.wallet.v2.util.showMessage
import kotlinx.android.synthetic.main.activity_no_internet.*
import pers.victor.ext.click

class NoInternetActivity : BaseActivity(), NoInternetView {

    override fun configLayoutRes() = R.layout.activity_no_internet

    override fun onViewReady(savedInstanceState: Bundle?) {
        button_retry.click { checkConnection() }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        launchActivity<SplashActivity>(clear = true) {
            putExtra(SplashActivity.EXIT, true)
        }
    }

    override fun onResume() {
        super.onResume()
        checkConnection()
    }

    private fun checkConnection() {
        if (isNetworkConnection()) {
            launchActivity<SplashActivity>(clear = true)
        } else {
            showMessage(getString(R.string.no_internet_title), R.id.root)
        }
    }
}