package com.wavesplatform.wallet.v2.ui.receive.bank

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.novoda.simplechromecustomtabs.SimpleChromeCustomTabs
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.base.view.BaseFragment
import com.wavesplatform.wallet.v2.util.makeLinks
import kotlinx.android.synthetic.main.fragment_bank.*
import pers.victor.ext.click
import javax.inject.Inject

class BankFragment : BaseFragment(), BankView {
    @Inject
    @InjectPresenter
    lateinit var presenter: BankPresenter

    @ProvidePresenter
    fun providePresenter(): BankPresenter = presenter

    override fun configLayoutRes(): Int = R.layout.fragment_bank

    companion object {

        /**
         * @return BankFragment instance
         * */
        fun newInstance(): BankFragment {
            return BankFragment()
        }
    }

    override fun onViewReady(savedInstanceState: Bundle?) {
        val siteClick = object : ClickableSpan() {
            override fun onClick(p0: View?) {
                SimpleChromeCustomTabs.getInstance()
                        .withFallback({
                            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.receive_step_url_link)))
                            startActivity(browserIntent)
                        }).withIntentCustomizer({
                            it.withToolbarColor(ContextCompat.getColor(activity!!, R.color.submit400))
                        })
                        .navigateTo(Uri.parse(getString(R.string.receive_step_1_msg_link)), activity!!)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = ContextCompat.getColor(activity!!, R.color.black)
            }
        }

        text_step_2_msg.makeLinks(arrayOf(getString(R.string.receive_step_1_msg_link)), arrayOf(siteClick))

        val listOfCountriesClick = object : ClickableSpan() {
            override fun onClick(p0: View?) {
                SimpleChromeCustomTabs.getInstance()
                        .withFallback({
                            //                            TODO CLICK
                        }).withIntentCustomizer({
                            it.withToolbarColor(ContextCompat.getColor(activity!!, R.color.submit400))
                        })
                        .navigateTo(Uri.parse(getString(R.string.receive_list_of_countries)), activity!!)
            }
        }

        text_list_of_countries.makeLinks(arrayOf(getString(R.string.receive_list_of_countries)), arrayOf(listOfCountriesClick))

        val contactMsgClick = object : ClickableSpan() {
            override fun onClick(p0: View?) {
                SimpleChromeCustomTabs.getInstance()
                        .withFallback({
                            //                            TODO CLICK
                        }).withIntentCustomizer({
                            it.withToolbarColor(ContextCompat.getColor(activity!!, R.color.submit400))
                        })
                        .navigateTo(Uri.parse(getString(R.string.receive_contact_msg_key)), activity!!)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = ContextCompat.getColor(activity!!, R.color.black)
            }
        }

        text_contact_msg.makeLinks(arrayOf(getString(R.string.receive_contact_msg_key)), arrayOf(contactMsgClick))

        button_get_verified.click {

        }
    }
}
