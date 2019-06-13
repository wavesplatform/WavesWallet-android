/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.language.choose

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.wavesplatform.sdk.model.Language
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.util.ViewUtils
import com.wavesplatform.sdk.model.LanguageItem
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.ui.language.LanguageAdapter
import com.wavesplatform.wallet.v2.ui.language.LanguagePresenter
import com.wavesplatform.wallet.v2.ui.language.LanguageView
import com.wavesplatform.wallet.v2.ui.tutorial.TutorialActivity
import com.wavesplatform.wallet.v2.util.getLocalizedString
import com.wavesplatform.wallet.v2.util.launchActivity
import com.wavesplatform.sdk.utils.notNull
import kotlinx.android.synthetic.main.activity_choose_language.*
import pers.victor.ext.click
import pers.victor.ext.dp2px
import pers.victor.ext.visiable
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
        val logo = layoutInflater
                .inflate(R.layout.content_splash_text_logo, null)
        adapter.setHeaderView(logo)
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

            setLocalizedTextToButton(item)
        }

        button_continue.click {
            val item = adapter.getItem(presenter.currentLanguagePosition)
            item.notNull { langItem ->
                presenter.saveLanguage(langItem.language.code)
            }
            exitAnimation()
        }
        enterAnimation()
    }

    private fun setLocalizedTextToButton(item: LanguageItem) {
        button_continue.text = getLocalizedString(R.string.choose_language_confirm, Language.getLocale(item.language.code))
    }

    private fun enterAnimation() {
        image_logo.post {
            image_logo.animate()
                    .translationY(- image_logo.y +
                            ViewUtils.convertDpToPixel(12f, this))
                    .setDuration(500)
                    .withEndAction {
                        recycle_language.animate()
                                .alpha(1f)
                                .setDuration(50)
                                .withEndAction { image_logo.alpha = 0f }
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
                        launchActivity<TutorialActivity> {
                            putExtra(TutorialActivity.BUNDLE_LANG, preferencesHelper.getLanguage())
                        }
                        overridePendingTransition(0, R.anim.fade_out)
                    }
                    .start()
        }
    }
}
