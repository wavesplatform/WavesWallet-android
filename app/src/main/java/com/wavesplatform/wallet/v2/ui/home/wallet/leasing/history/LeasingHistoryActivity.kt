package com.wavesplatform.wallet.v2.ui.home.wallet.leasing.history

import android.os.Bundle
import android.util.Log
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import kotlinx.android.synthetic.main.activity_leasing_history.*
import pers.victor.ext.dp2px
import pers.victor.ext.gone
import pers.victor.ext.visiable
import javax.inject.Inject


class LeasingHistoryActivity : BaseActivity(), LeasingHistoryView {

    @Inject
    @InjectPresenter
    lateinit var presenter: LeasingHistoryPresenter

    @ProvidePresenter
    fun providePresenter(): LeasingHistoryPresenter = presenter

    override fun configLayoutRes() = R.layout.activity_leasing_history


    override fun onViewReady(savedInstanceState: Bundle?) {
        setupToolbar(toolbar_view, View.OnClickListener { onBackPressed() }, true, getString(R.string.leasing_history_toolbar_title), R.drawable.ic_toolbar_back_black)

        viewpager_history.adapter = LeasingHistoryFragmentPageAdapter(supportFragmentManager, arrayOf(getString(R.string.history_all),
                getString(R.string.history_active_now), getString(R.string.history_canceled)))
        stl_history.setViewPager(viewpager_history)
        appbar_layout.addOnOffsetChangedListener({ appBarLayout, verticalOffset ->
            Log.d("test", "vertical offset: ${verticalOffset}")
            val offsetForShowShadow = appbar_layout.totalScrollRange - dp2px(9)
            if (-verticalOffset > offsetForShowShadow) {
                viewpager_history.setPagingEnabled(false)
                view_shadow.visiable()
            } else {
                viewpager_history.setPagingEnabled(true)
                view_shadow.gone()
            }
        })
        stl_history.currentTab = 0
    }

}
