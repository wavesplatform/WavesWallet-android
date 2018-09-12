package com.wavesplatform.wallet.v2.ui.auth.new_account.backup_info

import android.os.Bundle
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.ui.home.profile.backup.BackupPhraseActivity
import com.wavesplatform.wallet.v2.util.launchActivity
import kotlinx.android.synthetic.main.activity_backup_info.*
import pers.victor.ext.click
import javax.inject.Inject


class BackupInfoActivity : BaseActivity(), BackupInfoView {

    @Inject
    @InjectPresenter
    lateinit var presenter: BackupInfoPresenter

    @ProvidePresenter
    fun providePresenter(): BackupInfoPresenter = presenter

    override fun configLayoutRes() = R.layout.activity_backup_info


    override fun onViewReady(savedInstanceState: Bundle?) {
        button_confirm.click {
            launchActivity<BackupPhraseActivity>(options = intent.extras)
        }
    }
}
