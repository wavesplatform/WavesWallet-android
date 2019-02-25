package com.wavesplatform.wallet.v2.ui.home.history

import android.os.Bundle
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.ui.home.MainActivity
import kotlinx.android.synthetic.main.activity_leasing_history.*

class HistoryActivity : BaseActivity() {

    override fun configLayoutRes() = R.layout.activity_leasing_history

    override fun onCreate(savedInstanceState: Bundle?) {
        overridePendingTransition(R.anim.slide_in_right, R.anim.null_animation)
        super.onCreate(savedInstanceState)
    }

    override fun onViewReady(savedInstanceState: Bundle?) {
        setStatusBarColor(R.color.basic50)
        setupToolbar(toolbar_view, true, getString(R.string.leasing_history_toolbar_title), R.drawable.ic_toolbar_back_black)

        val fragment = HistoryFragment.newInstance().apply {
            arguments = intent.extras
        }

        val elevationListener = object : MainActivity.OnElevationAppBarChangeListener {
            override fun onChange(elevateEnable: Boolean) {
                appbar_layout.isSelected = !elevateEnable
            }
        }

        fragment.setOnElevationChangeListener(elevationListener)

        openFragment(R.id.frame_fragment_container, fragment)
    }

    override fun needToShowNetworkMessage(): Boolean = true

    override fun onBackPressed() {
        finish()
        overridePendingTransition(R.anim.null_animation, R.anim.slide_out_right)
    }
}
