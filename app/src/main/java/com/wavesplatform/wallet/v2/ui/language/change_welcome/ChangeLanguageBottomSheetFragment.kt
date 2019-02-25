package com.wavesplatform.wallet.v2.ui.language.change_welcome

import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.chad.library.adapter.base.BaseQuickAdapter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.local.Language
import com.wavesplatform.wallet.v2.data.model.local.LanguageItem
import com.wavesplatform.wallet.v2.ui.base.view.BaseSuperBottomSheetDialogFragment
import com.wavesplatform.wallet.v2.util.notNull
import kotlinx.android.synthetic.main.fragment_bottom_sheet_change_language_layout.view.*
import pers.victor.ext.click
import pers.victor.ext.invisiable
import pers.victor.ext.visiable

class ChangeLanguageBottomSheetFragment : BaseSuperBottomSheetDialogFragment() {

    var adapter: LanguageAdapter = LanguageAdapter()
    var languageChooseListener: LanguageSelectListener? = null
    var currentLanguagePosition: Int = -1
    lateinit var behavior: BottomSheetBehavior<View>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val rootView = inflater.inflate(com.wavesplatform.wallet.R.layout.fragment_bottom_sheet_change_language_layout, container, false)

        rootView.recycle_language.layoutManager = LinearLayoutManager(activity)
        rootView.recycle_language.adapter = adapter

        adapter.setNewData(Language.getLanguagesItems())
        markCurrentSelectedLanguage()

        dialog.setOnShowListener { dialog ->
            val bottomSheetInternal = this.dialog.findViewById<View>(R.id.super_bottom_sheet)
            behavior = BottomSheetBehavior.from(bottomSheetInternal)
        }

        adapter.onItemClickListener = BaseQuickAdapter.OnItemClickListener { adapter, view, position ->
            val item = adapter.getItem(position) as LanguageItem

            if (currentLanguagePosition == position) return@OnItemClickListener

            val languageItemByCode = Language.getLanguageItemByCode(preferencesHelper.getLanguage())
            val positionCurrent = adapter.data.indexOf(languageItemByCode)
            if (position == positionCurrent) {
                rootView.frame_button_confirm.invisiable()
            } else {
                if (behavior.state != BottomSheetBehavior.STATE_EXPANDED) {
                    behavior.state = BottomSheetBehavior.STATE_EXPANDED
                }
                rootView.frame_button_confirm.visiable()
            }

            if (currentLanguagePosition == -1) {
                // check new item
                currentLanguagePosition = position
                item.checked = true
                adapter.setData(position, item)
            } else {
                // uncheck old item
                val currentCheckedItem = adapter.getItem(currentLanguagePosition) as LanguageItem
                currentCheckedItem.checked = false
                adapter.setData(currentLanguagePosition, currentCheckedItem)

                // check new item
                currentLanguagePosition = position
                item.checked = true
                adapter.setData(position, item)
            }
        }

        rootView.button_confirm.click {
            val item = adapter.getItem(currentLanguagePosition)
            item.notNull {
                languageChooseListener?.onLanguageSelected(it.language.code)
            }
            dismiss()
        }

        return rootView
    }

    private fun markCurrentSelectedLanguage() {
        val languageItemByCode = Language.getLanguageItemByCode(preferencesHelper.getLanguage())
        val position = adapter.data.indexOf(languageItemByCode)
        currentLanguagePosition = position
        val languageItem = adapter.getItem(position) as LanguageItem
        languageItem.checked = true
        adapter.setData(position, languageItem)
    }

    interface LanguageSelectListener {
        fun onLanguageSelected(lang: String)
    }
}