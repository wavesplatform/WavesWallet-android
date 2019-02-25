package com.wavesplatform.wallet.v2.ui.home.dex.markets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jakewharton.rxbinding2.view.RxView
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.remote.response.MarketResponse
import com.wavesplatform.wallet.v2.ui.base.view.BaseBottomSheetDialogFragment
import com.wavesplatform.wallet.v2.util.copyToClipboard
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.dex_markets_info_bottom_sheet_dialog_layout.view.*
import pers.victor.ext.visiable
import java.util.concurrent.TimeUnit

class DexMarketInformationBottomSheetFragment : BaseBottomSheetDialogFragment() {
    private var market: MarketResponse? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val rootView = inflater.inflate(R.layout.dex_markets_info_bottom_sheet_dialog_layout, container, false)

        rootView.text_name.text = "${market?.amountAssetShortName} / ${market?.priceAssetShortName}"

        rootView.text_amount_asset.text = String.format(getString(R.string.dex_markets_info_dialog_amount_asset), market?.amountAssetLongName)
        rootView.text_price_asset.text = String.format(getString(R.string.dex_markets_info_dialog_price_asset), market?.priceAssetLongName)
        rootView.text_amount_asset_value.text = market?.amountAsset
        rootView.text_price_asset_value.text = market?.priceAsset

        if (market?.popular == true) rootView.text_popular.visiable()

        eventSubscriptions.add(RxView.clicks(rootView.image_copy_amount_asset)
                .throttleFirst(1500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    rootView.image_copy_amount_asset.copyToClipboard(rootView.text_amount_asset_value.text.toString(), R.drawable.ic_copy_18_submit_400)
                })

        eventSubscriptions.add(RxView.clicks(rootView.image_copy_price_asset)
                .throttleFirst(1500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    rootView.image_copy_price_asset.copyToClipboard(rootView.text_price_asset_value.text.toString(), R.drawable.ic_copy_18_submit_400)
                })

        return rootView
    }

    fun withMarketInformation(market: MarketResponse) {
        this.market = market
    }
}