package com.wavesplatform.wallet.v2.ui.home.history

import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.local.HistoryTab
import com.wavesplatform.wallet.v2.ui.base.view.BaseFragment
import com.wavesplatform.wallet.v2.ui.home.MainActivity
import com.wavesplatform.wallet.v2.ui.home.history.tab.HistoryTabFragment
import com.wavesplatform.wallet.v2.util.notNull
import kotlinx.android.synthetic.main.fragment_history.*
import pers.victor.ext.gone
import pers.victor.ext.visiable
import pyxis.uzuki.live.richutilskt.utils.runDelayed
import javax.inject.Inject

class HistoryFragment : BaseFragment(), HistoryView {

    @Inject
    @InjectPresenter
    lateinit var presenter: HistoryPresenter
    private var onElevationAppBarChangeListener: MainActivity.OnElevationAppBarChangeListener? = null

    @ProvidePresenter
    fun providePresenter(): HistoryPresenter = presenter

    override fun configLayoutRes(): Int = R.layout.fragment_history

    override fun onViewReady(savedInstanceState: Bundle?) {
        setupUI()
    }

    private fun setupUI() {
        val tabs = arguments?.getParcelableArrayList<HistoryTab>(BUNDLE_TABS)
        val list = tabs?.map {
            val fragment = HistoryTabFragment.newInstance(it.data, arguments?.getParcelable(BUNDLE_ASSET))
            fragment.changeTabBarVisibilityListener = object : HistoryTabFragment.ChangeTabBarVisibilityListener {
                override fun changeTabBarVisibility(show: Boolean, onlyExpand: Boolean) {
                    if (show) {
                        appbar_layout.setExpanded(true, false)
                        appbar_layout.visiable()
                    } else {
                        if (appbar_layout.visibility != View.GONE) {
                            appbar_layout.setExpanded(false, false)
                            runDelayed(100) {
                                appbar_layout.gone()
                            }
                        }
                    }
                }
            }

            return@map Pair(fragment, it.title)
        }?.toMutableList()

        list.notNull {
            viewpager_history.adapter = HistoryFragmentPageAdapter(childFragmentManager, it)
            viewpager_history.offscreenPageLimit = 7
        }

        stl_history.setViewPager(viewpager_history)
        stl_history.currentTab = 0
        appbar_layout.addOnOffsetChangedListener(
                AppBarLayout.OnOffsetChangedListener { _, verticalOffset ->
                    onElevationAppBarChangeListener.notNull {
                        presenter.hideShadow = verticalOffset == 0
                        onElevationAppBarChangeListener?.onChange(presenter.hideShadow)
                        viewpager_history.setPagingEnabled(presenter.hideShadow)
                    }
                })
    }

    fun setOnElevationChangeListener(listener: MainActivity.OnElevationAppBarChangeListener) {
        this.onElevationAppBarChangeListener = listener
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            applyElevation()
        }
    }

    private fun applyElevation() {
        onElevationAppBarChangeListener?.let {
            onElevationAppBarChangeListener?.onChange(presenter.hideShadow)
        }
    }

    companion object {
        const val BUNDLE_TABS = "tabs"
        const val BUNDLE_ASSET = "asset"

        fun newInstance(): HistoryFragment {
            return HistoryFragment()
        }
    }
}
