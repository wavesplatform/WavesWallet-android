/*
 * Created by Eduard Zaydel on 7/8/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.widget.configuration.options

import android.os.Bundle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.chad.library.adapter.base.BaseQuickAdapter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.local.OptionsDialogItem
import com.wavesplatform.wallet.v2.data.model.local.OptionsDialogModel
import kotlinx.android.synthetic.main.bottom_sheet_dialog_options_layout.view.*

class OptionsBottomSheetFragment<T : OptionsDialogModel> : BottomSheetDialogFragment() {

    var onOptionSelectedListener: OnSelectedListener<T>? = null

    var adapter: OptionsAdapter<T> = OptionsAdapter()

    private var options = mutableListOf<T>()
    private var title = ""
    private var defaultPosition = 0

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        val rootView = inflater.inflate(R.layout.bottom_sheet_dialog_options_layout,
                container, false)

        rootView.option_title.text = title

        rootView.recycle_options.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this.context)
        rootView.recycle_options.adapter = adapter

        adapter.bindToRecyclerView(rootView.recycle_options)
        adapter.setNewData(createOptions())

        adapter.onItemClickListener = BaseQuickAdapter.OnItemClickListener { adapter, view, position ->
            val item = adapter.getItem(position) as OptionsDialogItem<T>
            onOptionSelectedListener?.onSelected(item.data)
            this.dismiss()
        }

        return rootView
    }

    private fun createOptions(): MutableList<OptionsDialogItem<T>> {
        return options
                .mapIndexed { index, data ->
                    return@mapIndexed OptionsDialogItem(data, index == defaultPosition)
                }
                .toMutableList()
    }

    fun configureDialog(options: MutableList<T>,
                        title: String,
                        defaultPosition: Int = 0) {
        this.options = options
        this.title = title
        this.defaultPosition = defaultPosition
    }

    interface OnSelectedListener<T> {
        fun onSelected(data: T)
    }

}