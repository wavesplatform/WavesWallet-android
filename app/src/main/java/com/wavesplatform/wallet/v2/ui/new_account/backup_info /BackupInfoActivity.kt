package com.wavesplatform.wallet.v2.ui.new_account.backup_info

import android.os.Bundle
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.new_account.backup_info .BackupInfoPresenter
import com.wavesplatform.wallet.v2.ui.new_account.backup_info .BackupInfoView
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import javax.inject.Inject


class BackupInfoActivity : BaseActivity(), BackupInfoView {

    @Inject
    @InjectPresenter
    lateinit var presenter: BackupInfoPresenter

    @ProvidePresenter
    fun providePresenter(): BackupInfoPresenter = presenter

    override fun configLayoutRes() = R.layout.activity_backup_info


    override fun onViewReady(savedInstanceState: Bundle?) {

    }

}
