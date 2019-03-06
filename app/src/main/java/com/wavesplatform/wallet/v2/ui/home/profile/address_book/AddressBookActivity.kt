package com.wavesplatform.wallet.v2.ui.home.profile.address_book

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.AppCompatCheckBox
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.jakewharton.rxbinding2.widget.RxTextView
import com.mindorks.editdrawabletext.DrawablePosition
import com.mindorks.editdrawabletext.onDrawableClickListener
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.ui.home.profile.address_book.add.AddAddressActivity
import com.wavesplatform.wallet.v2.ui.home.profile.address_book.edit.EditAddressActivity
import com.wavesplatform.wallet.v2.util.launchActivity
import com.wavesplatform.wallet.v2.util.notNull
import com.wavesplatform.wallet.v2.util.showSnackbar
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_address_book.*
import kotlinx.android.synthetic.main.layout_empty_data.view.*
import pers.victor.ext.gone
import pers.victor.ext.visiable
import pyxis.uzuki.live.richutilskt.utils.runDelayed
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AddressBookActivity : BaseActivity(), AddressBookView {

    @Inject
    @InjectPresenter
    lateinit var presenter: AddressBookPresenter

    @Inject
    lateinit var adapter: AddressBookAdapter

    @ProvidePresenter
    fun providePresenter(): AddressBookPresenter = presenter

    override fun configLayoutRes() = R.layout.activity_address_book

    override fun onCreate(savedInstanceState: Bundle?) {
        overridePendingTransition(R.anim.slide_in_right, R.anim.null_animation)
        super.onCreate(savedInstanceState)
    }

    override fun onViewReady(savedInstanceState: Bundle?) {
        setupToolbar(toolbar_view, true, getString(R.string.address_book_toolbar_title), R.drawable.ic_toolbar_back_black)

        eventSubscriptions.add(RxTextView.textChanges(edit_search)
                .skipInitialValue()
                .map {
                    if (it.isNotEmpty()) {
                        edit_search.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_search_24_basic_500, 0, R.drawable.ic_clear_24_basic_500, 0)
                    } else {
                        edit_search.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_search_24_basic_500, 0, 0, 0)
                    }
                    return@map it.toString()
                }
                .debounce(300, TimeUnit.MILLISECONDS)
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    adapter.filter(it)
                })

        edit_search.setDrawableClickListener(object : onDrawableClickListener {
            override fun onClick(target: DrawablePosition) {
                when (target) {
                    DrawablePosition.RIGHT -> {
                        edit_search.text = null
                    }
                }
            }
        })

        recycle_addresses.layoutManager = LinearLayoutManager(this)
        recycle_addresses.adapter = adapter
        adapter.screenType = intent.getIntExtra(BUNDLE_SCREEN_TYPE, AddressBookScreenType.EDIT.type)
        adapter.bindToRecyclerView(recycle_addresses)

        presenter.getAddresses()

        adapter.onItemClickListener = BaseQuickAdapter.OnItemClickListener { adapter, view, position ->
            val item = adapter.getItem(position) as AddressBookUser
            if (this.adapter.screenType == AddressBookScreenType.EDIT.type) {
                launchActivity<EditAddressActivity>(REQUEST_EDIT_ADDRESS) {
                    putExtra(BUNDLE_ADDRESS_ITEM, item)
                    putExtra(BUNDLE_POSITION, position)
                    putExtra(BUNDLE_TYPE, SCREEN_TYPE_EDITABLE)
                }
            } else if (this.adapter.screenType == AddressBookScreenType.CHOOSE.type) {
                val viewItem = (recycle_addresses.layoutManager as LinearLayoutManager)
                        .findViewByPosition(position)
                val checkBox = viewItem?.findViewById<AppCompatCheckBox>(R.id.checkbox_choose)
                checkBox?.isChecked = true

                // disable click for next items, which user click before activity will finish
                adapter.onItemClickListener = BaseQuickAdapter.OnItemClickListener { adapter, view, position -> }

                // finish activity with result and timeout
                runDelayed(500) {
                    setResult(Activity.RESULT_OK, Intent().apply {
                        putExtra(BUNDLE_ADDRESS_ITEM, item)
                    })
                    finish()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_ADD_ADDRESS -> {
                if (resultCode == Constants.RESULT_OK) {
                    val item = data?.getParcelableExtra<AddressBookUser>(BUNDLE_ADDRESS_ITEM)
                    item.notNull {
                        adapter.allData.add(it)
                        adapter.allData.sortBy { it.name }
                        adapter.setNewData(ArrayList(adapter.allData))
                        configureSearchVisibility()
                    }
                }
            }
            REQUEST_EDIT_ADDRESS -> {
                if (resultCode == Constants.RESULT_OK) {
                    val position = data?.getIntExtra(BUNDLE_POSITION, -1)
                    val item = data?.getParcelableExtra<AddressBookUser>(BUNDLE_ADDRESS_ITEM)
                    position.notNull { position ->
                        if (position != -1) {
                            item.notNull {
                                adapter.allData[position] = it
                                adapter.allData.sortBy { it.name }
                                adapter.setNewData(ArrayList(adapter.allData))
                            }
                        }
                    }
                } else if (resultCode == Constants.RESULT_OK_NO_RESULT) {
                    val position = data?.getIntExtra(BUNDLE_POSITION, -1)
                    position?.let {
                        if (position != -1) {
                            adapter.remove(position)
                            adapter.allData.removeAt(position)
                            configureSearchVisibility()
                            showSnackbar(R.string.address_book_success_deleted, R.color.success500)
                        }
                    }
                }
            }
        }
    }

    private fun configureSearchVisibility() {
        if (adapter.allData.isEmpty()) edit_search.gone()
        else edit_search.visiable()
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

    override fun afterSuccessGetAddress(list: MutableList<AddressBookUser>) {
        adapter.allData = ArrayList(list)
        adapter.setNewData(list)
        adapter.emptyView = getEmptyView()
        configureSearchVisibility()
    }

    override fun afterFailedGetAddress() {
        adapter.emptyView = getEmptyView()
        configureSearchVisibility()
    }

    private fun getEmptyView(): View {
        val view = LayoutInflater.from(this).inflate(R.layout.address_book_empty_state, null)
        view.text_empty.text = getString(R.string.address_book_empty_state)
        return view
    }

    override fun onBackPressed() {
        finish()
        overridePendingTransition(R.anim.null_animation, R.anim.slide_out_right)
    }

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

    enum class AddressBookScreenType(var type: Int) {
        EDIT(0),
        CHOOSE(1)
    }
}
