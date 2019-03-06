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
import com.wavesplatform.wallet.v1.util.MoneyUtil
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.model.remote.response.Alias
import com.wavesplatform.wallet.v2.ui.base.view.BaseSuperBottomSheetDialogFragment
import com.wavesplatform.wallet.v2.ui.custom.ImageProgressBar
import com.wavesplatform.wallet.v2.ui.home.profile.addresses.alias.create.CreateAliasActivity
import com.wavesplatform.wallet.v2.util.launchActivity
import com.wavesplatform.wallet.v2.util.notNull
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

    override fun onDestroyView() {
        progressBarFee.hide()
        super.onDestroyView()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CREATE_ALIAS && resultCode == Constants.RESULT_OK) {
            val aliasModel = data?.getParcelableExtra<Alias>(CreateAliasActivity.RESULT_ALIAS)
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

    override fun afterSuccessLoadAliases(ownAliases: List<Alias>) {
    }

    interface OnCreateAliasListener {
        fun onSuccess()
    }

    companion object {
        var TYPE_EMPTY = 1
        var TYPE_CONTENT = 2
        var REQUEST_CREATE_ALIAS = 43
    }
}