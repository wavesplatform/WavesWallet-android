/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.dex.trade.buy_and_sell.smart_info

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.wavesplatform.sdk.net.model.response.AssetInfo
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.base.view.BaseBottomSheetDialogFragment
import kotlinx.android.synthetic.main.fragment_bottom_sheet_smart_pair_info_layout.view.*
import pers.victor.ext.click
import pers.victor.ext.visiableIf

class SmartPairInfoBottomSheetFragment : BaseBottomSheetDialogFragment() {

    private lateinit var amountInfo: AssetInfo
    private lateinit var priceInfo: AssetInfo
    private lateinit var listener: SmartPairDialogListener

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val rootView = inflater.inflate(R.layout.fragment_bottom_sheet_smart_pair_info_layout, container, false)

        rootView.image_asset_amount.setAsset(amountInfo)
        rootView.image_asset_price.setAsset(priceInfo)

        rootView.image_asset_amount.visiableIf { amountInfo.hasScript }
        rootView.image_asset_price.visiableIf { priceInfo.hasScript }

        rootView.button_continue.click {
            listener.onContinueClicked(rootView.checkbox_not_show_again.isChecked)
            dialog.dismiss()
        }

        rootView.button_cancel.click {
            listener.onCancelClicked(rootView.checkbox_not_show_again.isChecked)
            dialog.dismiss()
        }

        return rootView
    }

    fun configureDialog(amountInfo: AssetInfo, priceInfo: AssetInfo, listener: SmartPairDialogListener) {
        this.amountInfo = amountInfo
        this.priceInfo = priceInfo
        this.listener = listener
    }

    interface SmartPairDialogListener {
        fun onContinueClicked(notShowAgain: Boolean)
        fun onCancelClicked(notShowAgain: Boolean)
    }

}