package com.wavesplatform.wallet.v2.ui.language.change

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import javax.inject.Inject

import com.arellomobile.mvp.presenter.InjectPresenter
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity;

import com.arellomobile.mvp.presenter.ProvidePresenter;
import com.chad.library.adapter.base.BaseQuickAdapter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.local.Language
import com.wavesplatform.wallet.v2.ui.language.LanguageAdapter
import com.wavesplatform.wallet.v2.ui.language.LanguagePresenter
import com.wavesplatform.wallet.v2.ui.language.LanguageView
import kotlinx.android.synthetic.main.activity_change_language.*
import pers.victor.ext.dp2px
import pers.victor.ext.screenHeight
import pers.victor.ext.visiable
import android.util.TypedValue




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
        // initial position of animation
        card_content.translationY = screenHeight.toFloat()

        setupToolbar(toolbar_view, View.OnClickListener { onBackPressed() }, true, getString(R.string.change_language_toolbar_title), R.drawable.ic_toolbar_back_white)

        recycle_language.layoutManager = LinearLayoutManager(this)
        recycle_language.adapter = adapter

        adapter.setNewData(presenter.getLanguages())

        adapter.onItemClickListener = BaseQuickAdapter.OnItemClickListener { adapter, view, position ->
            val item = adapter.getItem(position) as Language

            if (presenter.currentLanguagePosition == position) return@OnItemClickListener

            if (presenter.currentLanguagePosition == -1) {
                // check new item
                button_confirm.visiable()
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

        enterAnimation()
    }

    override fun onBackPressed() {
        exitAnimation()
    }

    private fun enterAnimation() {
        card_content.post {
            card_content.animate()
                    .translationY(dp2px(2).toFloat())
                    .setDuration(500)
                    .setStartDelay(100)
                    .withEndAction({
                        relative_content.animate()
                                .alpha(1f)
                                .translationY(0f)
                                .setDuration(500)
                                .start()
                    })
                    .start()
        }
    }


    private fun exitAnimation() {
        val tv = TypedValue()
        var actionBarHeight = 0
        if (theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, resources.displayMetrics)
        }
        card_content.post {
            card_content.animate()
                    .translationY(screenHeight.toFloat() - actionBarHeight)
                    .setDuration(500)
                    .withEndAction({
                        finish()
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    })
                    .start()
        }
    }


}
