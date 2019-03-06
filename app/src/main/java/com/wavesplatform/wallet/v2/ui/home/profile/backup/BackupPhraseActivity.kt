package com.wavesplatform.wallet.v2.ui.home.profile.backup

import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.widget.TextView
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.jakewharton.rxbinding2.view.RxView
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.ui.auth.new_account.NewAccountActivity
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.ui.home.profile.ProfileFragment
import com.wavesplatform.wallet.v2.ui.home.profile.backup.confirm.ConfirmBackupPhraseActivity
import com.wavesplatform.wallet.v2.util.copyToClipboard
import com.wavesplatform.wallet.v2.util.launchActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_backup_pharse.*
import pers.victor.ext.click
import pers.victor.ext.dp2px
import pers.victor.ext.findColor
import pers.victor.ext.invisiable
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class BackupPhraseActivity : BaseActivity(), BackupPhraseView {

    @Inject
    @InjectPresenter
    lateinit var presenter: BackupPhrasePresenter

    @ProvidePresenter
    fun providePresenter(): BackupPhrasePresenter = presenter

    override fun configLayoutRes(): Int = R.layout.activity_backup_pharse

    override fun askPassCode() = App.getAccessManager().getWallet() != null

    override fun onCreate(savedInstanceState: Bundle?) {
        overridePendingTransition(R.anim.slide_in_right, R.anim.null_animation)
        super.onCreate(savedInstanceState)
    }

    override fun onViewReady(savedInstanceState: Bundle?) {
        setupToolbar(toolbar_view, true,
                getString(R.string.backup_phrase), R.drawable.ic_toolbar_back_black)

        if (intent.hasExtra(NewAccountActivity.KEY_INTENT_PROCESS_ACCOUNT_CREATION)) {
            setSeed(intent.extras.getString(NewAccountActivity.KEY_INTENT_SEED, ""))
        } else {
            setSeed(App.getAccessManager().getWallet()?.seedStr ?: "")
        }

        if (!App.getAccessManager().isCurrentAccountBackupSkipped()) {
            button_written_down.invisiable()
            text_view_written_down_hint.invisiable()
        }
    }

    private fun setSeed(seed: String) {
        val phraseList = if (seed.isNotEmpty()) {
            seed.split(" ")
        } else {
            arrayListOf()
        }

        for (text in phraseList) {
            val textView = buildLabel(text)
            flow.addView(textView)
        }

        button_written_down.click {
            launchActivity<ConfirmBackupPhraseActivity>(
                    requestCode = REQUEST_CONFIRM_BACKUP, options = intent.extras) {
                putStringArrayListExtra(KEY_INTENT_SEED_AS_ARRAY, ArrayList(phraseList))
                if (intent.hasExtra(ProfileFragment.KEY_INTENT_SET_BACKUP)) {
                    putExtra(ProfileFragment.KEY_INTENT_SET_BACKUP, true)
                }
            }
        }

        eventSubscriptions.add(RxView.clicks(image_copy)
                .throttleFirst(1500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    image_copy.copyToClipboard(phraseList.toString()
                            .replace("[", "")
                            .replace("]", "")
                            .replace(",", ""), R.drawable.ic_copy_18_submit_400)
                })
    }

    private fun buildLabel(text: String): TextView {
        val textView = TextView(this)
        textView.text = text
        textView.setTextColor(findColor(R.color.black))
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        textView.setPadding(dp2px(7f), dp2px(7f), dp2px(7f), dp2px(7f))
        return textView
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CONFIRM_BACKUP -> {
                if (resultCode == Constants.RESULT_OK) {
                    finish()
                }
            }
        }
    }

    override fun onBackPressed() {
        finish()
        overridePendingTransition(R.anim.null_animation, R.anim.slide_out_right)
    }

    companion object {
        const val KEY_INTENT_SEED_AS_ARRAY = "intent_seed_as_array"
        const val REQUEST_CONFIRM_BACKUP = 5555
    }
}
