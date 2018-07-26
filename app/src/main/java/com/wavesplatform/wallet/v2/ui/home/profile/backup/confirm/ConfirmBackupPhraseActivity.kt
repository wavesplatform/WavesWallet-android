package com.wavesplatform.wallet.v2.ui.home.profile.backup.confirm

import android.graphics.Color
import android.os.Bundle
import android.support.v4.view.ViewCompat.animate
import android.support.v7.widget.CardView
import android.util.TypedValue
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.R.string.click
import com.wavesplatform.wallet.v2.ui.auth.passcode.create.CreatePasscodeActivity
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.ui.home.profile.backup.BackupPhraseActivity
import com.wavesplatform.wallet.v2.util.launchActivity
import com.wavesplatform.wallet.v2.util.setMargins
import kotlinx.android.synthetic.main.activity_confirm_backup_pharse.*
import kotlinx.android.synthetic.main.activity_confirm_backup_pharse.view.*
import pers.victor.ext.*
import java.lang.StringBuilder
import javax.inject.Inject

class ConfirmBackupPhraseActivity : BaseActivity(), ConfirmBackupPhraseView {

    @Inject
    @InjectPresenter
    lateinit var presenter: ConfirmBackupPhrasePresenter

    @ProvidePresenter
    fun providePresenter(): ConfirmBackupPhrasePresenter = presenter

    override fun configLayoutRes(): Int = R.layout.activity_confirm_backup_pharse

    override fun onViewReady(savedInstanceState: Bundle?) {
        setupToolbar(toolbar_view, View.OnClickListener { onBackPressed() }, true, getString(R.string.confirm_backup), R.drawable.ic_toolbar_back_black)

        val originPhrase = intent?.getSerializableExtra(BackupPhraseActivity.PHRASE_LIST) as ArrayList<String>

        presenter.getRandomPhrasePositions(originPhrase)
    }

    override fun showRandomPhraseList(listRandomPhrase: ArrayList<String>) {
        for (i in 0 until listRandomPhrase.size) {
            val textView = buildRandomLabel(listRandomPhrase[i], i)
            flow_random_phrase.addView(textView)
        }
    }

    private fun buildRandomLabel(text: String, position: Int): FrameLayout {
        val frame = FrameLayout(this)
        frame.setBackgroundResource(R.drawable.shape_rect_outline_basic300_transparent)
        val textView = TextView(this)
        textView.text = text
        textView.setTextColor(Color.WHITE)
        textView.setBackgroundResource(R.drawable.blue_shape)
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        textView.setPadding(dp2px(14f), dp2px(8f), dp2px(14f), dp2px(8f))
        var clickListener = View.OnClickListener {
            it.isClickable = false
            it.animate()
                    .scaleX(0f)
                    .scaleY(0f)
                    .setDuration(200)
                    .withEndAction({
                        it.invisiable()
                        it.isClickable = true
                    })
                    .start()
            val confirmLabel = buildConfirmLabel(text, position)
            flow_confirm.addView(confirmLabel)
            confirmLabel.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(200)
                    .withEndAction({

                    })
                    .start()
            checkCountToChangeTextVisibility()

            if (flow_confirm.childCount == presenter.originPhraseList.size) {
                val phraseText = StringBuilder()
                for (view in flow_confirm.children) {
                    val childAt = (view as CardView).getChildAt(0) as TextView
                    phraseText.append(childAt.text.toString() + " ")
                }

                if (phraseText.trim() == presenter.originPhraseString) {
                    button_confirm.visiable()
                    button_confirm.click {
                        launchActivity<CreatePasscodeActivity> { }
                    }
                } else {
                    text_error.visiable()
                    frame_phrase_form.foreground = findDrawable(R.drawable.shape_rect_outline_red)
                }
            }
        }
        textView.setOnClickListener(clickListener)
        frame.addView(textView)
        return frame
    }

    private fun buildConfirmLabel(text: String, randomPosition: Int): CardView {
        val cardView = CardView(this)
        val textView = TextView(this)
        textView.text = text
        textView.setMargins(dp2px(4), dp2px(7), dp2px(4), dp2px(7))
        textView.setTextColor(findColor(R.color.black))
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        textView.setPadding(dp2px(14f), dp2px(8f), dp2px(14f), dp2px(8f))
        textView.setOnClickListener({
            cardView.animate()
                    .scaleX(0f)
                    .scaleY(0f)
                    .setDuration(200)
                    .withEndAction({
                        flow_confirm.removeView(cardView)
                    })
                    .start()
            checkCountToChangeTextVisibility()
            val frameDotted = flow_random_phrase.getChildAt(randomPosition) as FrameLayout
            var textView = frameDotted.getChildAt(0)
            textView.visiable()
            textView.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(200)
                    .withEndAction({

                    })
                    .start()

            button_confirm.gone()

            if (text_error.isVisible()) {
                text_error.gone()
                frame_phrase_form.foreground = findDrawable(R.drawable.shape_rect_outline_gray)
            }
        })
        cardView.radius = dp2px(2).toFloat()
        cardView.cardElevation = dp2px(2).toFloat()
        cardView.scaleX = 0f
        cardView.scaleY = 0f
        cardView.setCardBackgroundColor(Color.WHITE)
        cardView.addView(textView)
        return cardView
    }

    private fun checkCountToChangeTextVisibility() {
        if (flow_confirm.childCount == 0) {
            text_please_tap.visiable()
        } else {
            text_please_tap.gone()
        }
    }
}
