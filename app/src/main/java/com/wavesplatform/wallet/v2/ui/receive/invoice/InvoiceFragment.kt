package com.wavesplatform.wallet.v2.ui.receive.invoice

import android.os.Bundle
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.base.view.BaseFragment
import kotlinx.android.synthetic.main.fragment_invoice.*
import pers.victor.ext.click
import pers.victor.ext.gone
import pers.victor.ext.toast
import pers.victor.ext.visiable
import javax.inject.Inject

class InvoiceFragment : BaseFragment(), InvoiceView {
    @Inject
    @InjectPresenter
    lateinit var presenter: InvoicePresenter

    @ProvidePresenter
    fun providePresenter(): InvoicePresenter = presenter

    override fun configLayoutRes(): Int = R.layout.fragment_invoice

    companion object {

        /**
         * @return InvoiceFragment instance
         * */
        fun newInstance(): InvoiceFragment {
            return InvoiceFragment()
        }
    }

    override fun onViewReady(savedInstanceState: Bundle?) {

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

        edit_asset.click {
            edit_asset.gone()
            container_asset.visiable()
            button_continue.isEnabled = true
//            TODO open asset list screen
        }
    }
}
