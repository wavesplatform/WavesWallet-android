package com.wavesplatform.wallet.v2.ui.home.profile.addresses.alias

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.vicpin.krealmextensions.queryAllAsync
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.model.remote.response.Alias
import com.wavesplatform.wallet.v2.ui.base.view.BaseBottomSheetDialogFragment
import com.wavesplatform.wallet.v2.ui.home.profile.addresses.alias.create.CreateAliasActivity
import com.wavesplatform.wallet.v2.util.copyToClipboard
import com.wavesplatform.wallet.v2.util.launchActivity
import com.wavesplatform.wallet.v2.util.notNull
import com.wavesplatform.wallet.v2.util.showSuccess
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.bottom_sheet_dialog_aliases_empty_layout.view.*
import kotlinx.android.synthetic.main.bottom_sheet_dialog_aliases_layout.view.*
import pers.victor.ext.click
import pers.victor.ext.dp2px
import pers.victor.ext.toast
import pyxis.uzuki.live.richutilskt.utils.runAsync
import javax.inject.Inject


class AddressesAndKeysBottomSheetFragment : BaseBottomSheetDialogFragment() {

    var type = TYPE_EMPTY
        set(value) {
            field = value
            fullScreenHeightEnable = value != TYPE_EMPTY
            when (value) {
                TYPE_CONTENT -> extraTopMargin = dp2px(4)
            }
        }

    @Inject
    lateinit var adapter: AliasesAdapter

    companion object {
        var TYPE_EMPTY = 1
        var TYPE_CONTENT = 2
        var REQUEST_CREATE_ALIAS = 43
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        var rootView = View(this.context)

        when (type) {
            TYPE_EMPTY -> {
                rootView = inflater.inflate(R.layout.bottom_sheet_dialog_aliases_empty_layout, container, false)

                rootView.button_create_alias.click {
                    launchActivity<CreateAliasActivity>(REQUEST_CREATE_ALIAS) { }
                }
            }
            TYPE_CONTENT -> {
                adapter = AliasesAdapter()
                adapter.subscriptions = eventSubscriptions
                rootView = inflater.inflate(R.layout.bottom_sheet_dialog_aliases_layout, container, false)
                rootView.recycle_aliases.layoutManager = LinearLayoutManager(this.context)
                rootView.recycle_aliases.isNestedScrollingEnabled = false
                rootView.recycle_aliases.adapter = adapter

                runAsync {
                    queryAllAsync<Alias> {
                        val ownAliases = it.filter { it.own }
                        adapter.setNewData(ownAliases)
                    }
                }

                rootView.relative_about_alias.click {
                    if (rootView.expandable_layout_hidden.isExpanded) {
                        rootView.expandable_layout_hidden.collapse()
                        rootView.image_arrowup.animate()
                                .rotation(0f)
                                .setDuration(500)
                                .start()
                    } else {
                        rootView.expandable_layout_hidden.expand()
                        rootView.image_arrowup.animate()
                                .rotation(180f)
                                .setDuration(500)
                                .start()
                    }
                }

                rootView.button_create_alias_content.click {
                    launchActivity<CreateAliasActivity>(REQUEST_CREATE_ALIAS) { }
                }
            }
        }

        return rootView
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CREATE_ALIAS && resultCode == Constants.RESULT_OK) {
            when (type) {
                TYPE_EMPTY -> {
                    dismiss()
                    val bottomSheetFragment = AddressesAndKeysBottomSheetFragment()
                    bottomSheetFragment.type = AddressesAndKeysBottomSheetFragment.TYPE_CONTENT
                    bottomSheetFragment.show(fragmentManager, bottomSheetFragment.tag)
                    showSuccess(getString(R.string.new_alias_success_create), R.id.root)
                }
                TYPE_CONTENT -> {
                    val aliasModel = data?.getParcelableExtra<Alias>(CreateAliasActivity.RESULT_ALIAS)
                    aliasModel.notNull {
                        adapter.addData(it)
                    }
                }
            }
        }
    }
}