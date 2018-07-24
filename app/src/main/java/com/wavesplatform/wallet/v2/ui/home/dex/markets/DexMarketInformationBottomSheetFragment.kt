package com.wavesplatform.wallet.v2.ui.home.dex.markets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.remote.response.Market
import com.wavesplatform.wallet.v2.ui.base.view.BaseBottomSheetDialogFragment
import com.wavesplatform.wallet.v2.util.copyToClipboard
import kotlinx.android.synthetic.main.dex_markets_info_bottom_sheet_dialog_layout.view.*
import pers.victor.ext.click


class DexMarketInformationBottomSheetFragment : BaseBottomSheetDialogFragment() {
    private var market: Market? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.dex_markets_info_bottom_sheet_dialog_layout, container, false)

        rootView.text_name.text = "${market?.amountAssetName} / ${market?.priceAssetName}"

        rootView.text_amount_asset.text = String.format(getString(R.string.dex_markets_info_dialog_amount_asset), market?.amountAssetName)
        rootView.text_price_asset.text = String.format(getString(R.string.dex_markets_info_dialog_price_asset), market?.priceAssetName)
        rootView.text_amount_asset_value.text = market?.amountAsset
        rootView.text_price_asset_value.text = market?.priceAsset

        rootView.image_copy_amount_asset.click {
            rootView.text_amount_asset_value.copyToClipboard(it, R.drawable.ic_copy_18_submit_400)
        }
        rootView.image_copy_price_asset.click {
            rootView.text_price_asset_value.copyToClipboard(it, R.drawable.ic_copy_18_submit_400)
        }

        return rootView
    }

    fun withMarketInformation(market: Market){
        this.market = market
    }
}