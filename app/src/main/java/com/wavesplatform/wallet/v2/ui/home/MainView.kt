package com.wavesplatform.wallet.v2.ui.home

import com.wavesplatform.sdk.model.response.News
import com.wavesplatform.wallet.v2.ui.base.view.BaseMvpView

interface MainView : BaseMvpView {
    fun showNews(news: News)
}
