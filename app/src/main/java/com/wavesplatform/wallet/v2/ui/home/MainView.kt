/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home

import com.wavesplatform.wallet.v2.data.model.local.MigrateAccountItem
import com.wavesplatform.wallet.v2.data.model.local.widget.MyAccountItem
import com.wavesplatform.wallet.v2.data.model.service.configs.NewsResponse
import com.wavesplatform.wallet.v2.ui.base.view.BaseMvpView

interface MainView : BaseMvpView {
    fun showNews(news: NewsResponse)
    fun afterSuccessGetAddress(accounts: MutableList<MyAccountItem>)
}
