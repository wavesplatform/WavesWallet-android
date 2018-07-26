package com.wavesplatform.wallet.v2.ui.home.profile.backup

import android.content.ClipData
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.ui.home.profile.backup.confirm.ConfirmBackupPhraseActivity
import com.wavesplatform.wallet.v2.util.copyToClipboard
import com.wavesplatform.wallet.v2.util.launchActivity
import kotlinx.android.synthetic.main.activity_backup_pharse.*
import pers.victor.ext.*
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

        val phraseList = arrayListOf<String>("utmost", "get", "igot", "nigga", "host", "wanna", "stacks", "attack ", "close",
                "too", "get", "wack", "that’ll", "but", "any", "tothe")

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
            image_copy.copyToClipboard(phraseList.toString().replace("[", "").replace("]", "").replace(",", ""), R.drawable.ic_copy_18_submit_400)
        }
    }

    private fun buildLabel(text: String): TextView {
        val textView = TextView(this)
        textView.text = text
        textView.setTextColor(findColor(R.color.black))
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        textView.setPadding(dp2px(7f), dp2px(7f), dp2px(7f), dp2px(7f))

        return textView
    }
}
