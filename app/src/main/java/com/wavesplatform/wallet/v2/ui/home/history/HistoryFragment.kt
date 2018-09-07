package com.wavesplatform.wallet.v2.ui.home.history

import android.os.Bundle
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.local.HistoryTab
import com.wavesplatform.wallet.v2.ui.base.view.BaseFragment
import com.wavesplatform.wallet.v2.ui.home.history.tab.HistoryTabFragment
import com.wavesplatform.wallet.v2.util.notNull
import kotlinx.android.synthetic.main.fragment_history.*
import pers.victor.ext.dp2px
import pers.victor.ext.gone
import pers.victor.ext.visiable
import javax.inject.Inject

class HistoryFragment : BaseFragment(), HistoryView {

    @Inject
    @InjectPresenter
    lateinit var presenter: HistoryPresenter

    @ProvidePresenter
    fun providePresenter(): HistoryPresenter = presenter

    override fun configLayoutRes(): Int = R.layout.fragment_history

    companion object {
        var BUNDLE_TABS = "tabs"
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
        val tabs = arguments?.getParcelableArrayList<HistoryTab>(BUNDLE_TABS)
        val list = tabs?.map {
            return@map Pair(HistoryTabFragment.newInstance(it.data), it.title)
        }?.toMutableList()

        list.notNull {
            viewpager_history.adapter = HistoryFragmentPageAdapter(childFragmentManager, it)
        }

        stl_history.setViewPager(viewpager_history)
        appbar_layout.addOnOffsetChangedListener({ appBarLayout, verticalOffset ->
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

//    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
//        inflater.inflate(R.menu.menu_history, menu)
//        super.onCreateOptionsMenu(menu, inflater)
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
//        when(item?.itemId){
//            R.id.action_sorting -> {
//                val historyFilter = HistoryFilterBottomSheetFragment()
//                historyFilter.show(fragmentManager, historyFilter.tag)
//            }
//        }
//
//        return super.onOptionsItemSelected(item)
//    }

}
