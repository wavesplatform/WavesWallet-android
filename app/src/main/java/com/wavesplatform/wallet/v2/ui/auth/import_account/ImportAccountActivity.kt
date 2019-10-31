/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.auth.import_account

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.google.zxing.integration.android.IntentIntegrator
import com.wavesplatform.sdk.utils.WAVES_PREFIX
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.auth.import_account.protect_account.ProtectAccountActivity
import com.wavesplatform.wallet.v2.ui.auth.new_account.NewAccountActivity
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.util.REQUEST_SCAN_QR_CODE
import com.wavesplatform.wallet.v2.util.launchActivity
import com.wavesplatform.wallet.v2.util.showError
import kotlinx.android.synthetic.main.activity_import_account.*
import javax.inject.Inject

class ImportAccountActivity : BaseActivity(), ImportAccountView {

    @Inject
    @InjectPresenter
    lateinit var presenter: ImportAccountPresenter

    @ProvidePresenter
    fun providePresenter(): ImportAccountPresenter = presenter

    override fun configLayoutRes() = R.layout.activity_import_account

    override fun askPassCode() = false

    override fun onViewReady(savedInstanceState: Bundle?) {
        setStatusBarColor(R.color.basic50)
        setNavigationBarColor(R.color.basic50)
        setupToolbar(toolbar_view, true, title = getString(R.string.import_account_toolbar_title), icon = R.drawable.ic_toolbar_back_black)

        viewpager_import.adapter = ImportAccountFragmentPageAdapter(supportFragmentManager, arrayOf(getString(R.string.import_account_tab_scan),
                getString(R.string.import_account_tab_manually)))

        stl_import_tabs.setViewPager(viewpager_import)
        stl_import_tabs.currentTab = 0
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_CANCELED)
        finish()
        overridePendingTransition(R.anim.null_animation, R.anim.slide_out_right)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_SCAN_QR_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    val result = IntentIntegrator.parseActivityResult(resultCode, data)
                    val seed = result.contents.replace(WAVES_PREFIX, "")
                    if (!TextUtils.isEmpty(seed)) {
                        when {
                            App.accessManager.isAccountWithSeedExist(seed) -> {
                                showError(R.string.enter_seed_manually_validation_seed_exists_error, R.id.root_view)
                            }
                            seed.length < 24 -> {
                                showError(R.string.enter_seed_manually_validation_seed_is_invalid_error, R.id.root_view)
                            }
                            else -> {
                                launchActivity<ProtectAccountActivity> {
                                    putExtra(NewAccountActivity.KEY_INTENT_SEED, seed)
                                    putExtra(NewAccountActivity.KEY_INTENT_PROCESS_ACCOUNT_IMPORT, true)
                                }
                            }
                        }
                    } else {
                        showError(R.string.enter_seed_manually_validation_seed_is_invalid_error, R.id.root_view)
                    }
                }
            }
        }
    }

    override fun needToShowNetworkMessage(): Boolean = true
}
