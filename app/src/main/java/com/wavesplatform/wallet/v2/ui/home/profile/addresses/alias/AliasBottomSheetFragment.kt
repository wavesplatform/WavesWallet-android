/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.profile.addresses.alias

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.R
import com.wavesplatform.sdk.utils.MoneyUtil
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.sdk.model.response.api.AliasResponse
import com.wavesplatform.wallet.v2.data.analytics.AnalyticEvents
import com.wavesplatform.wallet.v2.data.analytics.analytics
import com.wavesplatform.wallet.v2.ui.base.view.BaseSuperBottomSheetDialogFragment
import com.wavesplatform.wallet.v2.ui.custom.ImageProgressBar
import com.wavesplatform.wallet.v2.ui.home.profile.addresses.alias.create.CreateAliasActivity
import com.wavesplatform.wallet.v2.util.launchActivity
import com.wavesplatform.sdk.utils.notNull
import com.wavesplatform.wallet.v2.util.showError
import kotlinx.android.synthetic.main.bottom_sheet_dialog_aliases_layout.view.*
import pers.victor.ext.click
import pers.victor.ext.gone
import pers.victor.ext.visiable
import javax.inject.Inject

class AliasBottomSheetFragment : BaseSuperBottomSheetDialogFragment(), AliasView {

    @Inject
    lateinit var adapter: AliasesAdapter

    @Inject
    @InjectPresenter
    lateinit var presenter: AliasPresenter

    @ProvidePresenter
    fun providePresenter(): AliasPresenter = presenter

    var onCreateAliasListener: OnCreateAliasListener? = null

    var type = TYPE_EMPTY
    var from = FROM_WALLET

    lateinit var rootView: View
    lateinit var progressBarFee: ImageProgressBar
    lateinit var feeTransaction: TextView
    lateinit var buttonCreateAlias: View

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        when (type) {
            TYPE_EMPTY -> {
                rootView = inflater.inflate(R.layout.bottom_sheet_dialog_aliases_empty_layout, container, false)
                buttonCreateAlias = rootView.findViewById<View>(R.id.button_create_alias)
                buttonCreateAlias.click {
                    launchActivity<CreateAliasActivity>(REQUEST_CREATE_ALIAS) {
                        putExtra(CreateAliasActivity.BUNDLE_BLOCKCHAIN_COMMISSION, presenter.fee)
                    }
                }
            }
            TYPE_CONTENT -> {
                adapter = AliasesAdapter()
                adapter.subscriptions = eventSubscriptions
                rootView = inflater.inflate(R.layout.bottom_sheet_dialog_aliases_layout, container, false)
                rootView.recycle_aliases.layoutManager = LinearLayoutManager(this.context)
                rootView.recycle_aliases.isNestedScrollingEnabled = false
                rootView.recycle_aliases.adapter = adapter

                buttonCreateAlias = rootView.findViewById<View>(R.id.button_create_alias)

                rootView.relative_about_alias.click {
                    if (rootView.expandable_layout_hidden.isExpanded) {
                        rootView.expandable_layout_hidden.collapse()
                        rootView.image_arrowup.animate()
                                .rotation(0f)
                                .setDuration(500)
                                .start()
                    } else {
                        rootView.expandable_layout_hidden.expand()
                        rootView.image_arrowup.animate()
                                .rotation(180f)
                                .setDuration(500)
                                .start()
                    }
                }

                buttonCreateAlias.click {
                    logEvent()
                    launchActivity<CreateAliasActivity>(REQUEST_CREATE_ALIAS) {
                        putExtra(CreateAliasActivity.BUNDLE_BLOCKCHAIN_COMMISSION, presenter.fee)
                    }
                }

                presenter.loadAliases {
                    adapter.setNewData(it)
                }
            }
        }
        progressBarFee = rootView.findViewById<ImageProgressBar>(R.id.progress_bar_fee_transaction)
        feeTransaction = rootView.findViewById(R.id.text_fee_transaction)
        return rootView
    }

    private fun logEvent() {
        if (from == FROM_WALLET) {
            analytics.trackEvent(AnalyticEvents.AliasCreateVcardEvent)
        } else {
            analytics.trackEvent(AnalyticEvents.AliasCreateProfileEvent)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.loadCommission(object : AliasPresenter.OnCommissionGetListener {

            override fun showCommissionLoading() {
                progressBarFee.show()
                feeTransaction.gone()
                buttonCreateAlias.isEnabled = false
            }

            override fun showCommissionSuccess(unscaledAmount: Long) {
                feeTransaction.text = MoneyUtil.getScaledText(unscaledAmount, 8)
                progressBarFee.hide()
                feeTransaction.visiable()
                buttonCreateAlias.isEnabled = true
            }

            override fun showCommissionError() {
                feeTransaction.text = "-"
                showError(R.string.common_error_commission_receiving, R.id.root)
                progressBarFee.hide()
                feeTransaction.visiable()
            }
        })
    }

    fun configureDialog(emptyType: Boolean, from: String) {
        type = if (emptyType) {
            AliasBottomSheetFragment.TYPE_EMPTY
        } else {
            AliasBottomSheetFragment.TYPE_CONTENT
        }
        this.from = from
    }

    override fun onDestroyView() {
        progressBarFee.hide()
        super.onDestroyView()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CREATE_ALIAS && resultCode == Constants.RESULT_OK) {
            val aliasModel = data?.getParcelableExtra<AliasResponse>(CreateAliasActivity.RESULT_ALIAS)
            aliasModel.notNull {
                onCreateAliasListener.notNull { listener ->
                    listener.onSuccess()
                }
            }
        }
    }

    override fun onNetworkConnectionChanged(networkConnected: Boolean) {
        buttonCreateAlias.isEnabled = networkConnected
    }

    override fun afterSuccessLoadAliases(ownAliases: List<AliasResponse>) {
    }

    interface OnCreateAliasListener {
        fun onSuccess()
    }

    companion object {
        const val TYPE_EMPTY = 1
        const val TYPE_CONTENT = 2
        const val REQUEST_CREATE_ALIAS = 43
        const val FROM_WALLET = "wallet"
        const val FROM_PROFILE = "profile"
    }
}