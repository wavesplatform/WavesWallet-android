package com.wavesplatform.wallet.v2.ui.language.choose

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.local.LanguageItem
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.ui.language.LanguageAdapter
import com.wavesplatform.wallet.v2.ui.language.LanguagePresenter
import com.wavesplatform.wallet.v2.ui.language.LanguageView
import com.wavesplatform.wallet.v2.ui.tutorial.TutorialActivity
import com.wavesplatform.wallet.v2.util.launchActivity
import com.wavesplatform.wallet.v2.util.notNull
import kotlinx.android.synthetic.main.activity_choose_language.*
import pers.victor.ext.click
import pers.victor.ext.dp2px
import pers.victor.ext.visiable
import java.util.*
import javax.inject.Inject


class ChooseLanguageActivity : BaseActivity(), LanguageView {

    @Inject
    @InjectPresenter
    lateinit var presenter: LanguagePresenter


    @Inject
    lateinit var adapter: LanguageAdapter

    @ProvidePresenter
    fun providePresenter(): LanguagePresenter = presenter

    override fun configLayoutRes() = R.layout.activity_choose_language

    override fun askPassCode() = false

    override fun onViewReady(savedInstanceState: Bundle?) {
        recycle_language.layoutManager = LinearLayoutManager(this)
        recycle_language.adapter = adapter

        adapter.setNewData(presenter.getLanguages())

        adapter.onItemClickListener = BaseQuickAdapter.OnItemClickListener { adapter, view, position ->
            val item = adapter.getItem(position) as LanguageItem

            if (presenter.currentLanguagePosition == position) return@OnItemClickListener

            if (presenter.currentLanguagePosition == -1) {
                // check new item
                frame_button_continue.visiable()
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

        button_continue.click {
            val item = adapter.getItem(presenter.currentLanguagePosition)
            item.notNull { langItem->
                presenter.saveLanguage(langItem.language.code)
            }
            exitAnimation()
        }
        enterAnimation()
    }

    private fun enterAnimation() {
        image_logo.post {
            image_logo.animate()
                    .translationY(-(image_logo.y - dp2px(80)))
                    .setDuration(500)
                    .withEndAction {
                        recycle_language.animate()
                                .alpha(1f)
                                .translationY(0f)
                                .setDuration(500)
                                .start()
                    }
                    .start()
        }
    }

    private fun exitAnimation() {
        relative_root.post {
            relative_root.animate()
                    .translationY(dp2px(50).toFloat())
                    .alpha(0f)
                    .setDuration(250)
                    .withEndAction {
                        launchActivity<TutorialActivity>(){
                            putExtra(TutorialActivity.BUNDLE_LANG, preferencesHelper.getLanguage())
                        }
                        overridePendingTransition(0, R.anim.fade_out)
                    }
                    .start()
        }
    }
}
