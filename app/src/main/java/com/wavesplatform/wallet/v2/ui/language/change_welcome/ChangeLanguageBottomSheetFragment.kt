package com.wavesplatform.wallet.v2.ui.language.change_welcome

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.chad.library.adapter.base.BaseQuickAdapter
import com.franmontiel.localechanger.LocaleChanger
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.local.PreferencesHelper
import com.wavesplatform.wallet.v2.data.model.local.Language
import com.wavesplatform.wallet.v2.data.model.local.LanguageItem
import com.wavesplatform.wallet.v2.ui.base.view.BaseBottomSheetDialogFragment
import com.wavesplatform.wallet.v2.util.notNull
import kotlinx.android.synthetic.main.fragment_bottom_sheet_change_language_layout.view.*
import pers.victor.ext.*
import java.util.*
import javax.inject.Inject


class ChangeLanguageBottomSheetFragment @Inject constructor() : BaseBottomSheetDialogFragment() {

    var adapter: LanguageAdapter = LanguageAdapter()
    var languageChooseListener: LanguageSelectListener? = null
    var currentLanguagePosition: Int = -1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_bottom_sheet_change_language_layout, container, false)


        rootView.recycle_language.layoutManager = LinearLayoutManager(activity)
        rootView.recycle_language.adapter = adapter

        adapter.setNewData(getLanguages())
        markCurrentSelectedLanguage()

        adapter.onItemClickListener = BaseQuickAdapter.OnItemClickListener { adapter, view, position ->
            val item = adapter.getItem(position) as LanguageItem

            if (currentLanguagePosition == position) return@OnItemClickListener

            val languageItemByCode = Language.getLanguageItemByCode(preferencesHelper.getLanguage())
            val positionCurrent = adapter.data.indexOf(languageItemByCode)
            if (position == positionCurrent){
                rootView.button_confirm.invisiable()
            }else{
                rootView.button_confirm.visiable()
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
                saveLanguage(it.language.code)
                LocaleChanger.setLocale(Locale(getString(it.language.code).toLowerCase()))
            }
            languageChooseListener?.onLanguageSelected()
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

    fun getLanguages(): MutableList<LanguageItem>? {
        return Language.getLanguagesItems()
    }

    fun saveLanguage(lang: Int) {
        preferencesHelper.setLanguage(lang)
    }

    interface LanguageSelectListener {
        fun onLanguageSelected()
    }
}