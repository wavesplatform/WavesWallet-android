/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.quick_action.receive.invoice

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.sdk.model.response.node.AssetBalanceResponse
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import javax.inject.Inject

@InjectViewState
class InvoicePresenter @Inject constructor() : BasePresenter<InvoiceView>() {
    var assetBalance: AssetBalanceResponse? = null
}
