package com.wavesplatform.wallet.v2.ui.home.profile.backup

import android.content.ClipData
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.R.id.*
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.ui.home.profile.backup.confirm.ConfirmBackupPhraseActivity
import com.wavesplatform.wallet.v2.util.launchActivity
import kotlinx.android.synthetic.main.activity_backup_pharse.*
import pers.victor.ext.click
import pers.victor.ext.clipboardManager
import pers.victor.ext.dp2px
import pers.victor.ext.toast
import java.lang.StringBuilder
import javax.inject.Inject


class BackupPhraseActivity : BaseActivity(), BackupPhraseView {

    @Inject
    @InjectPresenter
    lateinit var presenter: BackupPhrasePresenter

    @ProvidePresenter
    fun providePresenter(): BackupPhrasePresenter = presenter

    companion object {
        val PHRASE_LIST = "phrase_list"
    }

    override fun configLayoutRes(): Int = R.layout.activity_backup_pharse

    override fun onViewReady(savedInstanceState: Bundle?) {
        setupToolbar(toolbar_view, View.OnClickListener { onBackPressed() }, true, getString(R.string.backup_pharse), R.drawable.ic_toolbar_back_black)

        val phraseList = arrayListOf<String>("mad", "more", "curious", "blister", "badlands", "jump", "honorary", "calling ", "hopeless",
                "hatch", "duplicate", "dismemberment", " harsh", "hitchhiker", "extortion")

        for (text in phraseList) {
            val textView = buildLabel(text)
            flow.addView(textView)
        }

        button_written_down.click {
            launchActivity<ConfirmBackupPhraseActivity> {
                putExtra(PHRASE_LIST, phraseList)
            }
        }
        image_copy.click {
            clipboardManager.primaryClip = ClipData.newPlainText(getString(R.string.app_name), phraseList.toString().replace("[", "").replace("]", "").replace(",", ""))
            toast(getString(R.string.copied))
        }
    }

    private fun buildLabel(text: String): TextView {
        val textView = TextView(this)
        textView.text = text
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
        textView.setPadding(dp2px(16f), dp2px(8f), dp2px(16f), dp2px(8f))

        return textView
    }
}
