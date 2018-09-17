package com.wavesplatform.wallet.v2.ui.language.change_welcome

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.franmontiel.localechanger.LocaleChanger
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.local.Language
import com.wavesplatform.wallet.v2.data.model.local.LanguageItem
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.ui.language.LanguageAdapter
import com.wavesplatform.wallet.v2.ui.language.LanguagePresenter
import com.wavesplatform.wallet.v2.ui.language.LanguageView
import com.wavesplatform.wallet.v2.util.notNull
import kotlinx.android.synthetic.main.activity_change_language.*
import pers.victor.ext.click
import pers.victor.ext.gone
import pers.victor.ext.visiable
import java.util.*
import javax.inject.Inject


class ChangeLanguageActivity : BaseActivity(), LanguageView {

    @Inject
    @InjectPresenter
    lateinit var presenter: LanguagePresenter

    @Inject
    lateinit var adapter: LanguageAdapter

    @ProvidePresenter
    fun providePresenter(): LanguagePresenter = presenter

    override fun configLayoutRes() = R.layout.activity_change_language


    override fun onViewReady(savedInstanceState: Bundle?) {
        setupToolbar(toolbar_view, true, getString(R.string.profile_language_toolbar_title), R.drawable.ic_toolbar_back_black)

        recycle_language.layoutManager = LinearLayoutManager(this)
        recycle_language.adapter = adapter
        adapter.changeRootPadding = true

        adapter.setNewData(presenter.getLanguages())
        markCurrentSelectedLanguage()

        adapter.onItemClickListener = BaseQuickAdapter.OnItemClickListener { adapter, view, position ->
            val item = adapter.getItem(position) as LanguageItem

            if (presenter.currentLanguagePosition == position) return@OnItemClickListener

            val languageItemByCode = Language.getLanguageItemByCode(preferencesHelper.getLanguage())
            val positionCurrent = adapter.data.indexOf(languageItemByCode)
            if (position == positionCurrent){
                button_save.gone()
            }else{
                button_save.visiable()
            }

            if (presenter.currentLanguagePosition == -1) {
                // check new item
                presenter.currentLanguagePosition = position
                item.checked = true
                adapter.setData(position, item)
            } else {
                // uncheck old item
                val currentCheckedItem = adapter.getItem(presenter.currentLanguagePosition) as LanguageItem
                currentCheckedItem.checked = false
                adapter.setData(presenter.currentLanguagePosition, currentCheckedItem)

                // check new item
                presenter.currentLanguagePosition = position
                item.checked = true
                adapter.setData(position, item)
            }

        }

        button_save.click {
            val item = adapter.getItem(presenter.currentLanguagePosition)
            item.notNull {
                presenter.saveLanguage(it.language.code)
                LocaleChanger.setLocale(Locale(getString(it.language.code).toLowerCase()))
            }
            onBackPressed()
        }
    }

    private fun markCurrentSelectedLanguage() {
        val languageItemByCode = Language.getLanguageItemByCode(preferencesHelper.getLanguage())
        val position = adapter.data.indexOf(languageItemByCode)
        presenter.currentLanguagePosition = position
        val languageItem = adapter.getItem(position) as LanguageItem
        languageItem.checked = true
        adapter.setData(position, languageItem)
    }

}
