package com.wavesplatform.wallet.v2.ui.home.profile.address_book

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.local.LanguageItem
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import kotlinx.android.synthetic.main.activity_address_book.*
import pers.victor.ext.addTextChangedListener
import javax.inject.Inject
import android.support.constraint.solver.widgets.WidgetContainer.getBounds
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View.OnTouchListener
import com.jakewharton.rxbinding2.widget.RxTextView
import pers.victor.ext.gone
import java.util.concurrent.TimeUnit


class AddressBookActivity : BaseActivity(), AddressBookView {

    @Inject
    @InjectPresenter
    lateinit var presenter: AddressBookPresenter

    @Inject
    lateinit var adapter: AddressBookAdapter

    @ProvidePresenter
    fun providePresenter(): AddressBookPresenter = presenter

    override fun configLayoutRes() = R.layout.activity_address_book


    override fun onViewReady(savedInstanceState: Bundle?) {

        setupToolbar(toolbar_view, View.OnClickListener { onBackPressed() }, true, getString(R.string.address_book_toolbar_title), R.drawable.ic_toolbar_back_black)


        edit_search.addTextChangedListener {
            on { s, start, before, count ->
                if (edit_search.text.isNotEmpty()){
                    edit_search.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_search_24_basic_500, 0, R.drawable.ic_clear_24_basic_500, 0)
                }else{
                    edit_search.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_search_24_basic_500, 0, 0, 0)
                }
            }
        }

        edit_search.setOnTouchListener(OnTouchListener { v, event ->
            val DRAWABLE_RIGHT = 2

            if (edit_search.compoundDrawables[DRAWABLE_RIGHT] != null){
                if (event.action == MotionEvent.ACTION_UP) {
                    if (event.rawX >= edit_search.right - edit_search.compoundDrawables[DRAWABLE_RIGHT].bounds.width()) {
                        edit_search.text = null

                        return@OnTouchListener true
                    }
                }
            }

            false
        })

        recycle_addresses.layoutManager = LinearLayoutManager(this)
        recycle_addresses.adapter = adapter
        adapter.bindToRecyclerView(recycle_addresses)

        presenter.getAddresses()

        adapter.onItemClickListener = BaseQuickAdapter.OnItemClickListener { adapter, view, position ->
            val item = adapter.getItem(position) as AddressTestObject
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_add_address -> {
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_address_book, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun afterSuccessGetAddress(list: List<AddressTestObject>) {
        if (list.isEmpty()){
            edit_search.gone()
        }
        adapter.allData = ArrayList(list)
        adapter.setNewData(list)
        adapter.setEmptyView(R.layout.address_book_empty_state)
    }

}
