package com.wavesplatform.wallet.v2.ui.home.profile.address_book

import android.app.Activity
import android.content.Intent
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
import android.support.v7.widget.AppCompatCheckBox
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View.OnTouchListener
import com.jakewharton.rxbinding2.widget.RxTextView
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.ui.home.profile.address_book.add.AddAddressActivity
import com.wavesplatform.wallet.v2.ui.home.profile.address_book.edit.EditAddressActivity
import com.wavesplatform.wallet.v2.util.launchActivity
import com.wavesplatform.wallet.v2.util.notNull
import pers.victor.ext.gone
import pyxis.uzuki.live.richutilskt.utils.runDelayed
import java.util.concurrent.TimeUnit


class AddressBookActivity : BaseActivity(), AddressBookView {

    @Inject
    @InjectPresenter
    lateinit var presenter: AddressBookPresenter

    @Inject
    lateinit var adapter: AddressBookAdapter

    @ProvidePresenter
    fun providePresenter(): AddressBookPresenter = presenter

    companion object {
        var SCREEN_TYPE_NOT_EDITABLE = 998
        var SCREEN_TYPE_EDITABLE = 999
        var REQUEST_ADD_ADDRESS = 101
        var REQUEST_EDIT_ADDRESS = 102
        var BUNDLE_SCREEN_TYPE = "screen_type"
        var BUNDLE_ADDRESS_ITEM = "item"
        var BUNDLE_POSITION = "position"
        var BUNDLE_TYPE = "type"
    }

    override fun configLayoutRes() = R.layout.activity_address_book


    override fun onViewReady(savedInstanceState: Bundle?) {

        setupToolbar(toolbar_view, View.OnClickListener { onBackPressed() }, true, getString(R.string.address_book_toolbar_title), R.drawable.ic_toolbar_back_black)


        edit_search.addTextChangedListener {
            on { s, start, before, count ->
                if (edit_search.text.isNotEmpty()) {
                    edit_search.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_search_24_basic_500, 0, R.drawable.ic_clear_24_basic_500, 0)
                } else {
                    edit_search.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_search_24_basic_500, 0, 0, 0)
                }
            }
        }

        edit_search.setOnTouchListener(OnTouchListener { v, event ->
            val DRAWABLE_RIGHT = 2

            if (edit_search.compoundDrawables[DRAWABLE_RIGHT] != null) {
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
        adapter.screenType = intent.getIntExtra(BUNDLE_SCREEN_TYPE, AddressBookScreenType.EDIT.type)
        adapter.bindToRecyclerView(recycle_addresses)

        presenter.getAddresses()

        adapter.onItemClickListener = BaseQuickAdapter.OnItemClickListener { adapter, view, position ->
            val item = adapter.getItem(position) as AddressTestObject
            if (this.adapter.screenType == AddressBookScreenType.EDIT.type) {
                launchActivity<EditAddressActivity>(REQUEST_EDIT_ADDRESS) {
                    putExtra(BUNDLE_ADDRESS_ITEM, item)
                    putExtra(BUNDLE_POSITION, position)
                    putExtra(BUNDLE_TYPE, SCREEN_TYPE_EDITABLE)
                }
            } else if (this.adapter.screenType == AddressBookScreenType.CHOOSE.type) {
                val viewItem = recycle_addresses.layoutManager.findViewByPosition(position)
                val checkBox = viewItem.findViewById<AppCompatCheckBox>(R.id.checkbox_choose)
                checkBox.isChecked = true

                // disable click for next items, which user click before activity will finish
                adapter.onItemClickListener = BaseQuickAdapter.OnItemClickListener { adapter, view, position -> }

                // finish activity with result and timeout
                runDelayed(500, {
                    setResult(Activity.RESULT_OK, Intent().apply {
                        putExtra(BUNDLE_ADDRESS_ITEM, item)
                    })
                    finish()
                })
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_ADD_ADDRESS -> {
                if (resultCode == Constants.RESULT_OK) {
                    val item = data?.getParcelableExtra<AddressTestObject>(BUNDLE_ADDRESS_ITEM)
                    item.notNull {
                        adapter.addData(it)
                        adapter.allData.add(it)
                    }
                }
            }
            REQUEST_EDIT_ADDRESS -> {
                if (resultCode == Constants.RESULT_OK) {
                    val position = data?.getIntExtra(BUNDLE_POSITION, -1)
                    val item = data?.getParcelableExtra<AddressTestObject>(BUNDLE_ADDRESS_ITEM)
                    position.notNull { position ->
                        if (position != -1) {
                            item.notNull {
                                adapter.setData(position, it)
                                adapter.allData.add(position, it)
                            }
                        }
                    }

                } else if (resultCode == Constants.RESULT_OK_NO_RESULT) {
                    val position = data?.getIntExtra(BUNDLE_POSITION, -1)
                    position.notNull {
                        if (it != -1) {
                            adapter.remove(it)
                            adapter.allData.removeAt(it)
                        }
                    }
                }
            }
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_add_address -> {
                launchActivity<AddAddressActivity>(REQUEST_ADD_ADDRESS) {
                    putExtra(BUNDLE_TYPE, SCREEN_TYPE_EDITABLE)
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_address_book, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun afterSuccessGetAddress(list: ArrayList<AddressTestObject>) {
        if (list.isEmpty()) {
            edit_search.gone()
        }
        adapter.allData = ArrayList(list)
        adapter.setNewData(list)
        adapter.setEmptyView(R.layout.address_book_empty_state)
    }

    enum class AddressBookScreenType(var type: Int) {
        EDIT(0),
        CHOOSE(1)
    }

}
