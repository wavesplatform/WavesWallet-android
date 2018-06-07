package com.wavesplatform.wallet.v2.ui.home.history

import android.os.Bundle
import android.support.v4.view.ViewPager
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.flyco.tablayout.listener.OnTabSelectListener
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.base.view.BaseFragment
import com.wavesplatform.wallet.v2.ui.home.history.adapter.HistoryFragmentPageAdapter
import kotlinx.android.synthetic.main.fragment_history.*
import pers.victor.ext.addOnPageChangeListener
import javax.inject.Inject

class HistoryFragment : BaseFragment(), HistoryView, OnTabSelectListener, ViewPager.OnPageChangeListener {

    @Inject
    @InjectPresenter
    lateinit var presenter: HistoryPresenter

    @ProvidePresenter
    fun providePresenter(): HistoryPresenter = presenter

    override fun configLayoutRes(): Int = R.layout.fragment_history

    companion object {

        /**
         * @return HistoryFragment instance
         * */
        fun newInstance(): HistoryFragment {
            return HistoryFragment()
        }
    }

    override fun onViewReady(savedInstanceState: Bundle?) {
        setupUI()
    }

    private fun setupUI() {
        stl_history.setTabData(arrayOf(getString(R.string.all), getString(R.string.sent),getString(R.string.received)))

        viewpager_history.adapter = HistoryFragmentPageAdapter(childFragmentManager)

        stl_history.setOnTabSelectListener(this)
        viewpager_history.addOnPageChangeListener(this)

        stl_history.currentTab = 0
    }

    override fun onTabSelect(position: Int) {
        viewpager_history.currentItem = position
    }

    override fun onPageSelected(position: Int) {
        stl_history.currentTab = position
    }

    override fun onTabReselect(position: Int) {}

    override fun onPageScrollStateChanged(state: Int) {}

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

}
