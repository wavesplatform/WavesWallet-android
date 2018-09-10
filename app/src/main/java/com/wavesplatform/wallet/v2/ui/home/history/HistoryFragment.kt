package com.wavesplatform.wallet.v2.ui.home.history

import android.os.Bundle
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.base.view.BaseFragment
import kotlinx.android.synthetic.main.fragment_history.*
import pers.victor.ext.dp2px
import pers.victor.ext.gone
import pers.victor.ext.visiable
import javax.inject.Inject

class HistoryFragment : BaseFragment(), HistoryView {

    @Inject
    @InjectPresenter
    lateinit var presenter: HistoryPresenter
    private lateinit var adapter: HistoryFragmentPageAdapter

    @ProvidePresenter
    fun providePresenter(): HistoryPresenter = presenter

    override fun configLayoutRes(): Int = R.layout.fragment_history

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        adapter = HistoryFragmentPageAdapter(
                childFragmentManager,
                arrayOf(getString(R.string.history_all),
                        getString(R.string.history_sent),
                        getString(R.string.history_received),
                        getString(R.string.history_exchanged),
                        getString(R.string.history_leased),
                        getString(R.string.history_issued)))
    }

    override fun onViewReady(savedInstanceState: Bundle?) {
        setupUI()
    }

    private fun setupUI() {
        viewpager_history.adapter = adapter
        stl_history.setViewPager(viewpager_history)
        appbar_layout.addOnOffsetChangedListener { _, verticalOffset ->
            val offsetForShowShadow = appbar_layout.totalScrollRange - dp2px(9)
            if (-verticalOffset > offsetForShowShadow) {
                viewpager_history.setPagingEnabled(false)
                view_shadow.visiable()
            } else {
                viewpager_history.setPagingEnabled(true)
                view_shadow.gone()
            }
        }
        stl_history.currentTab = HistoryFragmentPageAdapter.ALL
    }

    companion object {

        fun newInstance(): HistoryFragment {
            return HistoryFragment()
        }
    }
}
