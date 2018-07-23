package com.wavesplatform.wallet.v2.ui.home.dex

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.base.view.BaseFragment
import com.wavesplatform.wallet.v2.ui.home.dex.sorting.ActiveMarketsSortingActivity
import com.wavesplatform.wallet.v2.ui.home.wallet.assets.TestObject
import com.wavesplatform.wallet.v2.util.launchActivity
import kotlinx.android.synthetic.main.fragment_dex_new.*
import pers.victor.ext.gone
import pers.victor.ext.toast
import pers.victor.ext.visiable
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class DexFragment :BaseFragment(),DexView{

    @Inject
    @InjectPresenter
    lateinit var presenter: DexPresenter

    @ProvidePresenter
    fun providePresenter(): DexPresenter = presenter

    @Inject
    lateinit var adapter: DexAdapter
    var menu: Menu? = null

    override fun configLayoutRes(): Int = R.layout.fragment_dex_new

    companion object {

        /**
         * @return DexFragment instance
         * */
        fun newInstance(): DexFragment {
            return DexFragment()
        }
    }

    override fun onViewReady(savedInstanceState: Bundle?) {

        recycle_dex.layoutManager = LinearLayoutManager(baseActivity)
        recycle_dex.adapter = adapter
        recycle_dex.isNestedScrollingEnabled = false

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_dex, menu)
        this.menu = menu
        presenter.loadActiveMarkets()
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){
            R.id.action_sorting -> {
                launchActivity<ActiveMarketsSortingActivity> {  }
            }
            R.id.action_add_market -> {
                toast(item.title)
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun afterSuccessLoadMarkets(list: ArrayList<TestObject>) {
        if (list.isEmpty()){
            linear_content.gone()
            linear_empty.visiable()
            menu?.findItem(R.id.action_sorting)?.isVisible = false
        }else{
            linear_empty.gone()
            linear_content.visiable()
            menu?.findItem(R.id.action_sorting)?.isVisible = true
            adapter.setNewData(list)
        }
    }

    override fun afterFailedLoadMarkets() {
    }
}
