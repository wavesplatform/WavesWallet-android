package com.wavesplatform.wallet.v2.ui.home.profile.backup

import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v1.data.access.AccessState
import com.wavesplatform.wallet.v1.data.auth.WavesWallet
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.ui.auth.new_account.NewAccountActivity
import com.wavesplatform.wallet.v2.ui.auth.passcode.enter.EnterPassCodeActivity
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.ui.home.profile.ProfileFragment
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

    override fun configLayoutRes(): Int = R.layout.activity_backup_pharse

    override fun onViewReady(savedInstanceState: Bundle?) {
        setupToolbar(toolbar_view, View.OnClickListener { onBackPressed() }, true,
                getString(R.string.backup_pharse), R.drawable.ic_toolbar_back_black)

        if (intent.hasExtra(NewAccountActivity.KEY_INTENT_PROCESS_ACCOUNT_CREATION)) {
            setSeed(intent.extras.getString(NewAccountActivity.KEY_INTENT_SEED))
        } else {
            launchActivity<EnterPassCodeActivity>(
                    requestCode = EnterPassCodeActivity.REQUEST_ENTER_PASS_CODE)
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
            launchActivity<ConfirmBackupPhraseActivity>(options = intent.extras) {
                putExtra(KEY_INTENT_SEED_AS_ARRAY, phraseList.toTypedArray())
                if (intent.hasExtra(ProfileFragment.KEY_INTENT_SET_BACKUP)) {
                    putExtra(ProfileFragment.KEY_INTENT_SET_BACKUP, true)
                }
            }
        }

        image_copy.click {
            image_copy.copyToClipboard(phraseList.toString()
                    .replace("[", "")
                    .replace("]", "")
                    .replace(",", ""), R.drawable.ic_copy_18_submit_400)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            EnterPassCodeActivity.REQUEST_ENTER_PASS_CODE -> {
                if (resultCode == Constants.RESULT_OK) {
                    val password = data!!.extras.getString(NewAccountActivity.KEY_INTENT_PASSWORD)
                    val wallet = WavesWallet(
                            AccessState.getInstance().currentWavesWalletEncryptedData, password)
                    setSeed(wallet.seedStr)
                }
            }
        }
    }

    companion object {
        const val KEY_INTENT_SEED_AS_ARRAY = "intent_seed_as_array"
    }
}
