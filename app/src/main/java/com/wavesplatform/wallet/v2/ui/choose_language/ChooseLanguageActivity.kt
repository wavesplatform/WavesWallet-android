package com.wavesplatform.wallet.v2.ui.choose_language

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.local.Language
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import kotlinx.android.synthetic.main.activity_choose_language.*
import pers.victor.ext.visiable
import javax.inject.Inject


class ChooseLanguageActivity : BaseActivity(), ChooseLanguageView {

    @Inject
    @InjectPresenter
    lateinit var presenter: ChooseLanguagePresenter


    @Inject
    lateinit var adapter: ChooseLanguageAdapter

    @ProvidePresenter
    fun providePresenter(): ChooseLanguagePresenter = presenter

    override fun configLayoutRes() = R.layout.activity_choose_language


    override fun onViewReady(savedInstanceState: Bundle?) {

        recycle_language.layoutManager = LinearLayoutManager(this)
        recycle_language.adapter = adapter

        adapter.setNewData(presenter.getLanguages())

        adapter.onItemClickListener = BaseQuickAdapter.OnItemClickListener { adapter, view, position ->
            val item = adapter.getItem(position) as Language
            if (presenter.currentLanguagePosition == -1) {
                // check new item
                button_continue.visiable()
                presenter.currentLanguagePosition = position
                item.checked = true
                adapter.setData(position, item)
            } else {
                // uncheck old item
                val currentCheckedItem = adapter.getItem(presenter.currentLanguagePosition) as Language
                currentCheckedItem.checked = false
                adapter.setData(presenter.currentLanguagePosition, currentCheckedItem)

                // check new item
                presenter.currentLanguagePosition = position
                item.checked = true
                adapter.setData(position, item)
            }
        }
    }

}
