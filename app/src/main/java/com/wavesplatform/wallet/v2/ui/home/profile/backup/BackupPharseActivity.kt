package com.wavesplatform.wallet.v2.ui.home.profile.backup

import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import kotlinx.android.synthetic.main.activity_backup_pharse.*
import javax.inject.Inject


class BackupPharseActivity : BaseActivity(), BackupPharseView {

    @Inject
    @InjectPresenter
    lateinit var presenter: BackupPharsePresenter

    @ProvidePresenter
    fun providePresenter(): BackupPharsePresenter = presenter

    override fun configLayoutRes(): Int = R.layout.activity_backup_pharse

    override fun onViewReady(savedInstanceState: Bundle?) {
        setupToolbar(toolbar_view, View.OnClickListener { onBackPressed() }, true, getString(R.string.backup_pharse), R.drawable.ic_toolbar_back_black)

        val dummyTexts = arrayListOf<String>("nigga", "wanna", "go", "hack", "any", "wack", "host", "idot", "utmost")

        for (text in dummyTexts) {
            val textView = buildLabel(text)
            flow.addView(textView)
        }

    }

    private fun buildLabel(text: String): TextView {
        val textView = TextView(this)
        textView.text = text
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
        textView.setPadding(dpToPx(16f).toInt(), dpToPx(8f).toInt(), dpToPx(16f).toInt(), dpToPx(8f).toInt())

        return textView
    }

    private fun dpToPx(dp: Float): Float {
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)
    }
}
