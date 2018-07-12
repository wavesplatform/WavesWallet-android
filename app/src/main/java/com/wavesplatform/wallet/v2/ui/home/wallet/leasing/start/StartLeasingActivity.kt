package com.wavesplatform.wallet.v2.ui.home.wallet.leasing.start

import android.os.Bundle
import android.view.View
import com.mindorks.editdrawabletext.DrawablePosition
import com.mindorks.editdrawabletext.onDrawableClickListener
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.ui.home.wallet.leasing.confirmation.ConfirmationLeasingActivity
import com.wavesplatform.wallet.v2.util.launchActivity
import kotlinx.android.synthetic.main.activity_start_leasing.*
import pers.victor.ext.click
import pers.victor.ext.toast

class StartLeasingActivity : BaseActivity(), StartLeasingView {


    override fun configLayoutRes(): Int = R.layout.activity_start_leasing

    override fun onViewReady(savedInstanceState: Bundle?) {
        setupToolbar(toolbar_view, View.OnClickListener { onBackPressed() }, true, getString(R.string.start_leasing_toolbar), R.drawable.ic_toolbar_back_black)

        text_choose_from_address.click {
            toast("Address book")
        }
        text_use_total_balance.click {
            toast("Total balance")
        }
        text_leasing_0_100.click {
            edit_amount.setText("0.100")
        }
        text_leasing_0_100000.click {
            edit_amount.setText("0.00100000")
        }
        text_leasing_0_500000.click {
            edit_amount.setText("0.00500000")
        }
        image_view_qr_code.click {
            toast("Open scan QR code")
        }

        button_continue.click {
            launchActivity<ConfirmationLeasingActivity> { }
        }
    }

}
