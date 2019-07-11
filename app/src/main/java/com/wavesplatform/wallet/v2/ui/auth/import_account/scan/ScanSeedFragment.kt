/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.auth.import_account.scan

import android.os.Bundle
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.analytics.AnalyticEvents
import com.wavesplatform.wallet.v2.data.analytics.analytics
import com.wavesplatform.wallet.v2.ui.base.view.BaseFragment
import com.wavesplatform.wallet.v2.util.launchQrCodeScanner
import kotlinx.android.synthetic.main.fragment_scan_seed.*
import pers.victor.ext.click
import javax.inject.Inject

class ScanSeedFragment : BaseFragment(), ScanSeedView {

    @Inject
    @InjectPresenter
    lateinit var presenter: ScanSeedPresenter

    @ProvidePresenter
    fun providePresenter(): ScanSeedPresenter = presenter

    override fun configLayoutRes() = R.layout.fragment_scan_seed

    override fun onViewReady(savedInstanceState: Bundle?) {
        button_scan.click {
            analytics.trackEvent(AnalyticEvents.StartImportScanEvent)

            baseActivity.launchQrCodeScanner()
        }
    }
}
