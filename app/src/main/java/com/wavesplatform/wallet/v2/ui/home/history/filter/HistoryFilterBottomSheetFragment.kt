/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.history.filter

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import com.vicpin.krealmextensions.queryAllAsync
import com.wavesplatform.sdk.model.response.node.AssetBalanceResponse
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.db.AssetBalanceDb
import com.wavesplatform.wallet.v2.ui.base.view.BaseBottomSheetDialogFragment
import com.wavesplatform.wallet.v2.ui.home.history.filter.adapter.AssetsAdapter
import com.wavesplatform.wallet.v2.ui.home.history.filter.adapter.TransferAdapter
import pers.victor.ext.gone
import pers.victor.ext.visiable

class HistoryFilterBottomSheetFragment : BaseBottomSheetDialogFragment() {
    var rooView: View? = null
    var inflater: LayoutInflater? = null
    var periodListSelected = arrayListOf<String>()
    var assetListSelected = arrayListOf<AssetBalanceResponse>()
    var transferistSelected = arrayListOf<TransferModel>()
    var closeBtn: Button? = null
    var filterBtn: Button? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        this.inflater = inflater

        rooView = inflater.inflate(R.layout.content_history_filter_bottom_sheet_dialog, container, false)

        closeBtn = rooView?.findViewById(R.id.button_close)
        filterBtn = rooView?.findViewById(R.id.button_filter)

        rooView?.findViewById<CheckBox>(R.id.checkbox_week)?.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                periodListSelected.add(buttonView.text.toString())
                buttonView.setBackgroundResource(R.drawable.period_checked)
            } else {
                periodListSelected.remove(buttonView.text.toString())
                buttonView.setBackgroundResource(R.drawable.period_normal)
            }

            checkIsItemSelected()
        }
        rooView?.findViewById<CheckBox>(R.id.checkbox_month)?.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                periodListSelected.add(buttonView.text.toString())
                buttonView.setBackgroundResource(R.drawable.period_checked)
            } else {
                periodListSelected.remove(buttonView.text.toString())
                buttonView.setBackgroundResource(R.drawable.period_normal)
            }

            checkIsItemSelected()
        }
        rooView?.findViewById<CheckBox>(R.id.checkbox_half_year)?.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                periodListSelected.add(buttonView.text.toString())
                buttonView.setBackgroundResource(R.drawable.period_checked)
            } else {
                periodListSelected.remove(buttonView.text.toString())
                buttonView.setBackgroundResource(R.drawable.period_normal)
            }

            checkIsItemSelected()
        }
        setupAssetsList()
        setupTransferList()

        return rooView
    }

    private fun setupAssetsList() {
        val recycleAssets = rooView?.findViewById<RecyclerView>(R.id.recycle_assets)
        val assetsAdapter = AssetsAdapter()

        recycleAssets?.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        recycleAssets?.adapter = assetsAdapter

        queryAllAsync<AssetBalanceDb> {
            assetsAdapter.setNewData(AssetBalanceDb.convertFromDb(it))
        }

        assetsAdapter.setOnItemClickListener { adapter, view, position ->
            val item = adapter.getItem(position) as AssetBalanceResponse
            if (item.isChecked) {
                assetListSelected.remove(item)
            } else {
                assetListSelected.add(item)
            }

            item.isChecked = !item.isChecked
            adapter?.setData(position, item)

            checkIsItemSelected()
        }
    }

    private fun setupTransferList() {
        val recycleTransfer = rooView?.findViewById<RecyclerView>(R.id.recycle_transfer)
        val transferAdapter = TransferAdapter()

        recycleTransfer?.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        recycleTransfer?.adapter = transferAdapter

        val transfeeList = arrayListOf<TransferModel>()
        transfeeList.add(TransferModel("MaksTorch", "3PCjZftzzhtY4ZLLBfsyvNxw8RwAgXZVZJW", false))
        transfeeList.add(TransferModel("MaksTorch1", "3PCjZftzzhtY4ZLLBfsyvNxw8RwAgXZVZJW", false))
        transfeeList.add(TransferModel("MaksTorch2", "3PCjZftzzhtY4ZLLBfsyvNxw8RwAgXZVZJW", false))
        transfeeList.add(TransferModel("MaksTorch3", "3PCjZftzzhtY4ZLLBfsyvNxw8RwAgXZVZJW", false))
        transfeeList.add(TransferModel("MaksTorch4", "3PCjZftzzhtY4ZLLBfsyvNxw8RwAgXZVZJW", false))
        transfeeList.add(TransferModel("MaksTorch5", "3PCjZftzzhtY4ZLLBfsyvNxw8RwAgXZVZJW", false))
        transferAdapter.setNewData(transfeeList)

        transferAdapter.setOnItemClickListener { adapter, view, position ->
            val item = adapter.getItem(position) as TransferModel
            if (item.isChecked) {
                transferistSelected.remove(item)
            } else {
                transferistSelected.add(item)
            }
            item.isChecked = !item.isChecked
            adapter?.setData(position, item)

            checkIsItemSelected()
        }
    }

    private fun checkIsItemSelected() {
        if (periodListSelected.isNotEmpty() && assetListSelected.isNotEmpty() && transferistSelected.isNotEmpty()) {
            closeBtn?.gone()
            filterBtn?.visiable()
        } else {
            filterBtn?.gone()
            closeBtn?.visiable()
        }
    }
}
